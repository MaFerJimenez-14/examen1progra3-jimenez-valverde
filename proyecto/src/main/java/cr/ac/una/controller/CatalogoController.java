package cr.ac.una.controller;

import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;
import cr.ac.una.model.Libro;
import cr.ac.una.service.CatalogoService;

import java.util.List;

/**
 * Punto de entrada desde la consola para libros y ejemplares.
 */

public class CatalogoController {
    
    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    public String registrarLibro(String isbn, String titulo, String autor, int anio) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public String buscarLibro(String isbn) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public List<Libro> listarLibros() {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public String registrarEjemplar(String codigo, String isbn) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public List<Ejemplar> listarEjemplares(String isbn) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public String consultarDisponibilidad(String isbn) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public String cambiarEstadoEjemplar(String codigo, EstadoEjemplar nuevoEstado) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
}
