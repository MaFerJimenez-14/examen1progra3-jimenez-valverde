package cr.ac.una.exception;

/**
 * RN-05: Un usuario inactivo no puede solicitar préstamos.
 */

public class UsuarioInactivoException extends ReglaNegocioException {
    
    public UsuarioInactivoException(String mensaje){
        super(mensaje);
    }
    
}
