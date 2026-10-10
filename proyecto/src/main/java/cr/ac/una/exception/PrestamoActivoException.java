package cr.ac.una.exception;

/**
 * Solo se puede devolver un préstamo ACTIVO.
 */

public class PrestamoActivoException extends ReglaNegocioException{
    
    public PrestamoActivoException(String mensaje){
        super(mensaje);
    }
    
}
