package cr.ac.una.repository.jdbc;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.exception.DatosException;
import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;
import cr.ac.una.model.EstadoPrestamo;
import cr.ac.una.model.Libro;
import cr.ac.una.model.Prestamo;
import cr.ac.una.model.Usuario;
import cr.ac.una.repository.PrestamoRepository;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrestamoRepositoryJDBC implements PrestamoRepository {

    private static final String INSERTAR = "INSERT INTO prestamo " + "(usuario_id, ejemplar_codigo, fecha_prestamo, fecha_limite, " 
            + "fecha_devolucion, estado) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String BUSCAR_POR_ID = "SELECT p.id, p.fecha_prestamo, p.fecha_limite, " + "p.fecha_devolucion, p.estado, p.multa, p.multa_pagada, " 
            + "u.identificacion, u.nombre, u.correo, u.activo, " + "e.codigo, e.estado AS estado_ejemplar, " + "l.isbn, l.titulo, l.autor, l.anio " 
            + "FROM prestamo p " + "INNER JOIN usuario u ON p.usuario_id = u.identificacion " + "INNER JOIN ejemplar e ON p.ejemplar_codigo = e.codigo " 
            + "INNER JOIN libro l ON e.libro_isbn = l.isbn " + "WHERE p.id = ?";

    private static final String REGISTRAR_DEVOLUCION = "UPDATE prestamo SET fecha_devolucion = ?, estado = ?, multa = ?, multa_pagada = FALSE " + "WHERE id = ? AND estado = ?";

    private static final String LISTAR_ACTIVOS_POR_USUARIO = "SELECT p.id, p.fecha_prestamo, p.fecha_limite, " + "p.fecha_devolucion, p.estado, p.multa, p.multa_pagada, "
            + "u.identificacion, u.nombre, u.correo, u.activo, " + "e.codigo, e.estado AS estado_ejemplar, " + "l.isbn, l.titulo, l.autor, l.anio "
            + "FROM prestamo p " + "INNER JOIN usuario u ON p.usuario_id = u.identificacion " + "INNER JOIN ejemplar e ON p.ejemplar_codigo = e.codigo "
            + "INNER JOIN libro l ON e.libro_isbn = l.isbn " + "WHERE p.usuario_id = ? AND p.estado = ? " + "ORDER BY p.fecha_prestamo";

    private static final String LISTAR_ATRASADOS = "SELECT p.id, p.fecha_prestamo, p.fecha_limite, " + "p.fecha_devolucion, p.estado, p.multa, p.multa_pagada, "
            + "u.identificacion, u.nombre, u.correo, u.activo, " + "e.codigo, e.estado AS estado_ejemplar, " + "l.isbn, l.titulo, l.autor, l.anio "
            + "FROM prestamo p " + "INNER JOIN usuario u ON p.usuario_id = u.identificacion " + "INNER JOIN ejemplar e ON p.ejemplar_codigo = e.codigo "
            + "INNER JOIN libro l ON e.libro_isbn = l.isbn " + "WHERE p.estado = ? AND p.fecha_limite < ? " + "ORDER BY p.fecha_limite";

    private static final String CONTAR_ACTIVOS = "SELECT COUNT(*) FROM prestamo " + "WHERE usuario_id = ? AND estado = ?";

    private static final String TIENE_ATRASADOS = "SELECT COUNT(*) FROM prestamo " + "WHERE usuario_id = ? AND estado = ? AND fecha_limite < ?";
    
    private static final String TIENE_MULTAS_PENDIENTES = "SELECT COUNT(*) FROM prestamo " + "WHERE usuario_id = ? " + "AND multa > 0 " + "AND multa_pagada = FALSE";
    
    private static final String LISTAR_MULTAS_PENDIENTES = "SELECT p.id, p.usuario_id, p.ejemplar_codigo, " + "p.fecha_prestamo, p.fecha_limite, p.fecha_devolucion, "
            + "p.estado, p.multa, p.multa_pagada, " + "u.identificacion, u.nombre, u.correo, u.activo, " + "l.isbn, l.titulo, l.autor, l.anio, "
            + "e.codigo, e.estado AS estado_ejemplar " + "FROM prestamo p " + "JOIN usuario u ON p.usuario_id = u.identificacion " 
            + "JOIN ejemplar e ON p.ejemplar_codigo = e.codigo " + "JOIN libro l ON e.libro_isbn = l.isbn " + "WHERE p.multa > 0 AND p.multa_pagada = FALSE " + "ORDER BY u.nombre";
    
    private static final String REGISTRAR_PAGO_MULTA = "UPDATE prestamo " + "SET multa_pagada = TRUE " + "WHERE id = ? " + "AND multa > 0 " + "AND multa_pagada = FALSE";
    
    @Override
    public int insertar(Connection conexion, Prestamo prestamo) {
        try (PreparedStatement ps = conexion.prepareStatement(INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, prestamo.getUsuario().getIdentificacion());
            ps.setString(2, prestamo.getEjemplar().getCodigo());
            ps.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(prestamo.getFechaLimite()));
            ps.setDate(5, null);
            ps.setString(6, EstadoPrestamo.ACTIVO.name());
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DatosException("No se pudo obtener el ID generado del préstamo.");
        } 
        catch (SQLException e) {
            throw new DatosException("Error al insertar el préstamo.", e);
        }
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(BUSCAR_POR_ID)) {
            
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPrestamo(rs));
                }
            }
            return Optional.empty();
        } 
        catch (SQLException e) {
            throw new DatosException("Error al buscar el préstamo.", e);
        }
    }

    @Override
    public void registrarDevolucion(Connection conexion, int id, LocalDate fechaDevolucion, BigDecimal multa) {
        try (PreparedStatement ps = conexion.prepareStatement(REGISTRAR_DEVOLUCION)) {
            
            ps.setDate(1, Date.valueOf(fechaDevolucion));
            ps.setString(2, EstadoPrestamo.DEVUELTO.name());
            ps.setBigDecimal(3, multa);
            ps.setInt(4, id);
            ps.setString(5, EstadoPrestamo.ACTIVO.name());
            
            int filas = ps.executeUpdate();
            
            if (filas != 1) {
                throw new DatosException("No se registró la devolución: el préstamo no existe " + "o ya no está activo.");
            }
        } 
        catch (SQLException e) {
            throw new DatosException("Error al registrar la devolución.", e);
        }
    }

    @Override
    public List<Prestamo> listarActivosPorUsuario(String identificacionUsuario) {
        List<Prestamo> prestamos = new ArrayList<>();
        
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(LISTAR_ACTIVOS_POR_USUARIO)) {
            
            ps.setString(1, identificacionUsuario);
            ps.setString(2, EstadoPrestamo.ACTIVO.name());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapearPrestamo(rs));
                }
            }
            return prestamos;
        } 
        catch (SQLException e) {
            throw new DatosException("Error al listar los préstamos activos del usuario.", e);
        }
    }

    @Override
    public List<Prestamo> listarAtrasados(LocalDate hoy) {
        List<Prestamo> prestamos = new ArrayList<>();
        
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(LISTAR_ATRASADOS)) {
            
            ps.setString(1, EstadoPrestamo.ACTIVO.name());
            ps.setDate(2, Date.valueOf(hoy));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapearPrestamo(rs));
                }
            }
            return prestamos;
        } 
        catch (SQLException e) {
            throw new DatosException("Error al consultar los préstamos atrasados.", e);
        }
    }

    @Override
    public int contarActivosPorUsuario(String identificacionUsuario) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(CONTAR_ACTIVOS)) {
            
            ps.setString(1, identificacionUsuario);
            ps.setString(2, EstadoPrestamo.ACTIVO.name());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } 
        catch (SQLException e) {
            throw new DatosException("Error al contar los préstamos activos.", e);
        }
    }

    @Override
    public boolean tieneAtrasados(String identificacionUsuario, LocalDate hoy) {
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement ps = conexion.prepareStatement(TIENE_ATRASADOS)) {
            
            ps.setString(1, identificacionUsuario);
            ps.setString(2, EstadoPrestamo.ACTIVO.name());
            ps.setDate(3, Date.valueOf(hoy));
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } 
        catch (SQLException e) {
            throw new DatosException("Error al comprobar los préstamos atrasados.", e);
        }
    }

    @Override
    public boolean tieneMultasPendientes(String identificacionUsuario) {
        try (Connection conexion = ConexionBD.obtenerConexion(); 
                PreparedStatement ps = conexion.prepareStatement(TIENE_MULTAS_PENDIENTES)) {

            ps.setString(1, identificacionUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatosException("Error al comprobar las multas pendientes del usuario.", e);
        }
    }
    
    @Override
    public List<Prestamo> listarMultasPendientes() {
        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion(); 
                PreparedStatement ps = conexion.prepareStatement(LISTAR_MULTAS_PENDIENTES); 
                ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                prestamos.add(mapearPrestamo(rs));
            }
            return prestamos;
        } 
        catch (SQLException e) {
            throw new DatosException("Error al consultar los préstamos con multas pendientes.", e);
        }
    }
    
    @Override
    public void registrarPagoMulta(Connection conexion, int idPrestamo) {
        try (PreparedStatement ps = conexion.prepareStatement(REGISTRAR_PAGO_MULTA)) {

            ps.setInt(1, idPrestamo);

            int filasActualizadas = ps.executeUpdate();

            if (filasActualizadas == 0) {
                throw new DatosException("No se pudo registrar el pago: el préstamo no existe, "
                        + "no tiene una multa pendiente o ya fue pagada.");
            }

        } catch (SQLException e) {
            throw new DatosException("Error al registrar el pago de la multa.", e);
        }
    }
    
    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(
                rs.getString("identificacion"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getBoolean("activo"));
        
        Libro libro = new Libro(
                rs.getString("isbn"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getInt("anio"));
        
        Ejemplar ejemplar = new Ejemplar(
                rs.getString("codigo"),
                libro,
                EstadoEjemplar.valueOf(rs.getString("estado_ejemplar")));
        
        Date fechaDevolucionSql = rs.getDate("fecha_devolucion");
        LocalDate fechaDevolucion = fechaDevolucionSql == null ? null : fechaDevolucionSql.toLocalDate();
        
        return new Prestamo(
                rs.getInt("id"),
                usuario,
                ejemplar,
                rs.getDate("fecha_prestamo").toLocalDate(),
                rs.getDate("fecha_limite").toLocalDate(),
                fechaDevolucion,
                EstadoPrestamo.valueOf(rs.getString("estado")),
                rs.getBigDecimal("multa"),
                rs.getBoolean("multa_pagada"));
    }
}
