
# Informe de mejoras

## Mejora de calidad implementada

**Tipo de mejora:** Separación de responsabilidades y manejo transaccional de la devolución.

**Problema identificado:**

Inicialmente, el proceso de devolución no contemplaba el cálculo y almacenamiento de multas por atraso. Era necesario incorporar esta regla de negocio y garantizar que la multa, la devolución del préstamo y la actualización del estado del ejemplar se guardaran de forma consistente.

**Antes:**

El proceso de devolución no incluía el cálculo ni el registro de multas por atraso. Por lo tanto, no se aplicaba la regla de negocio de cobrar ₡200 por cada día de atraso.

**Después:**

Se incorporó el cálculo de la multa mediante `PoliticaPrestamo`, mientras que `PrestamoService` coordina la devolución y el almacenamiento de los cambios dentro de una transacción.

Fragmento de `PrestamoService.java`, método `devolver()`:

```java
// RN-N1: ₡200 por cada día de atraso (0 si se devuelve a tiempo).
long diasAtraso = prestamo.diasAtraso(fechaDevolucion);
BigDecimal multa = politica.calcularMulta(diasAtraso);

try (Connection conexion = proveedorConexion.obtenerConexion()) {
    conexion.setAutoCommit(false);

    try {
        // La multa se guarda en la misma transacción de la devolución.
        prestamoRepository.registrarDevolucion(
                conexion,
                idPrestamo,
                fechaDevolucion,
                multa);
```

Si ocurre un error durante la operación, se ejecuta `rollback` para evitar guardar parcialmente los cambios. Cuando todo finaliza correctamente, se confirma la transacción mediante `commit`.

**Beneficio obtenido:**

- Se aplica la regla de negocio de ₡200 por cada día de atraso.
- La multa queda registrada junto con la devolución del préstamo.
- Se evita confirmar parcialmente la devolución si falla la actualización del ejemplar o el registro en la base de datos.
- Se separa el cálculo de la multa, realizado por `PoliticaPrestamo`, de la coordinación de la operación, realizada por `PrestamoService`.

**Commits relacionados:**

- `d41ed6e`: Agrega el cálculo de multa por día de atraso en `PoliticaPrestamo`.
- `7c58944`: Calcula y guarda la multa al registrar la devolución (RN-N1).

## Defectos encontrados y corregidos

| ID | Defecto | Cómo se detectó (prueba) | Corrección | Commit |
|---|---|---|---|---|
| D-01 | La devolución no calculaba ni guardaba multas por atraso. | P-01, P-02, P-03 y P-07. | Se incorporó el cálculo de la multa y su almacenamiento durante la devolución. | `d41ed6e`, `7c58944` |
| D-02 | El sistema no verificaba las multas pendientes antes de permitir nuevos préstamos. | P-09. | Se agregó la validación de multas pendientes en `PrestamoService.prestar()`. | `a75a040` |
| D-03 | No existía una operación para registrar el pago de una multa pendiente. | P-10. | Se implementó el registro del pago y la actualización del estado de la multa. | `a75a040` |
