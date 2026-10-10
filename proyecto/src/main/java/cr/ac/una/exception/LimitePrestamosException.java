package cr.ac.una.exception;

/**
 * RN-03: El usuario ya alcanzó el máximo de préstamos activos.
 */

public class LimitePrestamosException extends ReglaNegocioException {
    
    public LimitePrestamosException(String mensaje){
        super(mensaje);
    }
    
}
