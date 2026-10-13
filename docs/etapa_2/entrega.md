![][image1]

Universidad Nacional de Costa Rica

Sede Regional Brunca – Campus Coto

Documentación Técnica Examen I \- EIF206

Programacion lll

Tema:  
implementación funcional y persistencia

Profesor:  
 Juan Gamboa Abarca

Estudiante:  
 Maria Jimenez Alpizar

2026

## **Cambios al diseño**

La arquitectura por capas de la Etapa 1 se mantuvo sin cambios de fondo: `App → Controller → Service → Repository (interface) → implementación JDBC → MySQL`. Los ajustes realizados durante la implementación fueron:

| Cambio | Motivo |
| :---- | :---- |
| Las implementaciones JDBC se ubicaron en el subpaquete `cr.ac.una.repository.jdbc` y se nombraron `UsuarioRepositoryJDBC`, `LibroRepositoryJDBC`, `EjemplarRepositoryJDBC` y `PrestamoRepositoryJDBC`. | Separar las interfaces (contrato) de su implementación concreta dentro del paquete `repository`. |
| Los repositorios obtienen la conexión con `ConexionBD.obtenerConexion()`, como indica la guía (sección 15). `ProveedorConexion` se usa solo en `PrestamoService`. | El servicio necesita abrir la conexión para controlar la transacción; los repositorios no manejan transacciones propias. |
| Se agregaron a los controladores los métodos `UsuarioController.buscarDetalle`, `CatalogoController.buscarDetalle` y `CatalogoController.consultarCantidadDisponible`. | Devuelven el objeto o el valor para que la consola los muestre con formato de tabla o ficha, en lugar de un texto plano. |
| `App` arma todas las dependencias en `configurarAplicacion()`: crea los repositorios JDBC y los inyecta por constructor en los servicios, y los servicios en los controladores. | Los servicios dependen de las interfaces, no de las clases JDBC concretas. |
| Se agregó la clase `Consola` con utilidades de presentación (títulos, tablas, mensajes de éxito, error e información). | Reutilizar el formato de salida en todas las opciones del menú. |
| `database/schema.sql` incluye ahora los datos de prueba (`INSERT`). | Permitir demostrar el sistema desde una base nueva. |

El diagrama UML y el modelo relacional de la Etapa 1 siguen vigentes. La única diferencia con el UML es el nombre y el subpaquete de las implementaciones JDBC.

## **Configuración y ejecución**

text

1\. Crear la base de datos: ejecutar database/schema.sql en MySQL 8

   (por ejemplo, desde phpMyAdmin → pestaña SQL, o con:

    mysql \-u \<usuario\> \-p \< database/schema.sql).

   En un servidor compartido sin permiso de CREATE DATABASE, ejecutar el script

   desde la sección de tablas sobre la base asignada.

2\. Configurar la conexión:

   cd proyecto/src/main/resources

   cp db.properties.example db.properties

   Completar db.url, db.usuario y db.clave con los datos del servidor MySQL.

3\. Ejecutar la aplicación:

   cd proyecto

   mvn clean compile exec:java

   (o desde NetBeans: abrir la carpeta proyecto/ y ejecutar cr.ac.una.app.App)

4\. En el menú, la opción 1 (Probar conexión a MySQL) verifica la configuración.

## **Operaciones implementadas**

