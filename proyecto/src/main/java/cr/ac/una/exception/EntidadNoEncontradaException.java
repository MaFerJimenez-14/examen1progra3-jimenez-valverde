package cr.ac.una.exception;

/**
 * Se buscó un usuario, libro, ejemplar o préstamo que no existe.
 */

public class EntidadNoEncontradaException extends BibliotecaException {
    
    public EntidadNoEncontradaException(String mensaje){
        super(mensaje);
    }
    
}
