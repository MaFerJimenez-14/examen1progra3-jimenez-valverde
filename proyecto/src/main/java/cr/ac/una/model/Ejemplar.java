package cr.ac.una.model;

import java.util.Objects;

/**
 * Copia física de un libro.
 */
public class Ejemplar {

    private String codigo;
    private Libro libro;
    private EstadoEjemplar estado;

    public Ejemplar() {
    }

    public Ejemplar(String codigo, Libro libro, EstadoEjemplar estado) {
        this.codigo = codigo;
        this.libro = libro;
        this.estado = estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public EstadoEjemplar getEstado() {
        return estado;
    }

    public void setEstado(EstadoEjemplar estado) {
        this.estado = estado;
    }

    public boolean estaDisponible() {
        return estado == EstadoEjemplar.DISPONIBLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ejemplar other)) return false;
        return Objects.equals(codigo, other.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "Ejemplar{codigo='" + codigo + "', libro="
                + (libro != null ? libro.getTitulo() : "null") + ", estado=" + estado + "}";
    }
}
