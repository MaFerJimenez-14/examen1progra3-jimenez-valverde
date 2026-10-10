package cr.ac.una.exception;

/**
 * Envuelve una SQLException para que los detalles de JDBC no salgan de la capa repository.
 */

public class DatosException extends BibliotecaException {
    
    public DatosException(String mensaje){
        super(mensaje);
    }
    
    public DatosException(String mensaje, Throwable causa){
        super(mensaje, causa);
    }
}
