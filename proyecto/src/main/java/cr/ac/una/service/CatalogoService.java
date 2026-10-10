package cr.ac.una.service;

import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;
import cr.ac.una.model.Libro;
import cr.ac.una.repository.EjemplarRepository;
import cr.ac.una.repository.LibroRepository;

import java.util.List;

/*
 * Casos de uso del catálogo: libros y sus ejemplares físicos.
 */

public class CatalogoService {
    
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    
    public CatalogoService(LibroRepository libroRepository, EjemplarRepository ejemplarRepository){
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
    }
    
    public List<Libro> listarLibros(){
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
    /* Valida ISBN, título, autor y año; el ISBN no debe existir. */
    public void registrarLibro(Libro libro){
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
    /* Lanza EntidadNoEncontradaException si no existe. */
    public Libro buscarLibro(String isbn){
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
    /* El libro debe existir; el ejemplar se registra como DISPONIBLE. */
    public void registrarEjemplar(String codigo, String isbn) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    /* Ejemplares de un libro con su estado. */
    public List<Ejemplar> listarEjemplares(String isbn) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    /* Cantidad de ejemplares DISPONIBLES del libro. */
    public int consultarDisponibilidad(String isbn) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    /* Cambia el estado de un ejemplar (por ejemplo, a DANADO).
     * No permite cambiar manualmente un ejemplar PRESTADO: eso solo ocurre por devolución. */
    public void cambiarEstadoEjemplar(String codigo, EstadoEjemplar nuevoEstado) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
}
