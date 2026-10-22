# Gestión de Biblioteca

## Curso

EIF206 – Programación III

## Integrantes

| Nombre | Usuario de GitHub |
|---|---|
| Maria Jimenez Alpizar | MaFerJimenez-14 |
| Mishia Valverde Narváez | mishia-gvn |

## Caso asignado

Caso 5 - Gestión de Biblioteca

## Descripción del sistema

La biblioteca comunitaria necesita un sistema que permita administrar el catálogo de libros, sus ejemplares físicos, los usuarios registrados y los préstamos realizados.

El sistema permite controlar la disponibilidad de los ejemplares, registrar préstamos y devoluciones, consultar préstamos atrasados y administrar multas por atraso. También aplica reglas de negocio para evitar operaciones no permitidas y mantener la consistencia de la información.

La aplicación está diseñada para ser utilizada por el personal bibliotecario mediante una interfaz de consola, con persistencia de datos en MySQL.

## Funcionalidades implementadas

- Registrar, consultar, listar y actualizar usuarios.
- Registrar libros y sus ejemplares físicos.
- Consultar libros, ejemplares y su disponibilidad.
- Cambiar el estado de los ejemplares según las reglas establecidas.
- Registrar préstamos y calcular sus fechas límite.
- Registrar devoluciones y calcular las multas por atraso.
- Consultar los préstamos activos de un usuario.
- Generar un reporte de préstamos atrasados.
- Consultar las multas pendientes de pago agrupadas por usuario.
- Consultar el total adeudado por cada usuario.
- Bloquear nuevos préstamos cuando el usuario tiene multas pendientes.
- Registrar el pago de una multa pendiente.

## Reglas de negocio implementadas

| ID | Regla | Dónde se valida |
|---|---|---|
| RN-01 | No se puede prestar un ejemplar cuyo estado no sea `DISPONIBLE`. | `PrestamoService` |
| RN-02 | Un usuario con préstamos atrasados no puede solicitar nuevos préstamos. | `PrestamoService` |
| RN-03 | Un usuario no puede tener más de tres préstamos `ACTIVOS` simultáneamente. | `PrestamoService` |
| RN-04 | Solo se puede devolver un préstamo que esté en estado `ACTIVO`. | `PrestamoService` |
| RN-05 | Un usuario inactivo no puede solicitar préstamos. | `PrestamoService` |
| RN-06 | La fecha límite corresponde a la fecha del préstamo más ocho días. | `PoliticaPrestamo` |
| RN-07 | La identificación y el correo de un usuario, el ISBN de un libro y el código de un ejemplar deben ser únicos. | Servicios y base de datos |
| RN-08 | Un ejemplar en estado `PRESTADO` solo puede volver a `DISPONIBLE` mediante el proceso de devolución. | `CatalogoService` y `PrestamoService` |
| RN-09 | Los campos obligatorios deben contener datos válidos, el correo debe tener un formato válido y el año del libro debe estar entre 1450 y 2100. | Servicios y validaciones de datos |
| RN-N1 | Se cobra una multa de ₡200 por cada día de atraso en la devolución. Si no hay atraso, la multa es de ₡0. | `PoliticaPrestamo` y `PrestamoService` |
| RN-N2 | Un usuario con multas pendientes de pago no puede solicitar nuevos préstamos. | `PrestamoService` |
| RN-N3 | El sistema permite registrar el pago de una multa pendiente y actualizar su estado. | `PrestamoService` y `PrestamoRepositoryJDBC` |


## Arquitectura

```text
App (consola) → Controller → Service → Repository (interface) → RepositoryJdbc → MySQL
```

- **App:** punto de entrada y presentación del menú de consola.
- **Controller:** coordina las solicitudes del usuario y las operaciones de los servicios.
- **Service:** concentra las validaciones y reglas de negocio.
- **Repository:** define las operaciones de persistencia mediante interfaces.
- **RepositoryJdbc:** implementación prevista para el acceso a MySQL mediante JDBC.
- **Model:** contiene las entidades y enumeraciones del dominio.
- **Config:** proporciona la configuración y las conexiones a la base de datos.
- **Exception:** contiene las excepciones personalizadas del sistema.

## Estructura del proyecto

