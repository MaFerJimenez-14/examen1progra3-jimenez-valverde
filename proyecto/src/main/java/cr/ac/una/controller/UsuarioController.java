package cr.ac.una.controller;

import cr.ac.una.exception.BibliotecaException;
import cr.ac.una.model.Usuario;
import cr.ac.una.service.UsuarioService;

import java.util.List;
import java.util.Objects;

/*
 * Punto de entrada desde la consola para las opciones de usuarios.
 * No contiene reglas de negocio: recibe datos, delega al servicio y
 * traduce excepciones a mensajes para el bibliotecario.
 */

public class UsuarioController {
    
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = Objects.requireNonNull(
                usuarioService,"El servicio de usuarios es obligatorio.");
    }

    public String registrar(String identificacion, String nombre, String correo) {
        try {
            Usuario usuario = new Usuario(identificacion, nombre, correo, true);

            usuarioService.registrar(usuario);

            return "Usuario registrado correctamente: " + usuario.getNombre();
        } 
        catch (BibliotecaException e) {
            return "No se pudo registrar el usuario: " + e.getMessage();
        }
    }

    public String buscar(String identificacion) {
        try {
            Usuario usuario = usuarioService.buscarPorId(identificacion);
            return usuario.toString();
        } 
        catch (BibliotecaException e) {
            return "No se pudo consultar el usuario: " + e.getMessage();
        }
    }

    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    public String actualizar(String identificacion, String nombre, String correo, boolean activo) {
        try {
            // Recupera el usuario para comprobar que existe.
            Usuario usuario = usuarioService.buscarPorId(identificacion);

            usuario.setNombre(nombre);
            usuario.setCorreo(correo);
            usuario.setActivo(activo);

            usuarioService.actualizar(usuario);

            return "Usuario actualizado correctamente: " + usuario.getIdentificacion();

        } catch (BibliotecaException e) {
            return "No se pudo actualizar el usuario: " + e.getMessage();
        }
    }
    
}
