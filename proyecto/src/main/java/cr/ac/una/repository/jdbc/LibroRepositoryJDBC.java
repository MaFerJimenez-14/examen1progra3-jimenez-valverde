package cr.ac.una.repository.jdbc;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.exception.DatosException;
import cr.ac.una.model.Libro;
import cr.ac.una.repository.LibroRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

public class LibroRepositoryJDBC implements LibroRepository{

    private static final String INSERTAR = "INSERT INTO libro (isbn, titulo, autor, anio) " + "VALUES (?, ?, ?, ?)";
    
    private static final String BUSCAR_POR_ISBN = "SELECT isbn, titulo, autor, anio " + "FROM libro WHERE isbn = ?";
    
    private static final String LISTAR = "SELECT isbn, titulo, autor, anio " + "FROM libro ORDER BY titulo";
    
    private static final String ACTUALIZAR = "UPDATE libro SET titulo = ?, autor = ?, anio = ? " + "WHERE isbn = ?";
    
    @Override
    public void insertar(Libro libro) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(INSERTAR)) {
            
            ps.setString(1, libro.getIsbn());
            ps.setString(2, libro.getTitulo());
            ps.setString(3, libro.getAutor());
            ps.setInt(4, libro.getAnio());
            
            ps.executeUpdate();
        } 
        catch (SQLException e) {
            throw new DatosException("Error al registrar el libro.", e);
        }
    }

    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(BUSCAR_POR_ISBN)) {
            
            ps.setString(1, isbn);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearLibro(rs));
                }
            }
            
            return Optional.empty();
        } 
        catch (SQLException e) {
            throw new DatosException("Error al buscar el libro por ISBN.", e);
        }
    }

    @Override
    public List<Libro> listar() {
        List<Libro> libros = new ArrayList<>();
        
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(LISTAR);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                libros.add(mapearLibro(rs));
            }
            
            return libros;
        } 
        catch (SQLException e) {
            throw new DatosException("Error al listar los libros.", e);
        }
    }

    @Override
    public void actualizar(Libro libro) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(ACTUALIZAR)) {
            
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setInt(3, libro.getAnio());
            ps.setString(4, libro.getIsbn());
            
            int filas = ps.executeUpdate();
            
            if (filas == 0) {
                throw new DatosException("No se actualizó el libro porque no existe.");
            }
        } 
        catch (SQLException e) {
            throw new DatosException("Error al actualizar el libro.", e);
        }
    }
    
    private Libro mapearLibro(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getString("isbn"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getInt("anio"));
    }
}