```text
proyecto/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── cr/ac/una/
    │   │       ├── app/
    │   │       │   ├── App.java
    │   │       │   └── Consola.java
    │   │       ├── config/
    │   │       │   ├── ConexionBD.java
    │   │       │   └── ProveedorConexion.java
    │   │       ├── controller/
    │   │       │   ├── UsuarioController.java
    │   │       │   ├── CatalogoController.java
    │   │       │   └── PrestamoController.java
    │   │       ├── exception/
    │   │       │   ├── BibliotecaException.java
    │   │       │   ├── DatosException.java
    │   │       │   ├── EjemplarNoDisponibleException.java
    │   │       │   ├── EntidadNoEncontradaException.java
    │   │       │   ├── LimitePrestamosException.java
    │   │       │   ├── MultaPendienteException.java
    │   │       │   ├── PrestamoNoActivoException.java
    │   │       │   ├── ReglaNegocioException.java
    │   │       │   ├── UsuarioConAtrasoException.java
    │   │       │   ├── UsuarioInactivoException.java
    │   │       │   └── ValidacionException.java
    │   │       ├── model/
    │   │       │   ├── Usuario.java
    │   │       │   ├── Libro.java
    │   │       │   ├── Ejemplar.java
    │   │       │   ├── Prestamo.java
    │   │       │   ├── EstadoEjemplar.java
    │   │       │   └── EstadoPrestamo.java
    │   │       ├── repository/
    │   │       │   ├── UsuarioRepository.java
    │   │       │   ├── LibroRepository.java
    │   │       │   ├── EjemplarRepository.java
    │   │       │   └── PrestamoRepository.java
    │   │       └── service/
    │   │           ├── UsuarioService.java
    │   │           ├── CatalogoService.java
    │   │           ├── PrestamoService.java
    │   │           └── PoliticaPrestamo.java
    │   └── resources/
    │       ├── db.properties
    │       └── db.properties.example
    └── test/
        └── java/
            └── cr/ac/una/
                ├── EjemploPruebaTest.java
                └── PrestamoServiceMultaTest
```

## Configuración de la base de datos

El proyecto utiliza MySQL para almacenar la información de la biblioteca. El esquema inicial se encuentra en `database/schema.sql`, ubicado en la raíz del repositorio.

La conexión se configura mediante el archivo local `db.properties`, utilizado por la clase `ConexionBD`.

1. Verificar que el servidor MySQL esté instalado y activo.
2. Crear la base de datos utilizando el esquema SQL del proyecto.
3. Copiar `db.properties.example` como `db.properties`.
4. Configurar el nombre de la base de datos, el usuario y la contraseña.
5. Verificar que las credenciales tengan los permisos necesarios.


Desde la raíz del repositorio, ejecutar:

```bash
mysql -u root -p < database/schema.sql
```

Luego, configurar el archivo de conexión. En Windows, también se puede copiar el archivo desde el explorador de archivos o desde NetBeans.

```bash
cd proyecto/src/main/resources
copy db.properties.example db.properties
```

Editar `db.properties` con los valores correspondientes al entorno local. No publicar contraseñas ni credenciales personales en el repositorio.


## Pasos para ejecutar

El proyecto utiliza Maven y puede abrirse y ejecutarse desde Apache NetBeans.

1. Clonar el repositorio desde GitHub.
2. Abrir en NetBeans la carpeta `proyecto`, donde se encuentra `pom.xml`.
3. Verificar la configuración de conexión a MySQL.
4. Verificar que la base de datos y las tablas estén creadas.
5. Ejecutar el proyecto desde NetBeans.

También puede ejecutarse desde una terminal, siempre que Maven y JDK 21 estén correctamente configurados:

```bash
git clone https://github.com/MaFerJimenez-14/examen1progra3-jimenez-valverde.git
cd examen1progra3-jimenez-valverde/proyecto
mvn clean compile exec:java
```

## Ejecutar las pruebas

El proyecto incluye pruebas automatizadas con JUnit 5.

- `PrestamoServiceMultaTest`: comprueba el cálculo de multas y el comportamiento relacionado con las devoluciones, el bloqueo de préstamos por multas pendientes y el pago de multas.
- `EjemploPruebaTest`: contiene pruebas de ejemplo.

Para ejecutar las pruebas desde la terminal:

```bash
cd proyecto
mvn test
```

Las pruebas de integración manual con MySQL y sus evidencias se documentan en `docs/etapa_3/pruebas.md`.

## Documentación adicional

La documentación de la tercera etapa se encuentra en `docs/etapa_3/`:

- `entrega.md`: integración, decisiones de diseño y distribución del trabajo.
- `pruebas.md`: pruebas realizadas y sus evidencias.
- `mejoras.md`: mejora de calidad implementada y defectos corregidos.

## Video de demostración

**Enlace:** pendiente de agregar.

<enlace al video (máximo 5 minutos, participan ambos integrantes)>

## Tecnologías utilizadas

- **Java:** JDK 21.
- **Maven:** gestión de dependencias y compilación.
- **MySQL:** sistema de gestión de bases de datos.
- **MySQL Connector/J:** controlador JDBC para la conexión con MySQL.
- **JUnit 5:** framework utilizado para las pruebas de ejemplo.
- **Apache NetBeans:** entorno de desarrollo utilizado.
