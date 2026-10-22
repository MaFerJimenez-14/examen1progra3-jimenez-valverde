# Etapa 3 — Integración, pruebas y mejora

## Integración

El sistema se ejecuta desde un menú de consola (`cr.ac.una.app.App`) que delega en los controladores. Todas las opciones operan sobre MySQL 8.0.32 mediante JDBC; la conexión se verificó con la opción 1.

| Opción del menú | Funcionalidad | Estado |
|---|---|---|
| 1 | Probar conexión a MySQL | ✅ |
| 2 | Listar usuarios | ✅ |
| 3 | Buscar usuario | ✅ |
| 4 | Registrar usuario | ✅ |
| 5 | Listar libros | ✅ |
| 6 | Buscar libro por ISBN | ✅ |
| 7 | Consultar ejemplares de un libro | ✅ |
| 8 | Consultar disponibilidad | ✅ |
| 9 | Registrar préstamo (valida RN-01, RN-02, RN-03, RN-05) | ✅ |
| 10 | Registrar devolución (calcula y guarda la multa, RN-N1) | ✅ |
| 11 | Listar préstamos activos de un usuario | ✅ |
| 12 | Reporte de préstamos atrasados | ✅ |
| 13 | Registrar libro | ✅ |
| 14 | Registrar ejemplar | ✅ |
| 15 | Cambiar estado de ejemplar | ✅ |
| (Persona 2: completar) | Registrar pago de multa (RN-N3) | |
| (Persona 2: completar) | Usuarios con multas pendientes y total adeudado | |

## Cambio de requerimiento

**Cambio solicitado por el docente:**

Multas por atraso. Las devoluciones atrasadas generan una multa que el usuario debe pagar.

- **Datos:** nuevos atributos `multa` (decimal, por defecto 0) y `multaPagada` (booleano) en `Prestamo`, con sus columnas.
- **RN-N1.** Al devolver con atraso, la multa es de ₡200 por cada día de atraso y se guarda en el préstamo.
- **RN-N2.** Un usuario con alguna multa sin pagar no puede solicitar nuevos préstamos.
- **RN-N3.** Nueva operación: registrar el pago de una multa (solo si existe y está pendiente).
- **Excepción sugerida:** `MultaPendienteException`.
- **Consulta nueva:** listar los usuarios con multas pendientes y el total adeudado por cada uno.

**Clases modificadas o agregadas:**