| Opción del menú | Operación | Tipo | Controller | Service | Repository |
| :---- | :---- | :---- | :---- | :---- | :---- |
| 2 | Listar usuarios | Consulta | `UsuarioController.listar` | `UsuarioService.listar` | `UsuarioRepository.listar` |
| 3 | Buscar usuario por identificación | Consulta | `UsuarioController.buscarDetalle` | `UsuarioService.buscarPorId` | `UsuarioRepository.buscarPorId` |
| 4 | Registrar usuario | Registro | `UsuarioController.registrar` | `UsuarioService.registrar` | `UsuarioRepository.insertar`, `buscarPorId`, `buscarPorCorreo` |
| 5 | Listar libros | Consulta | `CatalogoController.listarLibros` | `CatalogoService.listarLibros` | `LibroRepository.listar` |
| 6 | Buscar libro por ISBN | Consulta | `CatalogoController.buscarDetalle` | `CatalogoService.buscarLibro` | `LibroRepository.buscarPorIsbn` |
| 7 | Consultar ejemplares de un libro | Consulta (JOIN `ejemplar`–`libro`) | `CatalogoController.listarEjemplares` | `CatalogoService.listarEjemplares` | `EjemplarRepository.listarPorLibro` |
| 8 | Consultar disponibilidad de un libro | Consulta con filtro | `CatalogoController.consultarCantidadDisponible` | `CatalogoService.consultarDisponibilidad` | `EjemplarRepository.contarDisponibles` |
| 9 | Registrar préstamo | **Negocio (transacción)** | `PrestamoController.prestar` | `PrestamoService.prestar` | `UsuarioRepository.buscarPorId`, `PrestamoRepository.tieneAtrasados`, `contarActivosPorUsuario`, `insertar`, `EjemplarRepository.buscarPorCodigo`, `actualizarEstado` |
| 10 | Registrar devolución | **Negocio (transacción)** | `PrestamoController.devolver` | `PrestamoService.devolver` | `PrestamoRepository.buscarPorId`, `registrarDevolucion`, `EjemplarRepository.actualizarEstado` |
| 11 | Préstamos activos de un usuario | Consulta con JOIN (`prestamo`–`usuario`–`ejemplar`–`libro`) | `PrestamoController.listarActivosPorUsuario` | `PrestamoService.listarActivosPorUsuario` | `PrestamoRepository.listarActivosPorUsuario` |
| 12 | Reporte de préstamos atrasados | Consulta con JOIN y filtro por fecha | `PrestamoController.reporteAtrasados` | `PrestamoService.listarAtrasados` | `PrestamoRepository.listarAtrasados` |
| 13 | Registrar libro | Registro | `CatalogoController.registrarLibro` | `CatalogoService.registrarLibro` | `LibroRepository.insertar`, `buscarPorIsbn` |
| 14 | Registrar ejemplar | Registro | `CatalogoController.registrarEjemplar` | `CatalogoService.registrarEjemplar` | `EjemplarRepository.insertar`, `buscarPorCodigo` |
| 15 | Cambiar estado de un ejemplar | Actualización | `CatalogoController.cambiarEstadoEjemplar` | `CatalogoService.cambiarEstadoEjemplar` | `EjemplarRepository.actualizarEstado` |

## **Reglas de negocio implementadas**

| ID | Regla | Clase / método del Service | Excepción que se lanza |
| :---- | :---- | :---- | :---- |
| RN-01 | No se puede prestar un ejemplar que no esté `DISPONIBLE`. | `PrestamoService.prestar` (verificación previa y `UPDATE` condicionado dentro de la transacción) | `EjemplarNoDisponibleException` |
| RN-02 | Un usuario con préstamos atrasados no puede solicitar nuevos préstamos. | `PrestamoService.prestar` → `PrestamoRepository.tieneAtrasados` | `UsuarioConAtrasoException` |
| RN-03 | Un usuario no puede tener más de 3 préstamos activos. | `PrestamoService.prestar` → `PrestamoRepository.contarActivosPorUsuario` y `PoliticaPrestamo.maxPrestamosActivos` | `LimitePrestamosException` |
| RN-04 | Solo se puede devolver un préstamo `ACTIVO`. | `PrestamoService.devolver` | `PrestamoNoActivoException` |
| RN-05 | Un usuario inactivo no puede solicitar préstamos. | `PrestamoService.prestar` | `UsuarioInactivoException` |
| RN-06 | La fecha límite es la fecha del préstamo \+ 8 días. | `PrestamoService.prestar` con `PoliticaPrestamo.diasPrestamo` | — (la calcula el sistema) |
| RN-07 | Identificación y correo de usuario, ISBN de libro y código de ejemplar son únicos. | `UsuarioService.registrar` / `actualizar`, `CatalogoService.registrarLibro` / `registrarEjemplar` | `ValidacionException` |
| RN-08 | Un ejemplar `PRESTADO` solo vuelve a `DISPONIBLE` mediante una devolución. | `CatalogoService.cambiarEstadoEjemplar` | `ValidacionException` |
| RN-09 | Campos obligatorios, formato de correo y año del libro entre 1450 y 2100\. | `UsuarioService.validarUsuario`, `CatalogoService.validarLibro` | `ValidacionException` |

