package cr.ac.una.controller;

import cr.ac.una.exception.BibliotecaException;
import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;
import cr.ac.una.model.Libro;
import cr.ac.una.service.CatalogoService;

import java.util.List;
import java.util.Objects;

/**
 * Punto de entrada desde la consola para libros y ejemplares.
 */

public class CatalogoController {
    
    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = Objects.requireNonNull(
                catalogoService, "El servicio del catálogo es obligatorio.");
    }

    public String registrarLibro(String isbn, String titulo, String autor, int anio) {
        try {
            Libro libro = new Libro(isbn, titulo, autor, anio);
            catalogoService.registrarLibro(libro);

            return "Libro registrado correctamente: " + libro.getTitulo();
        } 
        catch (BibliotecaException e) {
            return "No se pudo registrar el libro: " + e.getMessage();
        }
    }

    public String buscarLibro(String isbn) {
        try {
            return catalogoService.buscarLibro(isbn).toString();
        } 
        catch (BibliotecaException e) {
            return "No se pudo consultar el libro: " + e.getMessage();
        }
    }

    public List<Libro> listarLibros() {
        return catalogoService.listarLibros();
    }

    public String registrarEjemplar(String codigo, String isbn) {
        try {
            catalogoService.registrarEjemplar(codigo, isbn);

            return "Ejemplar registrado correctamente: " + codigo;
        } 
        catch (BibliotecaException e) {
            return "No se pudo registrar el ejemplar: " + e.getMessage();
        }
    }

    public List<Ejemplar> listarEjemplares(String isbn) {
        return catalogoService.listarEjemplares(isbn);
    }

    public String consultarDisponibilidad(String isbn) {
        try {
            int cantidad = catalogoService.consultarDisponibilidad(isbn);

            return "Ejemplares disponibles para el ISBN " + isbn + ": " + cantidad;
        } 
        catch (BibliotecaException e) {
            return "No se pudo consultar la disponibilidad: " + e.getMessage();
        }
    }

    public String cambiarEstadoEjemplar(String codigo, EstadoEjemplar nuevoEstado) {
        try {
            catalogoService.cambiarEstadoEjemplar(codigo, nuevoEstado);

            return "Estado del ejemplar " + codigo + " actualizado a " + nuevoEstado + ".";
        } 
        catch (BibliotecaException e) {
            return "No se pudo cambiar el estado: " + e.getMessage();
        }
    }
    
}