| Clase | Capa | Cambio realizado | Commit |
|---|---|---|---|
| `Prestamo` | model | Atributos `multa` (`BigDecimal`, por defecto 0) y `multaPagada`, constructor que los incluye y método `tieneMultaPendiente()` | [`722ad62`](https://github.com/MaFerJimenez-14/examen1progra3-jimenez-valverde/commit/722ad62) |
| `schema.sql` | base de datos | Columnas `multa DECIMAL(10,2) DEFAULT 0` y `multa_pagada BOOLEAN DEFAULT FALSE`, `CHECK (multa >= 0)`, índice por usuario y datos de prueba con multas | [`b740367`](https://github.com/MaFerJimenez-14/examen1progra3-jimenez-valverde/commit/b740367) |
| `PoliticaPrestamo` | service | Nuevo parámetro `multaPorDia` (₡200) y método `calcularMulta(diasAtraso)` | [`d41ed6e`](https://github.com/MaFerJimenez-14/examen1progra3-jimenez-valverde/commit/d41ed6e) |
| `PrestamoService` | service | `devolver()` calcula la multa y la guarda en la misma transacción de la devolución (RN-N1) | [`7c58944`](https://github.com/MaFerJimenez-14/examen1progra3-jimenez-valverde/commit/7c58944) |
| `PrestamoRepository` | repository | `registrarDevolucion(...)` recibe la multa como parámetro | [`7c58944`](https://github.com/MaFerJimenez-14/examen1progra3-jimenez-valverde/commit/7c58944) |
| `PrestamoRepositoryJDBC` | repository | El `UPDATE` de la devolución guarda la multa; los `SELECT` y el mapeo leen `multa` y `multa_pagada` | [`7c58944`](https://github.com/MaFerJimenez-14/examen1progra3-jimenez-valverde/commit/7c58944) |
| `PrestamoController` | controller | El mensaje de devolución informa la multa generada | [`7c58944`](https://github.com/MaFerJimenez-14/examen1progra3-jimenez-valverde/commit/7c58944) |
| `PrestamoServiceMultaTest` | test | 6 pruebas JUnit del cálculo y almacenamiento de multas | [`cd2a0d2`](https://github.com/MaFerJimenez-14/examen1progra3-jimenez-valverde/commit/cd2a0d2) |
| `MultaPendienteException` | exception | (Persona 2: completar) | |
| (Persona 2: completar) | | Bloqueo de préstamos con multa pendiente (RN-N2) | |
| (Persona 2: completar) | | Registro del pago de multa (RN-N3) | |
| (Persona 2: completar) | | Consulta de usuarios con multas pendientes | |

**Análisis:** ¿qué partes del diseño facilitaron el cambio? ¿qué fue necesario rehacer?

El diseño por capas permitió agregar las multas sin rehacer componentes:

- **Parámetros centralizados.** El plazo y el máximo de préstamos ya vivían en `PoliticaPrestamo`. El monto de la multa se agregó ahí mismo, junto con su cálculo, por lo que un cambio de tarifa se hace en un solo lugar.
- **Transacción en el `Service`.** `PrestamoService.devolver()` ya controlaba una transacción que actualiza `prestamo` y `ejemplar`. Guardar la multa solo requirió pasar un dato más al repositorio dentro de esa misma transacción: si algo falla, la devolución y la multa se revierten juntas.
- **Mapeo en un solo método.** `PrestamoRepositoryJDBC` convierte cada fila en un `Prestamo` en `mapearPrestamo()`, así que leer las columnas nuevas se resolvió en un solo lugar para todas las consultas.
- **Dependencia de interfaces.** Como `PrestamoService` depende de `PrestamoRepository` (interface) y recibe un `Clock`, las pruebas del cálculo se escribieron con repositorios en memoria y una fecha fija, sin base de datos.
- **`Prestamo#diasAtraso`** ya existía desde la Etapa 1, por lo que el cálculo de la multa lo reutiliza.

Lo que fue necesario modificar: la firma de `PrestamoRepository.registrarDevolucion(...)`, que ahora recibe la multa, y por lo tanto su implementación JDBC. El constructor anterior de `Prestamo` se mantuvo para no afectar el código existente.

(Persona 2: completar el análisis de su parte: cómo se integró el bloqueo en `PrestamoService.prestar()` y el pago.)

## Decisiones de diseño relevantes

- **La multa se guarda en el préstamo**, no en una tabla aparte, tal como pide el cambio. Cada préstamo genera a lo sumo una multa, por lo que no hace falta otra entidad.
- **`BigDecimal` para el monto**, en lugar de `double`, para evitar errores de redondeo con dinero. En la base de datos se usa `DECIMAL(10,2)`.
- **La multa se calcula en el `Service`** y no en el repositorio ni en la base de datos, porque es una regla de negocio (RN-N1). El repositorio solo la guarda.
- **Se guarda en la misma transacción de la devolución**, para que nunca quede un préstamo devuelto sin su multa ni una multa sin devolución.
- **`multa_pagada` inicia en `FALSE`.** Una multa se considera pendiente solo si el monto es mayor que 0 y no está pagada (`Prestamo#tieneMultaPendiente()`), así que las devoluciones a tiempo (multa 0) no bloquean al usuario.
- **Restricción `CHECK (multa >= 0)`** en la base de datos como respaldo de la regla validada en el `Service`.
- **Datos de prueba con fechas relativas** (`CURDATE() - INTERVAL n DAY`), para que los casos de multa pendiente y préstamo atrasado sirvan cualquier día en que se ejecute el script.

## Pruebas y mejoras

- Pruebas: [`pruebas.md`](pruebas.md)
- Mejoras y defectos corregidos: [`mejoras.md`](mejoras.md)

## UML final

El diseño cambió: `Prestamo` tiene los atributos `multa` y `multaPagada`, `PoliticaPrestamo` tiene `multaPorDia` y `calcularMulta()`, y `PrestamoRepository.registrarDevolucion(...)` recibe la multa. (Persona 2: agregar sus clases y métodos nuevos.)

Diagrama actualizado: [`docs/etapa_1/diagramas/uml_final.png`](../etapa_1/diagramas/uml_final.png)

## Aspectos pendientes

- (Completar al final, por ejemplo: funciones que no se alcanzaron o mejoras posibles.)

## Distribución del trabajo

| Integrante | Aportes principales |
|---|---|
| Maria Jimenez Alpizar | Atributos de multa en `Prestamo`; columnas, restricciones y datos de prueba en `schema.sql`; cálculo de la multa en `PoliticaPrestamo` y `PrestamoService` (RN-N1); guardado y lectura de la multa en `PrestamoRepositoryJDBC`; pruebas JUnit `PrestamoServiceMultaTest`; pruebas manuales y documentación de pruebas P-01 a P-08. |
| Mishia Valverde Narváez | (Persona 2: completar) `MultaPendienteException`, bloqueo de préstamos con multa pendiente (RN-N2), pago de multas (RN-N3), consulta de usuarios con deudas y sus pruebas. |
| Ambas | Documentación final, UML final, README y video de demostración. |