Además, cuando no existe el usuario, libro, ejemplar o préstamo indicado se lanza `EntidadNoEncontradaException`.

## **JDBC**

Todas las operaciones usan `PreparedStatement` con parámetros `?`, `try-with-resources` para `Connection`, `PreparedStatement` y `ResultSet`, y un método privado de mapeo por repositorio. Las `SQLException` se envuelven en `DatosException`.

Fragmento de `UsuarioRepositoryJDBC.buscarPorId`:

java

private static final String BUSCAR\_POR\_ID \= "SELECT identificacion, nombre, correo, activo "

        \+ "FROM usuario WHERE identificacion \= ?";

@Override

public Optional\<Usuario\> buscarPorId(String identificacion) {

    try (Connection conexion \= ConexionBD.obtenerConexion();

        PreparedStatement ps \= conexion.prepareStatement(BUSCAR\_POR\_ID)) {

        ps.setString(1, identificacion);

        try (ResultSet rs \= ps.executeQuery()) {

            if (rs.next()) {

                return Optional.of(mapearUsuario(rs));

            }

        }

        return Optional.empty();

    }

    catch (SQLException e) {

        throw new DatosException("Error al buscar el usuario por identificación.", e);

    }

}

Consulta con JOIN de `PrestamoRepositoryJDBC.listarAtrasados` (préstamos `ACTIVOS` con fecha límite vencida):

java

private static final String LISTAR\_ATRASADOS \= "SELECT p.id, p.fecha\_prestamo, p.fecha\_limite, "

        \+ "p.fecha\_devolucion, p.estado, "

        \+ "u.identificacion, u.nombre, u.correo, u.activo, "

        \+ "e.codigo, e.estado AS estado\_ejemplar, "

        \+ "l.isbn, l.titulo, l.autor, l.anio "

        \+ "FROM prestamo p "

        \+ "INNER JOIN usuario u ON p.usuario\_id \= u.identificacion "

        \+ "INNER JOIN ejemplar e ON p.ejemplar\_codigo \= e.codigo "

        \+ "INNER JOIN libro l ON e.libro\_isbn \= l.isbn "

        \+ "WHERE p.estado \= ? AND p.fecha\_limite \< ? "

        \+ "ORDER BY p.fecha\_limite";

**¿Alguna operación usa transacción? ¿Cuál y por qué?**

Sí, las dos partes de la operación de negocio principal:

* **Registrar préstamo** (`PrestamoService.prestar`): inserta una fila en `prestamo` y cambia el `ejemplar` de `DISPONIBLE` a `PRESTADO`.  
* **Registrar devolución** (`PrestamoService.devolver`): actualiza el `prestamo` a `DEVUELTO` con su `fecha_devolucion` y cambia el `ejemplar` de `PRESTADO` a `DISPONIBLE`.

Ambas modifican dos tablas, por lo que deben aplicarse completas o no aplicarse. Si quedara un préstamo registrado con el ejemplar todavía `DISPONIBLE`, otro usuario podría llevarse la misma copia; si el ejemplar quedará `PRESTADO` sin préstamo asociado, la copia se perdería del inventario.

