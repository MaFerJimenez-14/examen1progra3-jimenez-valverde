package cr.ac.una.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstracción para obtener conexiones JDBC.
 *
 * Los servicios que manejan transacciones dependen de esta interfaz y no de
 * ConexionBD directamente, para poder cambiar la fuente de conexiones (o
 * usar una de prueba) sin tocar la lógica de negocio.
 */

@FunctionalInterface
public interface ProveedorConexion {
    Connection obtenerConexion() throws SQLException;
}
