package cr.ac.una.controller;

import cr.ac.una.model.Prestamo;
import cr.ac.una.service.PrestamoService;

import java.util.List;

/*
 * Punto de entrada desde la consola para préstamos, devoluciones y reportes.
 */

public class PrestamoController {
    
    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    /* Devuelve un mensaje con el id del préstamo y la fecha límite, o el motivo del rechazo. */
    public String prestar(String identificacionUsuario, String codigoEjemplar) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    /* Devuelve un mensaje de confirmación indicando los días de atraso, si los hay. */
    public String devolver(int idPrestamo) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public List<Prestamo> listarActivosPorUsuario(String identificacionUsuario) {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }

    public List<Prestamo> reporteAtrasados() {
        throw new UnsupportedOperationException("Pendiente Etapa 2");
    }
    
}
