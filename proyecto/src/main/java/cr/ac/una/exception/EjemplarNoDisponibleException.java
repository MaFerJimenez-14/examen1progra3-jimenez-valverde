package cr.ac.una.exception;

/**
 * El ejemplar no está DISPONIBLE.
 */

public class EjemplarNoDisponibleException extends ReglaNegocioException {
    
    public EjemplarNoDisponibleException(String mensaje){
        super(mensaje);
    }
    
}
