package cr.ac.una.exception;

/**
 * RN-02: El usuario tiene préstamos atrasados.
 */

public class UsuarioConAtrasoException extends ReglaNegocioException {
    
    public UsuarioConAtrasoException(String mensaje){
        super(mensaje);
    }
    
}
