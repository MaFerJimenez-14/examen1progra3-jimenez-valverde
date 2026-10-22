package cr.ac.una;

import cr.ac.una.config.ProveedorConexion;
import cr.ac.una.exception.PrestamoNoActivoException;
import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;
import cr.ac.una.model.EstadoPrestamo;
import cr.ac.una.model.Libro;
import cr.ac.una.model.Prestamo;
import cr.ac.una.model.Usuario;
import cr.ac.una.repository.EjemplarRepository;
import cr.ac.una.repository.PrestamoRepository;
import cr.ac.una.repository.UsuarioRepository;
import cr.ac.una.service.PoliticaPrestamo;
import cr.ac.una.service.PrestamoService;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del cálculo y almacenamiento de multas (RN-N1).
 *
 * El servicio se prueba sin MySQL: los repositorios son implementaciones en
 * memoria de las interfaces y la fecha de "hoy" se fija con un Clock.
 */
class PrestamoServiceMultaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Costa_Rica");
    private static final LocalDate FECHA_LIMITE = LocalDate.of(2026, 10, 10);

    private PrestamoRepositoryEnMemoria prestamos;
    private EjemplarRepositoryEnMemoria ejemplares;

    @BeforeEach
    void prepararDatos() {
        prestamos = new PrestamoRepositoryEnMemoria();
        ejemplares = new EjemplarRepositoryEnMemoria();

        Usuario usuario = new Usuario("101110111", "Ana Mora", "ana.mora@correo.com", true);
        Libro libro = new Libro("978-9968-12-001-1", "Cuentos de mi tía Panchita", "Carmen Lyra", 1920);
        Ejemplar ejemplar = new Ejemplar("EJ-001", libro, EstadoEjemplar.PRESTADO);
        ejemplares.guardar(ejemplar);

        // Préstamo activo con fecha límite 10/10/2026.
        prestamos.guardar(new Prestamo(1, usuario, ejemplar, FECHA_LIMITE.minusDays(8),
                FECHA_LIMITE, null, EstadoPrestamo.ACTIVO));
    }

    /** Crea el servicio con "hoy" fijado en la fecha indicada. */
    private PrestamoService servicioConFecha(LocalDate hoy) {
        Clock reloj = Clock.fixed(hoy.atStartOfDay(ZONA).toInstant(), ZONA);
        return new PrestamoService(new UsuarioRepositoryVacio(), ejemplares, prestamos,
                conexionFalsa(), PoliticaPrestamo.POR_DEFECTO, reloj);
    }

    // -----------------------------------------------------------------
    // Pruebas exitosas
    // -----------------------------------------------------------------

    @Test
    void devolucionATiempoNoGeneraMulta() {
        Prestamo devuelto = servicioConFecha(FECHA_LIMITE).devolver(1);

        assertEquals(0, devuelto.getMulta().compareTo(BigDecimal.ZERO));
        assertFalse(devuelto.tieneMultaPendiente());
        assertEquals(0, prestamos.multaGuardada(1).compareTo(BigDecimal.ZERO),
                "Debe guardarse multa 0 en el préstamo.");
    }

    @Test
    void devolucionConUnDiaDeAtrasoGenera200() {
        Prestamo devuelto = servicioConFecha(FECHA_LIMITE.plusDays(1)).devolver(1);

        assertEquals(0, devuelto.getMulta().compareTo(new BigDecimal("200")));
        assertTrue(devuelto.tieneMultaPendiente());
    }

    @Test
    void devolucionConCincoDiasDeAtrasoGuardaMultaDe1000() {
        Prestamo devuelto = servicioConFecha(FECHA_LIMITE.plusDays(5)).devolver(1);

        assertEquals(0, devuelto.getMulta().compareTo(new BigDecimal("1000")));
        assertEquals(EstadoPrestamo.DEVUELTO, devuelto.getEstado());
        assertFalse(devuelto.isMultaPagada(), "La multa nueva queda pendiente de pago.");

        // La multa llega al repositorio, es decir, se guarda en el préstamo.
        assertEquals(0, prestamos.multaGuardada(1).compareTo(new BigDecimal("1000")));
        assertEquals(EstadoEjemplar.DISPONIBLE,
                ejemplares.buscarPorCodigo("EJ-001").orElseThrow().getEstado());
    }

    @Test
    void politicaCalculaMultaPorDiasDeAtraso() {
        PoliticaPrestamo politica = PoliticaPrestamo.POR_DEFECTO;

        assertEquals(0, politica.calcularMulta(0).compareTo(BigDecimal.ZERO));
        assertEquals(0, politica.calcularMulta(-3).compareTo(BigDecimal.ZERO));
        assertEquals(0, politica.calcularMulta(7).compareTo(new BigDecimal("1400")));
    }

    // -----------------------------------------------------------------
    // Pruebas de rechazo
    // -----------------------------------------------------------------

    @Test
    void noSeDevuelveDosVecesNiSeCambiaLaMulta() {
        servicioConFecha(FECHA_LIMITE.plusDays(2)).devolver(1); // multa de ₡400

        PrestamoService despues = servicioConFecha(FECHA_LIMITE.plusDays(10));
        assertThrows(PrestamoNoActivoException.class, () -> despues.devolver(1));

        assertEquals(0, prestamos.multaGuardada(1).compareTo(new BigDecimal("400")),
                "Un segundo intento no debe recalcular la multa.");
    }

    @Test
    void multaPagadaNoQuedaPendiente() {
        Prestamo prestamo = new Prestamo();
        prestamo.setMulta(new BigDecimal("600"));
        prestamo.setMultaPagada(true);

        assertFalse(prestamo.tieneMultaPendiente());
    }

    // -----------------------------------------------------------------
    // Dobles de prueba (en memoria)
    // -----------------------------------------------------------------

    /** Connection que acepta setAutoCommit/commit/rollback/close sin hacer nada. */
    private static ProveedorConexion conexionFalsa() {
        Connection conexion = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, metodo, args) -> metodo.getReturnType() == boolean.class ? false : null);
        return () -> conexion;
    }

    private static class PrestamoRepositoryEnMemoria implements PrestamoRepository {

        private final Map<Integer, Prestamo> datos = new HashMap<>();

        void guardar(Prestamo prestamo) {
            datos.put(prestamo.getId(), prestamo);
        }

        BigDecimal multaGuardada(int id) {
            return datos.get(id).getMulta();
        }

        @Override
        public int insertar(Connection conexion, Prestamo prestamo) {
            int id = datos.size() + 1;
            prestamo.setId(id);
            datos.put(id, prestamo);
            return id;
        }

        @Override
        public Optional<Prestamo> buscarPorId(int id) {
            Prestamo p = datos.get(id);
            if (p == null) {
                return Optional.empty();
            }
            // Copia, como haría una consulta real a la base de datos.
            return Optional.of(new Prestamo(p.getId(), p.getUsuario(), p.getEjemplar(),
                    p.getFechaPrestamo(), p.getFechaLimite(), p.getFechaDevolucion(),
                    p.getEstado(), p.getMulta(), p.isMultaPagada()));
        }

        @Override
        public void registrarDevolucion(Connection conexion, int id, LocalDate fechaDevolucion,
                                        BigDecimal multa) {
            Prestamo p = datos.get(id);
            p.setFechaDevolucion(fechaDevolucion);
            p.setEstado(EstadoPrestamo.DEVUELTO);
            p.setMulta(multa);
            p.setMultaPagada(false);
        }

        @Override
        public List<Prestamo> listarActivosPorUsuario(String identificacionUsuario) {
            return new ArrayList<>();
        }

        @Override
        public List<Prestamo> listarAtrasados(LocalDate hoy) {
            return new ArrayList<>();
        }

        @Override
        public int contarActivosPorUsuario(String identificacionUsuario) {
            return 0;
        }

        @Override
        public boolean tieneAtrasados(String identificacionUsuario, LocalDate hoy) {
            return false;
        }
    }

    private static class EjemplarRepositoryEnMemoria implements EjemplarRepository {

        private final Map<String, Ejemplar> datos = new HashMap<>();

        void guardar(Ejemplar ejemplar) {
            datos.put(ejemplar.getCodigo(), ejemplar);
        }

        @Override
        public void insertar(Ejemplar ejemplar) {
            guardar(ejemplar);
        }

        @Override
        public Optional<Ejemplar> buscarPorCodigo(String codigo) {
            return Optional.ofNullable(datos.get(codigo));
        }

        @Override
        public List<Ejemplar> listarPorLibro(String isbn) {
            return new ArrayList<>(datos.values());
        }

        @Override
        public int contarDisponibles(String isbn) {
            return 0;
        }

        @Override
        public void actualizarEstado(String codigo, EstadoEjemplar nuevoEstado) {
            datos.get(codigo).setEstado(nuevoEstado);
        }

        @Override
        public boolean actualizarEstado(Connection conexion, String codigo,
                                        EstadoEjemplar estadoEsperado, EstadoEjemplar nuevoEstado) {
            Ejemplar e = datos.get(codigo);
            if (e == null || e.getEstado() != estadoEsperado) {
                return false;
            }
            e.setEstado(nuevoEstado);
            return true;
        }
    }

    /** La devolución no consulta usuarios. */
    private static class UsuarioRepositoryVacio implements UsuarioRepository {
        @Override public void insertar(Usuario usuario) { }
        @Override public Optional<Usuario> buscarPorId(String id) { return Optional.empty(); }
        @Override public Optional<Usuario> buscarPorCorreo(String correo) { return Optional.empty(); }
        @Override public List<Usuario> listar() { return new ArrayList<>(); }
        @Override public void actualizar(Usuario usuario) { }
    }
}
