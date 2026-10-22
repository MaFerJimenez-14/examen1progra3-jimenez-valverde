package cr.ac.una.controller;

import cr.ac.una.exception.BibliotecaException;
import cr.ac.una.model.Prestamo;
import cr.ac.una.service.PrestamoService;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.Objects;

/*
 * Punto de entrada desde la consola para préstamos, devoluciones y reportes.
 */

public class PrestamoController {
    
    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = Objects.requireNonNull(
                prestamoService, "El servicio de préstamos es obligatorio.");
    }

    /* Devuelve un mensaje con el id del préstamo y la fecha límite, o el motivo del rechazo. */
    public String prestar(String identificacionUsuario, String codigoEjemplar) {
        try {
            Prestamo prestamo = prestamoService.prestar(identificacionUsuario, codigoEjemplar);

            return "Préstamo registrado correctamente. ID: "
                    + prestamo.getId() + ". Fecha límite: " + prestamo.getFechaLimite();
        } 
        catch (BibliotecaException e) {
            return "No se pudo registrar el préstamo: " + e.getMessage();
        }
    }

    /* Devuelve un mensaje de confirmación indicando los días de atraso, si los hay. */
    public String devolver(int idPrestamo) {
        try {
            Prestamo prestamo = prestamoService.devolver(idPrestamo);

            long diasAtraso = 0;

            if (prestamo.getFechaDevolucion() != null
                    && prestamo.getFechaDevolucion().isAfter(prestamo.getFechaLimite())) {

                diasAtraso = ChronoUnit.DAYS.between(
                        prestamo.getFechaLimite(), prestamo.getFechaDevolucion());
            }

            String mensaje = "Devolución registrada correctamente. " + "Préstamo ID: " + prestamo.getId() + ".";

            if (diasAtraso > 0) {
                mensaje += " Días de atraso: " + diasAtraso + "."
                        + " Multa generada: ₡" + prestamo.getMulta() + " (pendiente de pago).";
            } 
            else {
                mensaje += " Sin días de atraso.";
            }

            return mensaje;
        } 
        catch (BibliotecaException e) {
            return "No se pudo registrar la devolución: " + e.getMessage();
        }
    }

    public List<Prestamo> listarActivosPorUsuario(String identificacionUsuario) {
        return prestamoService.listarActivosPorUsuario(identificacionUsuario);
    }

    public List<Prestamo> reporteAtrasados() {
        return prestamoService.listarAtrasados();
    }
    
    public List<Prestamo> reporteMultasPendientes() {
        return prestamoService.listarMultasPendientes();
    }
    
    public String pagarMulta(int idPrestamo) {
        try {
            prestamoService.pagarMulta(idPrestamo);
            return "Pago de multa registrado correctamente. " + "Préstamo ID: " + idPrestamo + ".";
        } catch (BibliotecaException e) {
            return "No se pudo registrar el pago: " + e.getMessage();
        }
    }
}
