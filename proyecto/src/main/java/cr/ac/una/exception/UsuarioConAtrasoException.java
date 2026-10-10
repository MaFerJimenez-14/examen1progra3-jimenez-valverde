package cr.ac.una.exception;

/**
 * El usuario tiene préstamos atrasados.
 */

public class UsuarioConAtrasoException extends ReglaNegocioException {
    
    public UsuarioConAtrasoException(String mensaje){
        super(mensaje);
    }
    
}
