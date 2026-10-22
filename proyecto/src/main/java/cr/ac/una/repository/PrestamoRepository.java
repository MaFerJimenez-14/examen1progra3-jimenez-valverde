package cr.ac.una.repository;

import cr.ac.una.model.Prestamo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla prestamo.
 *
 * Los métodos que reciben Connection participan en una transacción abierta
 * por la capa service: no hacen commit ni cierran la conexión.
 */
public interface PrestamoRepository {

    /* Inserta el préstamo y devuelve el id generado (AUTO_INCREMENT). */
    int insertar(Connection conexion, Prestamo prestamo);

    /* Busca el préstamo con su usuario y su ejemplar/libro (JOIN). */
    Optional<Prestamo> buscarPorId(int id);

    /* Registra fecha_devolucion, la multa calculada (RN-N1) y cambia el estado a DEVUELTO. */
    void registrarDevolucion(Connection conexion, int id, LocalDate fechaDevolucion, BigDecimal multa);

    /* Préstamos ACTIVOS de un usuario, con datos del libro (JOIN). */
    List<Prestamo> listarActivosPorUsuario(String identificacionUsuario);

    /* Préstamos ACTIVOS con fecha_limite anterior a hoy, con usuario y libro (JOIN). */
    List<Prestamo> listarAtrasados(LocalDate hoy);

    /* Cantidad de préstamos ACTIVOS de un usuario (RN-03). */
    int contarActivosPorUsuario(String identificacionUsuario);

    /* true si el usuario tiene al menos un préstamo ACTIVO vencido (RN-02). */
    boolean tieneAtrasados(String identificacionUsuario, LocalDate hoy);
    
    /* true si el usuario tiene multas pendientes de pago (RN-N2) */
    boolean tieneMultasPendientes(String identificacionUsuario);
    
    /* Lista los préstamos que tienen multas pendientes de pago */
    List<Prestamo> listarMultasPendientes();
    
    /* Marca como pagada la multa de un préstamo */
    void registrarPagoMulta(Connection conexion, int idPrestamo);
}
