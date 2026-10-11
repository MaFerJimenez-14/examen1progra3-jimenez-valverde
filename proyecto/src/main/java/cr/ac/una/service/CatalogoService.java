package cr.ac.una.service;

import cr.ac.una.exception.EntidadNoEncontradaException;
import cr.ac.una.exception.ValidacionException;
import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;
import cr.ac.una.model.Libro;
import cr.ac.una.repository.EjemplarRepository;
import cr.ac.una.repository.LibroRepository;

import java.util.List;
import java.util.Objects;

/*
 * Casos de uso del catálogo: libros y sus ejemplares físicos.
 */

public class CatalogoService {
    
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    
    public CatalogoService(LibroRepository libroRepository, EjemplarRepository ejemplarRepository){
        this.libroRepository = Objects.requireNonNull(
                libroRepository, "El repositorio de libros es obligatorio.");

        this.ejemplarRepository = Objects.requireNonNull(
                ejemplarRepository, "El repositorio de ejemplares es obligatorio.");
    }
    
    /* RF-05: Lista todos los libros. */
    public List<Libro> listarLibros(){
        return libroRepository.listar();
    }
    
    /* RF-04: Valida ISBN, título, autor y año; el ISBN no debe existir. */
    public void registrarLibro(Libro libro){
        validarLibro(libro);

        String isbn = libro.getIsbn();

        if (libroRepository.buscarPorIsbn(isbn).isPresent()) {
            throw new ValidacionException("Ya existe un libro registrado con ese ISBN.");
        }
        libroRepository.insertar(libro);
    }
    
    /* RF-05: Lanza EntidadNoEncontradaException si no existe. */
    public Libro buscarLibro(String isbn){
        validarTexto(isbn, "El ISBN");

        String isbnNormalizado = isbn.trim();

        return libroRepository.buscarPorIsbn(isbnNormalizado)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe un libro con ISBN: " + isbnNormalizado));
    }
    
    /* RF-04: El libro debe existir; el ejemplar se registra como DISPONIBLE. */
    public void registrarEjemplar(String codigo, String isbn) {
        validarTexto(codigo, "El código del ejemplar");
        validarTexto(isbn, "El ISBN del libro");

        String codigoNormalizado = codigo.trim();
        String isbnNormalizado = isbn.trim();

        Libro libro = buscarLibro(isbnNormalizado);

        if (ejemplarRepository.buscarPorCodigo(codigoNormalizado).isPresent()) {
            throw new ValidacionException("Ya existe un ejemplar con ese código.");
        }

        Ejemplar ejemplar = new Ejemplar(
                codigoNormalizado, libro, EstadoEjemplar.DISPONIBLE);

        ejemplarRepository.insertar(ejemplar);
    }

    /* RF-05: Ejemplares de un libro con su estado. */
    public List<Ejemplar> listarEjemplares(String isbn) {
        Libro libro = buscarLibro(isbn);
        return ejemplarRepository.listarPorLibro(libro.getIsbn());
    }

    /* RF-05: Cantidad de ejemplares DISPONIBLES del libro. */
    public int consultarDisponibilidad(String isbn) {
        Libro libro = buscarLibro(isbn);
        return ejemplarRepository.contarDisponibles(libro.getIsbn());
    }

    /* RF-06: Cambia el estado de un ejemplar (por ejemplo, a DANADO).
     * No permite cambiar manualmente un ejemplar PRESTADO: eso solo ocurre por devolución. */
    public void cambiarEstadoEjemplar(String codigo, EstadoEjemplar nuevoEstado) {
        validarTexto(codigo, "El código del ejemplar");

        if (nuevoEstado == null) {
            throw new ValidacionException("Debe indicar el nuevo estado del ejemplar.");
        }

        if (nuevoEstado != EstadoEjemplar.DISPONIBLE && nuevoEstado != EstadoEjemplar.DANADO) {
            throw new ValidacionException("El cambio manual solo permite los estados "
                    + "DISPONIBLE o DANADO.");
        }

        String codigoNormalizado = codigo.trim();

        Ejemplar ejemplar = ejemplarRepository.buscarPorCodigo(codigoNormalizado)
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe un ejemplar con código: " + codigoNormalizado));

        if (ejemplar.getEstado() == EstadoEjemplar.PRESTADO) {
            throw new ValidacionException("No se puede cambiar manualmente el estado "
                    + "de un ejemplar prestado. Debe registrarse " + "su devolución.");
        }

        if (ejemplar.getEstado() == nuevoEstado) {
            throw new ValidacionException("El ejemplar ya tiene el estado indicado.");
        }

        ejemplarRepository.actualizarEstado(codigoNormalizado, nuevoEstado);
    }
    
    /* Valida los datos obligatorios de un libro. */
    private void validarLibro(Libro libro) {
        if (libro == null) {
            throw new ValidacionException("Los datos del libro son obligatorios.");
        }

        validarTexto(libro.getIsbn(), "El ISBN");
        validarTexto(libro.getTitulo(), "El título");
        validarTexto(libro.getAutor(), "El autor");

        if (libro.getAnio() < 1450 || libro.getAnio() > 2100) {
            throw new ValidacionException("El año del libro debe estar entre 1450 y 2100.");
        }

        libro.setIsbn(libro.getIsbn().trim());
        libro.setTitulo(libro.getTitulo().trim());
        libro.setAutor(libro.getAutor().trim());
    }

    /* Valida que una cadena no sea nula ni esté vacía. */
    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException(campo + " es obligatorio.");
        }
    }
    
}
