package cr.ac.una.exception;

/**
 * Excepción base de la aplicación. Es unchecked para no contaminar las firmas de las capas superiores.
 */

public class BibliotecaException extends RuntimeException {
    
    public BibliotecaException(String mensaje){
        super(mensaje);
    }
    
    public BibliotecaException(String mensaje, Throwable causa){
        super(mensaje, causa);
    }
}