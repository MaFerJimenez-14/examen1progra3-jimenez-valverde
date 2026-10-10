package cr.ac.una.exception;

/**
 * RN-01: El ejemplar no está DISPONIBLE.
 */

public class EjemplarNoDisponibleException extends ReglaNegocioException {
    
    public EjemplarNoDisponibleException(String mensaje){
        super(mensaje);
    }
    
}
