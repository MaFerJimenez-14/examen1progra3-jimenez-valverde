package cr.ac.una.service;

/* Parámetros de las reglas de préstamo.
 * Si se cambia (por ejemplo, otro plazo o límite), solo se modifica aquí.
 *
 * diasPrestamo         días hasta la fecha límite (hoy + diasPrestamo)
 * maxPrestamosActivos  máximo de préstamos ACTIVOS por usuario */

public record PoliticaPrestamo(int diasPrestamo, int maxPrestamosActivos) {
    
    public static final PoliticaPrestamo POR_DEFECTO = new PoliticaPrestamo(8, 3);
    
    public PoliticaPrestamo {
        if(diasPrestamo <= 0 || maxPrestamosActivos <= 0)
            throw new IllegalArgumentException("Los parámetros de la política deben ser positivos");
    }
    
}
