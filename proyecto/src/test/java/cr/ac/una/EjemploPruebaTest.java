
package cr.ac.una;

import cr.ac.una.config.ProveedorConexion;
import cr.ac.una.model.EstadoPrestamo;
import cr.ac.una.model.Prestamo;
import cr.ac.una.repository.EjemplarRepository;
import cr.ac.una.repository.PrestamoRepository;
import cr.ac.una.repository.UsuarioRepository;
import cr.ac.una.service.PoliticaPrestamo;
import cr.ac.una.service.PrestamoService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EjemploPruebaTest {

    @Test
    void reporteIncluyePrestamosAtrasados() {
        LocalDate hoy = LocalDate.of(2026, 10, 30);
        LocalDate fechaLimite = LocalDate.of(2026, 10, 19);

        Clock reloj = Clock.fixed(
                Instant.parse("2026-10-30T12:00:00Z"),
                ZoneOffset.UTC
        );

        // Simular las dependencias sin conectarse a MySQL.
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        EjemplarRepository ejemplares = mock(EjemplarRepository.class);
        PrestamoRepository prestamos = mock(PrestamoRepository.class);
        ProveedorConexion proveedor = mock(ProveedorConexion.class);

        // Crear un préstamo ficticio atrasado.
        Prestamo prestamoAtrasado = mock(Prestamo.class);

        when(prestamoAtrasado.getEstado())
                .thenReturn(EstadoPrestamo.ACTIVO);
        when(prestamoAtrasado.getFechaLimite())
                .thenReturn(fechaLimite);

        when(prestamos.listarAtrasados(hoy))
                .thenReturn(List.of(prestamoAtrasado));

        PrestamoService servicio = new PrestamoService(
                usuarios,
                ejemplares,
                prestamos,
                proveedor,
                PoliticaPrestamo.POR_DEFECTO,
                reloj
        );

        List<Prestamo> resultado = servicio.listarAtrasados();

        assertFalse(resultado.isEmpty(),
                "El reporte debería incluir el préstamo atrasado.");

        assertEquals(1, resultado.size(),
                "Se esperaba un préstamo atrasado.");

        assertTrue(resultado.stream().allMatch(p ->
                        p.getEstado() == EstadoPrestamo.ACTIVO
                        && p.getFechaLimite().isBefore(hoy)),
                "El reporte debe incluir préstamos activos vencidos.");

        verify(prestamos).listarAtrasados(hoy);
    }
}
