
package cr.ac.una.app;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.config.ProveedorConexion;

import cr.ac.una.controller.CatalogoController;
import cr.ac.una.controller.PrestamoController;
import cr.ac.una.controller.UsuarioController;

import cr.ac.una.model.Ejemplar;
import cr.ac.una.model.EstadoEjemplar;
import cr.ac.una.model.Libro;
import cr.ac.una.model.Prestamo;
import cr.ac.una.model.Usuario;

import cr.ac.una.repository.jdbc.EjemplarRepositoryJDBC;
import cr.ac.una.repository.jdbc.LibroRepositoryJDBC;
import cr.ac.una.repository.jdbc.PrestamoRepositoryJDBC;
import cr.ac.una.repository.jdbc.UsuarioRepositoryJDBC;

import cr.ac.una.service.CatalogoService;
import cr.ac.una.service.PoliticaPrestamo;
import cr.ac.una.service.PrestamoService;
import cr.ac.una.service.UsuarioService;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import java.time.Clock;

import java.util.List;

/**
 * Punto de entrada de la aplicación (interfaz de consola).
 *
 * Responsabilidad de esta clase: mostrar menús, leer datos y presentar
 * resultados. No debe contener SQL ni reglas de negocio: debe delegar en
 * los Controllers del paquete controller.
 *
 * Flujo esperado:  App (consola) → Controller → Service → Repository → MySQL
 */

public class App {

    private static UsuarioController usuarioController;
    private static CatalogoController catalogoController;
    private static PrestamoController prestamoController;

    public static void main(String[] args) {
        System.setOut(new java.io.PrintStream(System.out, true, 
                java.nio.charset.StandardCharsets.UTF_8));

        System.setErr(new java.io.PrintStream(System.err, true,
                java.nio.charset.StandardCharsets.UTF_8));

        configurarAplicacion();
        mostrarMenu();
    }

    private static void configurarAplicacion() {
        UsuarioRepositoryJDBC usuarioRepo = new UsuarioRepositoryJDBC();
        LibroRepositoryJDBC libroRepo = new LibroRepositoryJDBC();
        EjemplarRepositoryJDBC ejemplarRepo = new EjemplarRepositoryJDBC();
        PrestamoRepositoryJDBC prestamoRepo = new PrestamoRepositoryJDBC();

        UsuarioService usuarioService = new UsuarioService(usuarioRepo);
        CatalogoService catalogoService = new CatalogoService(libroRepo, ejemplarRepo);

        ProveedorConexion proveedor = ConexionBD::obtenerConexion;

        PrestamoService prestamoService = new PrestamoService(
                usuarioRepo,
                ejemplarRepo,
                prestamoRepo,
                proveedor,
                PoliticaPrestamo.POR_DEFECTO,
                Clock.systemDefaultZone());

        usuarioController = new UsuarioController(usuarioService);
        catalogoController = new CatalogoController(catalogoService);
        prestamoController = new PrestamoController(prestamoService);
    }

    
private static void mostrarMenu() {
    int opcion;

    do {
        Consola.titulo("GESTIÓN DE BIBLIOTECA");

        System.out.println("  CONEXIÓN");
        System.out.println("  1.  Probar conexión a MySQL");

        Consola.separador();
        System.out.println("  USUARIOS");
        System.out.println("  2.  Listar usuarios");
        System.out.println("  3.  Buscar usuario");
        System.out.println("  4.  Registrar usuario");

        Consola.separador();
        System.out.println("  CATÁLOGO Y EJEMPLARES");
        System.out.println("  5.  Listar libros");
        System.out.println("  6.  Buscar libro por ISBN");
        System.out.println("  7.  Consultar ejemplares de un libro");
        System.out.println("  8.  Consultar disponibilidad");

        Consola.separador();
        System.out.println("  PRÉSTAMOS Y DEVOLUCIONES");
        System.out.println("  9.  Registrar préstamo");
        System.out.println("  10. Registrar devolución");
        System.out.println("  11. Listar préstamos activos de un usuario");
        System.out.println("  12. Reporte de préstamos atrasados");

        Consola.separador();
        System.out.println("  ADMINISTRACIÓN DEL CATÁLOGO");
        System.out.println("  13. Registrar libro");
        System.out.println("  14. Registrar ejemplar");
        System.out.println("  15. Cambiar estado de ejemplar");

        Consola.separador();
        System.out.println("  0.  Salir");
        Consola.separador();

        opcion = Consola.leerEntero("  Seleccione una opción: ");

        try {
            switch (opcion) {
                case 1 -> probarConexion();
                case 2 -> listarUsuarios();
                case 3 -> buscarUsuario();
                case 4 -> registrarUsuario();
                case 5 -> listarLibros();
                case 6 -> buscarLibro();
                case 7 -> listarEjemplares();
                case 8 -> consultarDisponibilidad();
                case 9 -> registrarPrestamo();
                case 10 -> registrarDevolucion();
                case 11 -> listarPrestamosActivos();
                case 12 -> reporteAtrasados();
                case 13 -> registrarLibro();
                case 14 -> registrarEjemplar();
                case 15 -> cambiarEstadoEjemplar();
                case 0 -> {
                    Consola.titulo("CIERRE DEL SISTEMA");
                    Consola.info("Gracias por utilizar Gestión de Biblioteca.");
                }
                default -> Consola.error("La opción seleccionada no existe.");
            }
        } catch (RuntimeException e) {
            Consola.error(e.getMessage() == null
                    ? "Ocurrió un error inesperado."
                    : e.getMessage());
        }

        if (opcion != 0) {
            System.out.println();
        }

    } while (opcion != 0);
}

    
    // ---------------------------------------------------------
    // CONEXIÓN
    // ---------------------------------------------------------

