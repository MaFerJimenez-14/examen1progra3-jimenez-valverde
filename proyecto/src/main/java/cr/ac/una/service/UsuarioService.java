package cr.ac.una.service;

import cr.ac.una.model.Usuario;
import cr.ac.una.repository.UsuarioRepository;

import java.util.List;

/**
 * Casos de uso de usuarios: validaciones y reglas antes de llegar a la base de datos.
 */

public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    
    public UsuarioService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }
    /* RF-02 */ 
    public List<Usuario> listar(){
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
    /* RF-01: Valida campos obligatorios y formato de correo (ValidacionException),
     * y que la identificación y el correo no estén registrados. */
    public void registrar(Usuario usuario){
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
    /* RF-02: Lanza EntidadNoEncontradaException si no existe. */
    public Usuario buscarPorId(String identificacion){
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
    /* RF-03: Actualiza nombre, correo y estado activo. Valida que exista. */
    public void actualizar(Usuario usuario){
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
}
