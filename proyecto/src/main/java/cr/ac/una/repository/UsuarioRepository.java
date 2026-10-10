package cr.ac.una.repository;

import cr.ac.una.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla usuario.
 * Las implementaciones JDBC traducen SQLException a DatosException.
 */
public interface UsuarioRepository {

    void insertar(Usuario usuario);

    Optional<Usuario> buscarPorId(String identificacion);

    Optional<Usuario> buscarPorCorreo(String correo);

    List<Usuario> listar();

    void actualizar(Usuario usuario);
}
