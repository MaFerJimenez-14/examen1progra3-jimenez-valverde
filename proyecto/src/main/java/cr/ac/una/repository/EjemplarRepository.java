package cr.ac.una.repository;

import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla ejemplar.
 *
 * Los métodos que reciben Connection participan en una transacción abierta
 * por la capa service (préstamo y devolución): no hacen commit ni cierran la conexión.
 */
public interface EjemplarRepository {

    void insertar(Ejemplar ejemplar);

    Optional<Ejemplar> buscarPorCodigo(String codigo);

    List<Ejemplar> listarPorLibro(String isbn);

    /** Cantidad de ejemplares DISPONIBLES de un libro (consulta de disponibilidad). */
    int contarDisponibles(String isbn);

    /** Cambio de estado fuera de una transacción (por ejemplo, marcar como DANADO). */
    void actualizarEstado(String codigo, EstadoEjemplar nuevoEstado);

    /**
     * Cambio de estado condicionado, dentro de una transacción:
     * UPDATE ejemplar SET estado = nuevo WHERE codigo = ? AND estado = esperado.
     *
     * @return true si se actualizó exactamente una fila; false si el ejemplar
     *         ya no estaba en el estado esperado (por ejemplo, otro bibliotecario lo prestó).
     */
    boolean actualizarEstado(Connection conexion, String codigo,
                             EstadoEjemplar estadoEsperado, EstadoEjemplar nuevoEstado);
}
