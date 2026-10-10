package cr.ac.una.exception;

/**
 * El usuario ya alcanzó el máximo de préstamos activos.
 */

public class LimitePrestamosException extends ReglaNegocioException {
    
    public LimitePrestamosException(String mensaje){
        super(mensaje);
    }
    
}
