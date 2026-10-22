package cr.ac.una.service;

import cr.ac.una.config.ProveedorConexion;

import cr.ac.una.exception.DatosException;
import cr.ac.una.exception.EjemplarNoDisponibleException;
import cr.ac.una.exception.EntidadNoEncontradaException;
import cr.ac.una.exception.LimitePrestamosException;
import cr.ac.una.exception.MultaPendienteException;
import cr.ac.una.exception.PrestamoNoActivoException;
import cr.ac.una.exception.UsuarioConAtrasoException;
import cr.ac.una.exception.UsuarioInactivoException;
import cr.ac.una.exception.ValidacionException;

import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;
import cr.ac.una.model.EstadoPrestamo;
import cr.ac.una.model.Prestamo;
import cr.ac.una.model.Usuario;

import cr.ac.una.repository.EjemplarRepository;
import cr.ac.una.repository.PrestamoRepository;
import cr.ac.una.repository.UsuarioRepository;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.SQLException;

import java.time.Clock;
import java.time.LocalDate;

import java.util.List;
import java.util.Objects;

/* Operación de negocio principal: prestar y devolver ejemplares.
 *
 * Esta clase es dueña de la transacción: obtiene la Connection, desactiva el
 * autocommit, llama a los repositorios con esa misma conexión y hace commit o rollback. */

public class PrestamoService {
    
    private final UsuarioRepository usuarioRepository;
    private final EjemplarRepository ejemplarRepository;
    private final PrestamoRepository prestamoRepository;
    private final ProveedorConexion proveedorConexion;
    private final PoliticaPrestamo politica;
    private final Clock reloj; // permite fijar "hoy" en las pruebas

    public PrestamoService(UsuarioRepository usuarioRepository,
                           EjemplarRepository ejemplarRepository,
                           PrestamoRepository prestamoRepository,
                           ProveedorConexion proveedorConexion,
                           PoliticaPrestamo politica,
                           Clock reloj) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.ejemplarRepository = Objects.requireNonNull(ejemplarRepository);
        this.prestamoRepository = Objects.requireNonNull(prestamoRepository);
        this.proveedorConexion = Objects.requireNonNull(proveedorConexion);
        this.politica = Objects.requireNonNull(politica);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public PrestamoService(UsuarioRepository usuarioRepository,
                           EjemplarRepository ejemplarRepository,
                           PrestamoRepository prestamoRepository,
                           ProveedorConexion proveedorConexion) {
        this(usuarioRepository, ejemplarRepository, prestamoRepository, proveedorConexion,
                PoliticaPrestamo.POR_DEFECTO, Clock.systemDefaultZone());
    }

    /* RF-07: Registra un préstamo y cambia el estado del ejemplar
     * dentro de una única transacción. */
    public Prestamo prestar(String identificacionUsuario, String codigoEjemplar) {
        validarTexto(identificacionUsuario, "La identificación del usuario");
        validarTexto(codigoEjemplar, "El código del ejemplar");

        String identificacion = identificacionUsuario.trim();
        String codigo = codigoEjemplar.trim();
        LocalDate hoy = LocalDate.now(reloj);

        // Consultar el usuario actual.
        Usuario usuario = usuarioRepository.buscarPorId(identificacion)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe un usuario con identificación: " + identificacion));

        // RN-05: el usuario debe estar activo.
        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException(
                    "El usuario está inactivo y no puede solicitar préstamos.");
        }

        // RN-02: no se permiten préstamos nuevos si hay atrasos.
        if (prestamoRepository.tieneAtrasados(identificacion, hoy)) {
            throw new UsuarioConAtrasoException(
                    "El usuario tiene préstamos atrasados y no puede "
                    + "solicitar otro préstamo.");
        }

        // RN-N2: no se permiten préstamos nuevos si hay multas pendientes.
        if (prestamoRepository.tieneMultasPendientes(identificacion)) {
            throw new MultaPendienteException(
                    "El usuario tiene multas pendientes de pago y no puede "
                    + "solicitar otro préstamo.");
        }
        
        // RN-03: respetar el máximo de préstamos activos.
        int activos = prestamoRepository.contarActivosPorUsuario(identificacion);

        if (activos >= politica.maxPrestamosActivos()) {
            throw new LimitePrestamosException("El usuario ya alcanzó el máximo de "
                    + politica.maxPrestamosActivos() + " préstamos activos.");
        }

