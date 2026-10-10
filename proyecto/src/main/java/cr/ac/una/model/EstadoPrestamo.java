package cr.ac.una.model;

/**
 * Estado de un préstamo. "Atrasado" no es un estado guardado:
 * se deriva de ACTIVO + fechaLimite anterior a hoy (ver Prestamo#estaAtrasado).
 */
public enum EstadoPrestamo {
    ACTIVO,
    DEVUELTO
}
