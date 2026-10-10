package cr.ac.una.controller;

import cr.ac.una.model.Usuario;
import cr.ac.una.service.UsuarioService;

import java.util.List;

/*
 * Punto de entrada desde la consola para las opciones de usuarios.
 * No contiene reglas de negocio: recibe datos, delega al servicio y
 * traduce excepciones a mensajes para el bibliotecario.
 */

public class UsuarioController {
    
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public String registrar(String identificacion, String nombre, String correo) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public String buscar(String identificacion) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public List<Usuario> listar() {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public String actualizar(String identificacion, String nombre, String correo, boolean activo) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
}