El servicio es dueño de la transacción: abre la conexión, ejecuta `setAutoCommit(false)`, pasa **la misma conexión** a los métodos del repositorio que la reciben como parámetro y hace `commit()` si todo sale bien o `rollback()` ante cualquier error. Los repositorios no hacen `commit` ni cierran esa conexión.

El cambio de estado del ejemplar usa un `UPDATE` condicionado (`... WHERE codigo = ? AND estado = ?`). Si no afecta exactamente una fila, otro préstamo se adelantó, y la transacción se revierte con `EjemplarNoDisponibleException`.

## **Service**

Fragmento de la operación de negocio principal, `PrestamoService.prestar`:

java

public Prestamo prestar(String identificacionUsuario, String codigoEjemplar) {

    validarTexto(identificacionUsuario, "La identificación del usuario");

    validarTexto(codigoEjemplar, "El código del ejemplar");

    String identificacion \= identificacionUsuario.trim();

    String codigo \= codigoEjemplar.trim();

    LocalDate hoy \= LocalDate.now(reloj);

    Usuario usuario \= usuarioRepository.buscarPorId(identificacion)

            .orElseThrow(() \-\> new EntidadNoEncontradaException(

                    "No existe un usuario con identificación: " \+ identificacion));

    // RN-05: el usuario debe estar activo.

    if (\!usuario.isActivo()) {

        throw new UsuarioInactivoException(

                "El usuario está inactivo y no puede solicitar préstamos.");

    }

    // RN-02: no se permiten préstamos nuevos si hay atrasos.

    if (prestamoRepository.tieneAtrasados(identificacion, hoy)) {

        throw new UsuarioConAtrasoException(

                "El usuario tiene préstamos atrasados y no puede solicitar otro préstamo.");

    }

    // RN-03: respetar el máximo de préstamos activos.

    int activos \= prestamoRepository.contarActivosPorUsuario(identificacion);

    if (activos \>= politica.maxPrestamosActivos()) {

        throw new LimitePrestamosException("El usuario ya alcanzó el máximo de "

                \+ politica.maxPrestamosActivos() \+ " préstamos activos.");

    }

    // RN-01: el ejemplar debe existir y estar disponible.

    Ejemplar ejemplar \= ejemplarRepository.buscarPorCodigo(codigo)

            .orElseThrow(() \-\> new EntidadNoEncontradaException(

                    "No existe un ejemplar con código: " \+ codigo));

    if (ejemplar.getEstado() \!= EstadoEjemplar.DISPONIBLE) {

        throw new EjemplarNoDisponibleException("El ejemplar no está disponible para préstamo.");

    }

    LocalDate fechaLimite \= hoy.plusDays(politica.diasPrestamo());

    Prestamo prestamo \= new Prestamo(usuario, ejemplar, hoy, fechaLimite);

    try (Connection conexion \= proveedorConexion.obtenerConexion()) {

        conexion.setAutoCommit(false);

        try {

            boolean actualizado \= ejemplarRepository.actualizarEstado(

                    conexion, codigo, EstadoEjemplar.DISPONIBLE, EstadoEjemplar.PRESTADO);

            if (\!actualizado) {

                throw new EjemplarNoDisponibleException(

                        "El ejemplar dejó de estar disponible. "

                        \+ "Actualice la consulta e inténtelo de nuevo.");

            }

            int idGenerado \= prestamoRepository.insertar(conexion, prestamo);

            prestamo.setId(idGenerado);

            conexion.commit();

            return prestamo;

        }

        catch (RuntimeException | SQLException e) {

            rollback(conexion, e);

            if (e instanceof RuntimeException excepcion) {

                throw excepcion;

            }

            throw new DatosException("No se pudo registrar el préstamo.", e);

        }

    }

    catch (SQLException e) {

        throw new DatosException("Error de base de datos al registrar el préstamo.", e);

    }

}

## **Excepciones**

