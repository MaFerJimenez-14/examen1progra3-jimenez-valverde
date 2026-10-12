package cr.ac.una.repository.jdbc;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.exception.DatosException;
import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;
import cr.ac.una.model.Libro;
import cr.ac.una.repository.EjemplarRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EjemplarRepositoryJDBC implements EjemplarRepository {

    private static final String INSERTAR = "INSERT INTO ejemplar (codigo, libro_isbn, estado) " + "VALUES (?, ?, ?)";
    
    private static final String BUSCAR_POR_CODIGO = "SELECT e.codigo, e.estado, " + "l.isbn, l.titulo, l.autor, l.anio " 
            + "FROM ejemplar e " + "INNER JOIN libro l ON e.libro_isbn = l.isbn " + "WHERE e.codigo = ?";
    
    private static final String LISTAR_POR_LIBRO = "SELECT e.codigo, e.estado, " + "l.isbn, l.titulo, l.autor, l.anio " 
            + "FROM ejemplar e " + "INNER JOIN libro l ON e.libro_isbn = l.isbn " + "WHERE e.libro_isbn = ? ORDER BY e.codigo";
    
    private static final String CONTAR_DISPONIBLES = "SELECT COUNT(*) FROM ejemplar " + "WHERE libro_isbn = ? AND estado = 'DISPONIBLE'";
    
    private static final String ACTUALIZAR_ESTADO = "UPDATE ejemplar SET estado = ? WHERE codigo = ?";
    
    private static final String ACTUALIZAR_ESTADO_CONDICIONAL = "UPDATE ejemplar SET estado = ? " + "WHERE codigo = ? AND estado = ?";

    @Override
    public void insertar(Ejemplar ejemplar) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(INSERTAR)) {
            
            ps.setString(1, ejemplar.getCodigo());
            ps.setString(2, ejemplar.getLibro().getIsbn());
            ps.setString(3, ejemplar.getEstado().name());
            
            ps.executeUpdate();
        } 
        catch (SQLException e) {
            throw new DatosException("Error al registrar el ejemplar.", e);
        }
    }

    @Override
    public Optional<Ejemplar> buscarPorCodigo(String codigo) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(BUSCAR_POR_CODIGO)) {
            
            ps.setString(1, codigo);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEjemplar(rs));
                }
            }
            return Optional.empty();
        } 
        catch (SQLException e) {
            throw new DatosException("Error al buscar el ejemplar.", e);
        }
    }

    @Override
    public List<Ejemplar> listarPorLibro(String isbn) {
        List<Ejemplar> ejemplares = new ArrayList<>();
        
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(LISTAR_POR_LIBRO)) {
            
            ps.setString(1, isbn);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ejemplares.add(mapearEjemplar(rs));
                }
            }
            return ejemplares;
        } 
        catch (SQLException e) {
            throw new DatosException("Error al listar los ejemplares del libro.", e);
        }
    }

    @Override
    public int contarDisponibles(String isbn) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(CONTAR_DISPONIBLES)) {
            
            ps.setString(1, isbn);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } 
        catch (SQLException e) {
            throw new DatosException("Error al consultar la disponibilidad.", e);
        }
    }

    @Override
    public void actualizarEstado(String codigo, EstadoEjemplar nuevoEstado) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(ACTUALIZAR_ESTADO)) {
            
            ps.setString(1, nuevoEstado.name());
            ps.setString(2, codigo);
            
            int filas = ps.executeUpdate();
            
            if (filas == 0) {
                throw new DatosException("No se actualizó el estado: el ejemplar no existe.");
            }
        } 
        catch (SQLException e) {
            throw new DatosException("Error al actualizar el estado del ejemplar.", e);
        }
    }

    @Override
    public boolean actualizarEstado(Connection conexion, String codigo, 
            EstadoEjemplar estadoEsperado, EstadoEjemplar nuevoEstado) {
        
        try (PreparedStatement ps = conexion.prepareStatement(ACTUALIZAR_ESTADO_CONDICIONAL)) {
            
            ps.setString(1, nuevoEstado.name());
            ps.setString(2, codigo);
            ps.setString(3, estadoEsperado.name());
            
            return ps.executeUpdate() == 1;
        } 
        catch (SQLException e) {
            throw new DatosException("Error al actualizar el estado del ejemplar " + "dentro de la transacción.", e);
        }
    }

    private Ejemplar mapearEjemplar(ResultSet rs) throws SQLException {
        Libro libro = new Libro(
                rs.getString("isbn"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getInt("anio"));
        
        EstadoEjemplar estado = EstadoEjemplar.valueOf(rs.getString("estado"));
        
        return new Ejemplar(rs.getString("codigo"), libro, estado);
    }
}