    private static void probarConexion() {
        Consola.titulo("PRUEBA DE CONEXIÓN");

        try (Connection con = ConexionBD.obtenerConexion()) {
            DatabaseMetaData meta = con.getMetaData();

            Consola.exito("Conexión establecida correctamente.");
            System.out.println("  Motor:       " + meta.getDatabaseProductName());
            System.out.println("  Versión:     " + meta.getDatabaseProductVersion());
            System.out.println("  Base de datos: " + con.getCatalog());

        } catch (SQLException e) {
            Consola.error("No fue posible conectar con la base de datos.");
            Consola.info(e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // USUARIOS
    // ---------------------------------------------------------

    private static void listarUsuarios() {
        Consola.titulo("LISTA DE USUARIOS");

        List<Usuario> usuarios = usuarioController.listar();

        if (usuarios.isEmpty()) {
            Consola.info("No hay usuarios registrados.");
            return;
        }

        String[][] filas = new String[usuarios.size()][4];

        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);

            filas[i][0] = u.getIdentificacion();
            filas[i][1] = u.getNombre();
            filas[i][2] = u.getCorreo();
            filas[i][3] = u.isActivo() ? "ACTIVO" : "INACTIVO";
        }

        Consola.tabla(new String[]{"Identificación", "Nombre", "Correo", 
            "Estado"}, filas, new int[]{16, 24, 30, 10});

        Consola.info("Total de usuarios: " + usuarios.size());
    }

    private static void buscarUsuario() {
        Consola.titulo("DETALLE DEL USUARIO");

        String id = Consola.leerTexto("Identificación: ");
        Usuario usuario = usuarioController.buscarDetalle(id);

        Consola.separador();
        System.out.println("  INFORMACIÓN PERSONAL");
        Consola.separador();
        System.out.printf("  %-18s %s%n", "Identificación:", usuario.getIdentificacion());
        System.out.printf("  %-18s %s%n", "Nombre:", usuario.getNombre());
        System.out.printf("  %-18s %s%n", "Correo:", usuario.getCorreo());
        System.out.printf("  %-18s %s%n", "Estado:", usuario.isActivo() ? "ACTIVO" : "INACTIVO");
        Consola.separador();
    }


    private static void registrarUsuario() {
        Consola.titulo("REGISTRAR USUARIO");

        String id = Consola.leerTexto("Identificación: ");
        String nombre = Consola.leerTexto("Nombre completo: ");
        String correo = Consola.leerTexto("Correo electrónico: ");

        Consola.mostrarMensaje(usuarioController.registrar(id, nombre, correo));
    }

    // ---------------------------------------------------------
    // LIBROS
    // ---------------------------------------------------------

    private static void listarLibros() {
        Consola.titulo("CATÁLOGO DE LIBROS");

        List<Libro> libros = catalogoController.listarLibros();

        if (libros.isEmpty()) {
            Consola.info("No hay libros registrados.");
            return;
        }

        String[][] filas = new String[libros.size()][4];

        for (int i = 0; i < libros.size(); i++) {
            Libro libro = libros.get(i);

            filas[i][0] = libro.getIsbn();
            filas[i][1] = libro.getTitulo();
            filas[i][2] = libro.getAutor();
            filas[i][3] = String.valueOf(libro.getAnio());
        }

        Consola.tabla(new String[]{"ISBN", "Título", "Autor", "Año"},
                filas, new int[]{19, 28, 24, 6});

        Consola.info("Total de libros: " + libros.size());
    }

    private static void buscarLibro() {
        Consola.titulo("DETALLE DEL LIBRO");

        String isbn = Consola.leerTexto("ISBN: ");
        Libro libro = catalogoController.buscarDetalle(isbn);

        Consola.separador();
        System.out.println("  INFORMACIÓN BIBLIOGRÁFICA");
        Consola.separador();
        System.out.printf("  %-18s %s%n", "ISBN:", libro.getIsbn());
        System.out.printf("  %-18s %s%n", "Título:", libro.getTitulo());
        System.out.printf("  %-18s %s%n", "Autor:", libro.getAutor());
        System.out.printf("  %-18s %d%n", "Año:", libro.getAnio());
        Consola.separador();
    }


    private static void registrarLibro() {
        Consola.titulo("REGISTRAR LIBRO");

        String isbn = Consola.leerTexto("ISBN: ");
        String titulo = Consola.leerTexto("Título: ");
        String autor = Consola.leerTexto("Autor: ");
        int anio = Consola.leerEntero("Año de publicación: ");

        Consola.mostrarMensaje(catalogoController.registrarLibro(isbn, titulo, autor, anio));
    }

    // ---------------------------------------------------------
    // EJEMPLARES Y DISPONIBILIDAD
    // ---------------------------------------------------------

    private static void listarEjemplares() {
        Consola.titulo("EJEMPLARES DE UN LIBRO");

        String isbn = Consola.leerTexto("ISBN del libro: ");
        List<Ejemplar> ejemplares = catalogoController.listarEjemplares(isbn);

        if (ejemplares.isEmpty()) {
            Consola.info("No hay ejemplares registrados para ese libro.");
            return;
        }

        String[][] filas = new String[ejemplares.size()][3];

        for (int i = 0; i < ejemplares.size(); i++) {
            Ejemplar ejemplar = ejemplares.get(i);

            filas[i][0] = ejemplar.getCodigo();
            filas[i][1] = ejemplar.getLibro() == null
                    ? "Sin información"
                    : ejemplar.getLibro().getTitulo();
            filas[i][2] = formatoEstado(ejemplar.getEstado());
        }

        Consola.tabla(new String[]{"Código", "Libro", "Estado"},
                filas, new int[]{16, 35, 15});

        Consola.info("Total de ejemplares: " + ejemplares.size());
    }

    private static void consultarDisponibilidad() {
        Consola.titulo("DISPONIBILIDAD DEL LIBRO");

        String isbn = Consola.leerTexto("ISBN del libro: ");
        int cantidad = catalogoController.consultarCantidadDisponible(isbn);

        Consola.separador();
        System.out.println("  RESUMEN DE DISPONIBILIDAD");
        Consola.separador();
        System.out.printf("  %-24s %s%n", "ISBN:", isbn);
        System.out.printf("  %-24s %d%n", "Ejemplares disponibles:", cantidad);

        if (cantidad > 0) {
            Consola.exito("Hay ejemplares disponibles para préstamo.");
        } 
        else {
            Consola.info("Actualmente no hay ejemplares disponibles.");
        }

        Consola.separador();
    }


    private static void registrarEjemplar() {
        Consola.titulo("REGISTRAR EJEMPLAR");

        String codigo = Consola.leerTexto("Código del ejemplar: ");
        String isbn = Consola.leerTexto("ISBN del libro: ");

        Consola.mostrarMensaje(catalogoController.registrarEjemplar(codigo, isbn));
    }

    private static void cambiarEstadoEjemplar() {
        Consola.titulo("CAMBIAR ESTADO DEL EJEMPLAR");

        String codigo = Consola.leerTexto("Código del ejemplar: ");

        System.out.println();
        System.out.println("  1. DISPONIBLE");
        System.out.println("  2. DAÑADO");

        int opcion = Consola.leerEntero("Nuevo estado: ");
        EstadoEjemplar estado;

        switch (opcion) {
            case 1 -> estado = EstadoEjemplar.DISPONIBLE;
            case 2 -> estado = EstadoEjemplar.DANADO;
            default -> {
                Consola.error("Estado no válido. No se realizaron cambios.");
                return;
            }
        }

        Consola.mostrarMensaje(catalogoController.cambiarEstadoEjemplar(codigo, estado));
    }

    private static String formatoEstado(EstadoEjemplar estado) {
        if (estado == null) {
            return "DESCONOCIDO";
        }

        return switch (estado) {
            case DISPONIBLE -> "DISPONIBLE";
            case PRESTADO -> "PRESTADO";
            case DANADO -> "DAÑADO";
        };
    }

    // ---------------------------------------------------------
    // PRÉSTAMOS Y DEVOLUCIONES
    // ---------------------------------------------------------

    private static void registrarPrestamo() {
        Consola.titulo("REGISTRAR PRÉSTAMO");

        String identificacion = Consola.leerTexto("Identificación del usuario: ");
        String codigo = Consola.leerTexto("Código del ejemplar: ");

        Consola.subtitulo("Resultado de la operación");

        Consola.mostrarMensaje(prestamoController.prestar(identificacion, codigo));
    }

    private static void registrarDevolucion() {
        Consola.titulo("REGISTRAR DEVOLUCIÓN");

        int id = Consola.leerEntero("ID del préstamo: ");

        Consola.subtitulo("Resultado de la operación");

        Consola.mostrarMensaje(prestamoController.devolver(id));
    }

    private static void listarPrestamosActivos() {
        Consola.titulo("PRÉSTAMOS ACTIVOS POR USUARIO");

        String identificacion = Consola.leerTexto("Identificación del usuario: ");

        List<Prestamo> prestamos = prestamoController.listarActivosPorUsuario(identificacion);

        if (prestamos.isEmpty()) {
            Consola.info("El usuario no tiene préstamos activos.");
            return;
        }

        mostrarTablaPrestamos(prestamos);

        Consola.info("Total de préstamos activos: " + prestamos.size());
    }

    private static void reporteAtrasados() {
        Consola.titulo("REPORTE DE PRÉSTAMOS ATRASADOS");

        List<Prestamo> prestamos = prestamoController.reporteAtrasados();

        if (prestamos.isEmpty()) {
            Consola.exito("No hay préstamos atrasados.");
            return;
        }

        mostrarTablaPrestamos(prestamos);

        Consola.info("Total de préstamos atrasados: " + prestamos.size());
    }

    private static void mostrarTablaPrestamos(List<Prestamo> prestamos) {
        String[][] filas = new String[prestamos.size()][7];

        for (int i = 0; i < prestamos.size(); i++) {
            Prestamo p = prestamos.get(i);

            filas[i][0] = String.valueOf(p.getId());
            filas[i][1] = p.getUsuario() == null
                    ? "Sin usuario"
                    : p.getUsuario().getNombre();
            filas[i][2] = p.getEjemplar() == null
                    ? "Sin ejemplar"
                    : p.getEjemplar().getCodigo();
            filas[i][3] = String.valueOf(p.getFechaPrestamo());
            filas[i][4] = String.valueOf(p.getFechaLimite());
            filas[i][5] = p.getFechaDevolucion() == null
                    ? "Pendiente"
                    : p.getFechaDevolucion().toString();
            filas[i][6] = p.getEstado() == null
                    ? "DESCONOCIDO"
                    : p.getEstado().toString();
        }

        Consola.tabla(
                new String[]{
                    "ID", "Usuario", "Ejemplar", "Préstamo",
                    "Fecha límite", "Devolución", "Estado"
                },
                filas,
                new int[]{5, 20, 12, 12, 13, 13, 10});
    }
}
