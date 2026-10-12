package cr.ac.una.repository.jdbc;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.exception.DatosException;
import cr.ac.una.model.Usuario;
import cr.ac.una.repository.UsuarioRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

public class UsuarioRepositoryJDBC implements UsuarioRepository {
    
    private static final String INSERTAR = "INSERT INTO usuario (identificacion, nombre, correo, activo) " + "VALUES (?, ?, ?, ?)";
    
    private static final String BUSCAR_POR_ID = "SELECT identificacion, nombre, correo, activo " + "FROM usuario WHERE identificacion = ?";
    
    private static final String BUSCAR_POR_CORREO = "SELECT identificacion, nombre, correo, activo " + "FROM usuario WHERE correo = ?";
    
    private static final String LISTAR = "SELECT identificacion, nombre, correo, activo " + "FROM usuario ORDER BY nombre";
    
    private static final String ACTUALIZAR = "UPDATE usuario SET nombre = ?, correo = ?, activo = ? " + "WHERE identificacion = ?";
    
    @Override
    public void insertar(Usuario usuario) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(INSERTAR)) {
            
            ps.setString(1, usuario.getIdentificacion());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getCorreo());
            ps.setBoolean(4, usuario.isActivo());
            
            ps.executeUpdate();
        } 
        catch (SQLException e) {
            throw new DatosException("Error al registrar el usuario.", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(String identificacion) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(BUSCAR_POR_ID)) {
            
            ps.setString(1, identificacion);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
            return Optional.empty();
        } 
        catch (SQLException e) {
            throw new DatosException("Error al buscar el usuario por identificación.", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(BUSCAR_POR_CORREO)) {
            
            ps.setString(1, correo);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
            return Optional.empty();
        } 
        catch (SQLException e) {
            throw new DatosException("Error al buscar el usuario por correo.", e);
        }
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(LISTAR);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
            return usuarios;
        } 
        catch (SQLException e) {
            throw new DatosException("Error al listar los usuarios.", e);
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(ACTUALIZAR)) {
            
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo());
            ps.setBoolean(3, usuario.isActivo());
            ps.setString(4, usuario.getIdentificacion());
            
            int filas = ps.executeUpdate();
            
            if (filas == 0) {
                throw new DatosException("No se actualizó el usuario porque no existe.");
            }
        } 
        catch (SQLException e) {
            throw new DatosException("Error al actualizar el usuario.", e);
        }
    }
    
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getString("identificacion"), 
                rs.getString("nombre"),
                rs.getString("correo"), 
                rs.getBoolean("activo"));
    }
}
