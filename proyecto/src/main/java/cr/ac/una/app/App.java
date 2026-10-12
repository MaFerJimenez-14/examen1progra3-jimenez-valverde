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
        
        // Configura la salida de la consola en UTF-8 para mostrar correctamente
        // caracteres especiales, como tildes y la letra ñ.
        System.setOut(new java.io.PrintStream(
            System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        System.setErr(new java.io.PrintStream(
            System.err, true, java.nio.charset.StandardCharsets.UTF_8));

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

        PrestamoService prestamoService =
                new PrestamoService(
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
            
            System.out.println("1. Probar conexión a MySQL");
            System.out.println("2. Listar usuarios");
            System.out.println("3. Buscar usuario");
            System.out.println("4. Registrar usuario");
            System.out.println("5. Listar libros");
            System.out.println("6. Buscar libro por ISBN");
            System.out.println("7. Consultar ejemplares de un libro");
            System.out.println("8. Consultar disponibilidad");
            System.out.println("9. Registrar préstamo");
            System.out.println("10. Registrar devolución");
            System.out.println("11. Listar préstamos activos de un usuario");
            System.out.println("12. Reporte de préstamos atrasados");
            System.out.println("13. Registrar libro");
            System.out.println("14. Registrar ejemplar");
            System.out.println("15. Cambiar estado de ejemplar");
            System.out.println("0. Salir");
            
            opcion = Consola.leerEntero("Opción: ");

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
                    case 0 -> System.out.println("Fin del programa.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            if (opcion != 0) {
                System.out.println();
            }
            
        } while (opcion != 0);
    }
    
    private static void probarConexion() {
        try (Connection con = ConexionBD.obtenerConexion()) {
            DatabaseMetaData meta = con.getMetaData();
            
            System.out.println("Conexión exitosa: "
                    + meta.getDatabaseProductName() + " "
                    + meta.getDatabaseProductVersion()
                    + " — base de datos: " + con.getCatalog());
        } 
        catch (SQLException e) {
            System.out.println("No fue posible conectar: " + e.getMessage());
        }
    }

    private static void listarUsuarios() {
        List<Usuario> usuarios = usuarioController.listar();
        
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }    
        usuarios.forEach(System.out::println);
    }
    
    private static void buscarUsuario() {
        String id = Consola.leerTexto("Identificación: ");
        System.out.println(usuarioController.buscar(id));
    }
    
    private static void registrarUsuario() {
        String id = Consola.leerTexto("Identificación: ");
        String nombre = Consola.leerTexto("Nombre: ");
        String correo = Consola.leerTexto("Correo: ");
        
        System.out.println(usuarioController.registrar(id, nombre, correo));
    }
    
    private static void listarLibros() {
        List<Libro> libros = catalogoController.listarLibros();
        
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
            return;
        }
        
        libros.forEach(System.out::println);
    }

    private static void buscarLibro() {
        String isbn = Consola.leerTexto("ISBN: ");
        System.out.println(catalogoController.buscarLibro(isbn));
    }

    private static void listarEjemplares() {
        String isbn = Consola.leerTexto("ISBN del libro: ");
        
        List<Ejemplar> ejemplares = catalogoController.listarEjemplares(isbn);
        
        if (ejemplares.isEmpty()) {
            System.out.println("No hay ejemplares para ese libro.");
            return;
        }
        ejemplares.forEach(System.out::println);
    }

    private static void consultarDisponibilidad() {
        String isbn = Consola.leerTexto("ISBN del libro: ");
        
        System.out.println(catalogoController.consultarDisponibilidad(isbn));
    }

    private static void registrarPrestamo() {
        String identificacion = Consola.leerTexto("Identificación del usuario: ");
        
        String codigo = Consola.leerTexto("Código del ejemplar: ");
        
        System.out.println(prestamoController.prestar(identificacion, codigo));
    }

    private static void registrarDevolucion() {
        int id = Consola.leerEntero("ID del préstamo: ");
        
        System.out.println(prestamoController.devolver(id));
    }

    private static void listarPrestamosActivos() {
        String identificacion = Consola.leerTexto("Identificación del usuario: ");
        
        List<Prestamo> prestamos = prestamoController.listarActivosPorUsuario(identificacion);
        
        if (prestamos.isEmpty()) {
            System.out.println("El usuario no tiene préstamos activos.");
            return;
        }
        
        prestamos.forEach(System.out::println);
    }

    private static void reporteAtrasados() {
        List<Prestamo> prestamos = prestamoController.reporteAtrasados();
        
        if (prestamos.isEmpty()) {
            System.out.println("No hay préstamos atrasados.");
            return;
        }
        prestamos.forEach(System.out::println);
    }

    private static void registrarLibro() {
        String isbn = Consola.leerTexto("ISBN: ");
        String titulo = Consola.leerTexto("Título: ");
        String autor = Consola.leerTexto("Autor: ");
        int anio = Consola.leerEntero("Año de publicación: ");
        
        System.out.println(catalogoController.registrarLibro(isbn, titulo, autor, anio));
    }

    private static void registrarEjemplar() {
        String codigo = Consola.leerTexto("Código del ejemplar: ");
        String isbn = Consola.leerTexto("ISBN del libro: ");
        System.out.println(catalogoController.registrarEjemplar(codigo, isbn));
    }

    private static void cambiarEstadoEjemplar() {
        String codigo = Consola.leerTexto("Código del ejemplar: ");
        
        System.out.println("1. DISPONIBLE");
        System.out.println("2. DANADO");
        
        int opcion = Consola.leerEntero("Nuevo estado: ");
        
        EstadoEjemplar estado;
        
        switch (opcion) {
            case 1 -> estado = EstadoEjemplar.DISPONIBLE;
            case 2 -> estado = EstadoEjemplar.DANADO;
            default -> {
                System.out.println("Estado no válido.");
                return;
            }
        }
        System.out.println(catalogoController.cambiarEstadoEjemplar(codigo, estado));
    }    
}
