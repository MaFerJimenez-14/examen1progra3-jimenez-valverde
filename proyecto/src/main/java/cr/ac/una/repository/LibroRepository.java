package cr.ac.una.repository;

import cr.ac.una.model.Libro;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla libro (catálogo de títulos).
 */
public interface LibroRepository {

    void insertar(Libro libro);

    Optional<Libro> buscarPorIsbn(String isbn);

    List<Libro> listar();

    void actualizar(Libro libro);
}
