package cr.ac.una.model;

import java.util.Objects;

/**
 * Lector registrado en la biblioteca.
 */
public class Usuario {

    private String identificacion;
    private String nombre;
    private String correo;
    private boolean activo;

    public Usuario() {
    }

    public Usuario(String identificacion, String nombre, String correo, boolean activo) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.correo = correo;
        this.activo = activo;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario other)) return false;
        return Objects.equals(identificacion, other.identificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacion);
    }

    @Override
    public String toString() {
        return "Usuario{identificacion='" + identificacion + "', nombre='" + nombre
                + "', correo='" + correo + "', activo=" + activo + "}";
    }
}