        // RN-01: el ejemplar debe existir y estar disponible.
        Ejemplar ejemplar = ejemplarRepository.buscarPorCodigo(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe un ejemplar con código: " + codigo));

        if (ejemplar.getEstado() != EstadoEjemplar.DISPONIBLE) {
            throw new EjemplarNoDisponibleException("El ejemplar no está disponible para préstamo.");
        }

        LocalDate fechaLimite = hoy.plusDays(politica.diasPrestamo());

        Prestamo prestamo = new Prestamo(
                usuario,
                ejemplar,
                hoy,
                fechaLimite);

        // Insertar préstamo y cambiar el estado
        try (Connection conexion = proveedorConexion.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                // La actualización condicionada evita prestar un ejemplar
                // que otro proceso ya haya cambiado de estado
                boolean actualizado = ejemplarRepository.actualizarEstado(
                        conexion,
                        codigo,
                        EstadoEjemplar.DISPONIBLE,
                        EstadoEjemplar.PRESTADO);

                if (!actualizado) {
                    throw new EjemplarNoDisponibleException(
                            "El ejemplar dejó de estar disponible. "
                            + "Actualice la consulta e inténtelo de nuevo.");
                }

                int idGenerado = prestamoRepository.insertar(conexion, prestamo);

                if (idGenerado <= 0) {
                    throw new DatosException("No se pudo obtener el identificador del préstamo.");
                }

                prestamo.setId(idGenerado);
                conexion.commit();

                return prestamo;
            } 
            catch (RuntimeException | SQLException e) {
                rollback(conexion, e);

                if (e instanceof RuntimeException excepcion) {
                    throw excepcion;
                }

                throw new DatosException("No se pudo registrar el préstamo.", e);
            }
        } 
        catch (SQLException e) {
            throw new DatosException("Error de base de datos al registrar el préstamo.", e);
        }
    }

    /* RF-08: Registra la devolución y libera el ejemplar
     * dentro de una única transacción. */
    public Prestamo devolver(int idPrestamo) {
        if (idPrestamo <= 0) {
            throw new ValidacionException("El identificador del préstamo debe ser positivo.");
        }

        Prestamo prestamo = prestamoRepository.buscarPorId(idPrestamo)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe el préstamo con ID: " + idPrestamo));

        // RN-04: solo se devuelven préstamos activos.
        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new PrestamoNoActivoException("El préstamo ya fue devuelto o no está activo.");
        }

        LocalDate fechaDevolucion = LocalDate.now(reloj);

        // RN-N1: ₡200 por cada día de atraso (0 si se devuelve a tiempo).
        long diasAtraso = prestamo.diasAtraso(fechaDevolucion);
        BigDecimal multa = politica.calcularMulta(diasAtraso);

        try (Connection conexion = proveedorConexion.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                // La multa se guarda en la misma transacción de la devolución.
                prestamoRepository.registrarDevolucion(
                        conexion,
                        idPrestamo,
                        fechaDevolucion,
                        multa);

                boolean actualizado = ejemplarRepository.actualizarEstado(
                        conexion,
                        prestamo.getEjemplar().getCodigo(),
                        EstadoEjemplar.PRESTADO,
                        EstadoEjemplar.DISPONIBLE);

                if (!actualizado) {
                    throw new EjemplarNoDisponibleException(
                            "No se pudo liberar el ejemplar porque "
                            + "su estado cambió. No se guardó la devolución.");
                }

                conexion.commit();

                // Reflejar en el objeto el resultado guardado.
                prestamo.setFechaDevolucion(fechaDevolucion);
                prestamo.setEstado(EstadoPrestamo.DEVUELTO);
                prestamo.setMulta(multa);
                prestamo.setMultaPagada(false);

                return prestamo;
            } 
            catch (RuntimeException | SQLException e) {
                rollback(conexion, e);

                if (e instanceof RuntimeException excepcion) {
                    throw excepcion;
                }
                throw new DatosException("No se pudo registrar la devolución.", e);
            }
        } 
        catch (SQLException e) {
            throw new DatosException("Error de base de datos al registrar la devolución.", e);
        }
    }

    /* RF-09: Préstamos activos de un usuario (JOIN con libro). */
    public List<Prestamo> listarActivosPorUsuario(String identificacionUsuario) {
        validarTexto(identificacionUsuario, "La identificación del usuario");

        String identificacion = identificacionUsuario.trim();

        if (usuarioRepository.buscarPorId(identificacion).isEmpty()) {
            throw new EntidadNoEncontradaException(
                    "No existe un usuario con identificación: " + identificacion);
        }

        return prestamoRepository.listarActivosPorUsuario(identificacion);
    }
    

    /* RF-10: Reporte de préstamos atrasados (JOIN con usuario y libro). */
    public List<Prestamo> listarAtrasados() {
        return prestamoRepository.listarAtrasados(LocalDate.now(reloj));
    }
    
    /* Consulta los préstamos que tienen multas pendientes de pago */
    public List<Prestamo> listarMultasPendientes() {
        return prestamoRepository.listarMultasPendientes();
    }
    
    /* Registra el pago de la multa de un préstamo */
    public void pagarMulta(int idPrestamo) {
        if (idPrestamo <= 0) {
            throw new ValidacionException("El identificador del préstamo debe ser positivo.");
        }

        try (Connection conexion = proveedorConexion.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                Prestamo prestamo = prestamoRepository.buscarPorId(idPrestamo)
                        .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe el préstamo con ID: " + idPrestamo));

                if (prestamo.getMulta() == null
                        || prestamo.getMulta().compareTo(BigDecimal.ZERO) <= 0
                        || prestamo.isMultaPagada()) {
                    throw new ValidacionException("El préstamo no tiene una multa pendiente de pago.");
                }

                prestamoRepository.registrarPagoMulta(conexion, idPrestamo);
                conexion.commit();

                prestamo.setMultaPagada(true);
            } 
            catch (RuntimeException | SQLException e) {
                rollback(conexion, e);

                if (e instanceof RuntimeException excepcion) {
                    throw excepcion;
                }

                throw new DatosException("No se pudo registrar el pago de la multa.", e);
            }
        } 
        catch (SQLException e) {
            throw new DatosException("Error de base de datos al registrar el pago de la multa.", e);
        }
    }
    
    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException(campo + " es obligatorio.");
        }
    }

    /* Revierte la transacción sin ocultar la excepción original. */
    private void rollback(Connection conexion, Exception original) {
        try {
            conexion.rollback();
        } 
        catch (SQLException errorRollback) {
            original.addSuppressed(errorRollback);
        }
    }
}
