package cr.ac.una.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Préstamo de un ejemplar a un usuario.
 */
public class Prestamo {

    private int id;
    private Usuario usuario;
    private Ejemplar ejemplar;
    private LocalDate fechaPrestamo;
    private LocalDate fechaLimite;
    private LocalDate fechaDevolucion; // null mientras el préstamo esté ACTIVO
    private EstadoPrestamo estado;
    private BigDecimal multa = BigDecimal.ZERO; // monto generado al devolver con atraso (RN-N1)
    private boolean multaPagada;                 // true cuando se registra el pago (RN-N3)

    public Prestamo() {
    }

    public Prestamo(int id, Usuario usuario, Ejemplar ejemplar, LocalDate fechaPrestamo,
                    LocalDate fechaLimite, LocalDate fechaDevolucion, EstadoPrestamo estado) {
        this.id = id;
        this.usuario = usuario;
        this.ejemplar = ejemplar;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaLimite = fechaLimite;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
    }

    /** Constructor completo, incluida la multa (usado al leer desde la base de datos). */
    public Prestamo(int id, Usuario usuario, Ejemplar ejemplar, LocalDate fechaPrestamo,
                    LocalDate fechaLimite, LocalDate fechaDevolucion, EstadoPrestamo estado,
                    BigDecimal multa, boolean multaPagada) {
        this(id, usuario, ejemplar, fechaPrestamo, fechaLimite, fechaDevolucion, estado);
        setMulta(multa);
        this.multaPagada = multaPagada;
    }

    /** Constructor para un préstamo nuevo (el id lo asigna la base de datos). */
    public Prestamo(Usuario usuario, Ejemplar ejemplar, LocalDate fechaPrestamo, LocalDate fechaLimite) {
        this(0, usuario, ejemplar, fechaPrestamo, fechaLimite, null, EstadoPrestamo.ACTIVO);
    }

    /** Un préstamo está atrasado si sigue ACTIVO y la fecha límite ya pasó. */
    public boolean estaAtrasado(LocalDate hoy) {
        return estado == EstadoPrestamo.ACTIVO && hoy.isAfter(fechaLimite);
    }

    /** Días de atraso respecto a una fecha (0 si no hay atraso). */
    public long diasAtraso(LocalDate fecha) {
        long dias = ChronoUnit.DAYS.between(fechaLimite, fecha);
        return Math.max(dias, 0);
    }

    /** Hay multa pendiente si el monto es mayor que cero y todavía no se pagó (RN-N2). */
    public boolean tieneMultaPendiente() {
        return multa.signum() > 0 && !multaPagada;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Ejemplar getEjemplar() {
        return ejemplar;
    }

    public void setEjemplar(Ejemplar ejemplar) {
        this.ejemplar = ejemplar;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    /** Un valor nulo se toma como 0, igual que el DEFAULT de la columna. */
    public void setMulta(BigDecimal multa) {
        this.multa = multa == null ? BigDecimal.ZERO : multa;
    }

    public boolean isMultaPagada() {
        return multaPagada;
    }

    public void setMultaPagada(boolean multaPagada) {
        this.multaPagada = multaPagada;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prestamo other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Prestamo{id=" + id
                + ", usuario=" + (usuario != null ? usuario.getIdentificacion() : "null")
                + ", ejemplar=" + (ejemplar != null ? ejemplar.getCodigo() : "null")
                + ", fechaPrestamo=" + fechaPrestamo
                + ", fechaLimite=" + fechaLimite
                + ", fechaDevolucion=" + fechaDevolucion
                + ", estado=" + estado
                + ", multa=" + multa
                + ", multaPagada=" + multaPagada + "}";
    }
}
