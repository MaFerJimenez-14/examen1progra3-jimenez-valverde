package cr.ac.una.service;

import java.math.BigDecimal;

/* Parámetros de las reglas de préstamo.
 * Si se cambia (por ejemplo, otro plazo, límite o monto de multa), solo se modifica aquí.
 *
 * diasPrestamo         días hasta la fecha límite (hoy + diasPrestamo)
 * maxPrestamosActivos  máximo de préstamos ACTIVOS por usuario
 * multaPorDia          monto en colones por cada día de atraso (RN-N1) */

public record PoliticaPrestamo(int diasPrestamo, int maxPrestamosActivos, BigDecimal multaPorDia) {
    
    public static final PoliticaPrestamo POR_DEFECTO =
            new PoliticaPrestamo(8, 3, new BigDecimal("200.00"));
    
    public PoliticaPrestamo {
        if(diasPrestamo <= 0 || maxPrestamosActivos <= 0)
            throw new IllegalArgumentException("Los parámetros de la política deben ser positivos");
        if(multaPorDia == null || multaPorDia.signum() < 0)
            throw new IllegalArgumentException("La multa por día no puede ser negativa");
    }

    /** RN-N1: multa = días de atraso × multa por día (0 si no hay atraso). */
    public BigDecimal calcularMulta(long diasAtraso) {
        if (diasAtraso <= 0) {
            return BigDecimal.ZERO;
        }
        return multaPorDia.multiply(BigDecimal.valueOf(diasAtraso));
    }
}
