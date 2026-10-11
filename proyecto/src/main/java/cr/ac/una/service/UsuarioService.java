package cr.ac.una.service;

import cr.ac.una.exception.EntidadNoEncontradaException;
import cr.ac.una.exception.ValidacionException;
import cr.ac.una.model.Usuario;
import cr.ac.una.repository.UsuarioRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Casos de uso de usuarios: validaciones y reglas antes de llegar a la base de datos.
 */

public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    
    public UsuarioService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = Objects.requireNonNull(
                usuarioRepository, "El repositorio de usuarios es obligatorio");
    }
    /* RF-02: Lista todos los usuarios registrados. */ 
    public List<Usuario> listar(){
        return usuarioRepository.listar();
    }
    
    /* RF-01: Valida campos obligatorios y formato de correo (ValidacionException),
     * y que la identificación y el correo no estén registrados. */
    public void registrar(Usuario usuario){
        validarUsuario(usuario);

        String identificacion = usuario.getIdentificacion();
        String correo = usuario.getCorreo();

        if (usuarioRepository.buscarPorId(identificacion).isPresent()) {
            throw new ValidacionException("Ya existe un usuario con esa identificación.");
        }

        if (usuarioRepository.buscarPorCorreo(correo).isPresent()) {
            throw new ValidacionException("Ya existe un usuario registrado con ese correo.");
        }

        usuario.setActivo(true);
        usuarioRepository.insertar(usuario);
    }
    
    /* RF-02: Lanza EntidadNoEncontradaException si no existe. */
    public Usuario buscarPorId(String identificacion){
        validarIdentificacion(identificacion);

        return usuarioRepository.buscarPorId(identificacion.trim())
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe un usuario con identificación: " 
                                + identificacion.trim()));
    }
    
    /* RF-03: Actualiza nombre, correo y estado activo. Valida que exista. */
    public void actualizar(Usuario usuario){
        validarUsuario(usuario);

        String identificacion = usuario.getIdentificacion();
        String correo = usuario.getCorreo();

        // Confirma que el usuario que se desea actualizar exista.
        Optional<Usuario> existente = usuarioRepository.buscarPorId(identificacion);

        if (existente.isEmpty()) {
            throw new EntidadNoEncontradaException(
                    "No existe un usuario con identificación: " + identificacion);
        }

        // Evita utilizar el correo de otro usuario.
        Optional<Usuario> usuarioConCorreo = usuarioRepository.buscarPorCorreo(correo);

        if (usuarioConCorreo.isPresent() && 
                !usuarioConCorreo.get().getIdentificacion().equals(identificacion)) {
            throw new ValidacionException("Ese correo ya está registrado por otro usuario.");
        }
        usuarioRepository.actualizar(usuario);
    }
    
    /* Valida y normaliza los campos obligatorios del usuario. */
    private void validarUsuario(Usuario usuario){
        if(usuario == null){
            throw new ValidacionException("Los datos del usuario son obligatorios");
        }
        
        validarIdentificacion(usuario.getIdentificacion());

        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new ValidacionException("El nombre del usuario es obligatorio.");
        }

        String correo = usuario.getCorreo();

        if (correo == null || correo.isBlank()) {
            throw new ValidacionException("El correo electrónico es obligatorio.");
        }

        // Normaliza los valores para evitar espacios accidentales.
        usuario.setIdentificacion(usuario.getIdentificacion().trim());
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setCorreo(correo.trim().toLowerCase());

        // Comprueba el formato básico de un correo electrónico.
        String patronCorreo = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!usuario.getCorreo().matches(patronCorreo)) {
            throw new ValidacionException("El formato del correo electrónico no es válido.");
        }
    }
    
    /* Comprueba que la identificación sea obligatoria. */
    private void validarIdentificacion(String identificacion) {
        if (identificacion == null || identificacion.isBlank()) {
            throw new ValidacionException("La identificación del usuario es obligatoria.");
        }
    }
}

