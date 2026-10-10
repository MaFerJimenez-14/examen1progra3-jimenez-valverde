package cr.ac.una.exception;

/**
 * Datos de entrada inválidos (campos vacíos, formato de correo, año fuera de rango, etc.).
 */

public class ValidacionException extends BibliotecaException{

    public ValidacionException(String mensaje){
        super(mensaje);
    }
}
