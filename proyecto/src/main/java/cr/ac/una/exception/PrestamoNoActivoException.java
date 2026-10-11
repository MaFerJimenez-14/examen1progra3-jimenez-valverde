package cr.ac.una.exception;

/**
 * RN-04: Solo se puede devolver un préstamo ACTIVO.
 */

public class PrestamoNoActivoException extends ReglaNegocioException{
    
    public PrestamoNoActivoException(String mensaje){
        super(mensaje);
    }
    
}
