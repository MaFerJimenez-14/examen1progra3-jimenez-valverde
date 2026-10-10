package cr.ac.una.exception;

/**
 * Base de las violaciones de reglas de negocio
 */

public class ReglaNegocioException extends BibliotecaException {
    
    public ReglaNegocioException(String mensaje){
        super(mensaje);
    }
    
}