Las excepciones son *unchecked* y forman una jerarquía: `BibliotecaException` → `ValidacionException`, `EntidadNoEncontradaException`, `DatosException` y `ReglaNegocioException`, que a su vez agrupa `EjemplarNoDisponibleException`, `UsuarioConAtrasoException`, `LimitePrestamosException`, `PrestamoNoActivoException` y `UsuarioInactivoException`.

Excepción personalizada (RN-02):

java

package cr.ac.una.exception;

/\*\*

 \* RN-02: El usuario tiene préstamos atrasados.

 \*/

public class UsuarioConAtrasoException extends ReglaNegocioException {

    public UsuarioConAtrasoException(String mensaje) {

        super(mensaje);

    }

}

Se lanza en `PrestamoService.prestar` y se captura en `PrestamoController.prestar`, que la convierte en un mensaje para el bibliotecario:

java

public String prestar(String identificacionUsuario, String codigoEjemplar) {

    try {

        Prestamo prestamo \= prestamoService.prestar(identificacionUsuario, codigoEjemplar);

        return "Préstamo registrado correctamente. ID: "

                \+ prestamo.getId() \+ ". Fecha límite: " \+ prestamo.getFechaLimite();

    }

    catch (BibliotecaException e) {

        return "No se pudo registrar el préstamo: " \+ e.getMessage();

    }

}

Como red de seguridad, el menú de `App` envuelve cada opción en un `try/catch (RuntimeException e)` y muestra el mensaje con `Consola.error(...)`. Así, una entrada inválida o un error de base de datos nunca cierra la aplicación.

## **Evidencias de ejecución**

Capturas de la consola ejecutando la aplicación contra MySQL, con los datos de prueba de `database/schema.sql`:

| \# | Evidencia | Opción y datos usados |
| :---- | :---- | :---- |
| 1 | Conexión a MySQL | `1` |
| 2 | Registro de usuario | `4` → `404440444`, `Diego Rojas Mena`, `diego.rojas@correo.com` |
| 3 | Registro de libro | `13` → `978-0-596-00920-5`, `Head First Java`, `Kathy Sierra`, `2005` |
| 4 | Registro de ejemplar | `14` → `EJ-006`, `978-0-596-00920-5` |
| 5 | Listado de usuarios | `2` |
| 6 | Consulta por identificador | `3` → `101110111` |
| 7 | Ejemplares y disponibilidad | `7` y `8` → `978-9968-12-001-1` |
| 8 | Actualización | `15` → `EJ-004`, `2` (DAÑADO) |
| 9 | Préstamo exitoso | `9` → `101110111`, `EJ-001` |
| 10 | Consulta con JOIN | `11` → `101110111` |
| 11 | Rechazo RN-01 (ejemplar no disponible) | `9` → `202220222`, `EJ-005` |
| 12 | Rechazo RN-05 (usuario inactivo) | `9` → `303330333`, `EJ-002` |
| 13 | Rechazo RN-03 (máximo 3 activos) | `9` → `101110111` con `EJ-002`, `EJ-003` y luego `EJ-006` |
| 14 | Devolución exitosa | `10` → ID del préstamo de la evidencia 9 |
| 15 | Rechazo RN-04 (préstamo ya devuelto) | `10` → el mismo ID otra vez |
| 16 | Validación de datos | `4` → correo `correo-invalido` |

## **Distribución del trabajo**

| Integrante | Aportes principales |
| :---- | :---- |
| Maria Jimenez Alpizar | Documentación de la Etapa 2 (`docs/etapa_2/entrega.md`): cambios al diseño, instrucciones de ejecución, tabla de operaciones y reglas, fragmentos de código y evidencias de ejecución. |
| Mishia Valverde Narváez | Implementación de los servicios y controladores, repositorios JDBC (`repository/jdbc`), menú de consola (`App`, `Consola`) y presentación de la interfaz. |

[image1]: <data:image/png;base64,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>