package cr.ac.una.service;

import cr.ac.una.config.ProveedorConexion;
import cr.ac.una.model.Prestamo;
import cr.ac.una.repository.EjemplarRepository;
import cr.ac.una.repository.PrestamoRepository;
import cr.ac.una.repository.UsuarioRepository;

import java.time.Clock;
import java.util.List;

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
        this.usuarioRepository = usuarioRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.prestamoRepository = prestamoRepository;
        this.proveedorConexion = proveedorConexion;
        this.politica = politica;
        this.reloj = reloj;
    }

    public PrestamoService(UsuarioRepository usuarioRepository,
                           EjemplarRepository ejemplarRepository,
                           PrestamoRepository prestamoRepository,
                           ProveedorConexion proveedorConexion) {
        this(usuarioRepository, ejemplarRepository, prestamoRepository, proveedorConexion,
                PoliticaPrestamo.POR_DEFECTO, Clock.systemDefaultZone());
    }

    /* Registrar préstamo.
     * 1. Usuario existe y está activo            -> UsuarioInactivoException
     * 2. Usuario sin préstamos atrasados         -> UsuarioConAtrasoException
     * 3. Usuario con menos de max préstamos      -> LimitePrestamosException
     * 4. Ejemplar existe y está DISPONIBLE       -> EjemplarNoDisponibleException
     * 5. Transacción: INSERT prestamo (límite = hoy + diasPrestamo)
     *    + UPDATE ejemplar DISPONIBLE -> PRESTADO, si el UPDATE no afecta 1 fila, rollback.
     *
     * return el préstamo registrado, con su id generado */
    public Prestamo prestar(String identificacionUsuario, String codigoEjemplar) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    /* Registrar devolución.
     * 1. Préstamo existe y está ACTIVO           -> PrestamoNoActivoException (RN-04)
     * 2. Transacción: UPDATE prestamo (fecha_devolucion = hoy, DEVUELTO)
     *    + UPDATE ejemplar PRESTADO -> DISPONIBLE.
     *
     * return el préstamo devuelto, los días de atraso se obtienen con
     *         prestamo.diasAtraso(prestamo.getFechaDevolucion()) */
    public Prestamo devolver(int idPrestamo) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    /* Préstamos activos de un usuario (JOIN con libro). */
    public List<Prestamo> listarActivosPorUsuario(String identificacionUsuario) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    /* Reporte de préstamos atrasados (JOIN con usuario y libro). */
    public List<Prestamo> listarAtrasados() {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
}
