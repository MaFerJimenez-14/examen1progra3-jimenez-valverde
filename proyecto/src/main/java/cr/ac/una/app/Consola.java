package cr.ac.una.app;

import java.util.Scanner;

/**
 * Utilidades mínimas para leer datos desde la consola.
 *
 * Solo resuelve la lectura de texto y números. Las validaciones propias
 * del negocio (rangos, duplicados, estados, fechas, etc.) NO van aquí:
 * pertenecen a la capa service.
 */
public final class Consola {

    private static final Scanner ENTRADA = new Scanner(System.in);
    private static final int ANCHO_TITULO = 72;
    
    private Consola() {
    }

    /** Lee una línea de texto (puede ser vacía). */
    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return ENTRADA.nextLine().trim();
    }

    /** Lee un número entero; repite la solicitud mientras el valor no sea válido. */
    public static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Digite un número entero válido.");
            }
        }
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(ANCHO_TITULO));
        System.out.println("  " + texto);
        System.out.println("=".repeat(ANCHO_TITULO));
    }
    
    /** Muestra un subtítulo. */
    public static void subtitulo(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    }
    
    public static void separador(){
        System.out.println("-".repeat(ANCHO_TITULO));
    }

    /** Muestra un mensaje de operación exitosa. */
    public static void exito(String mensaje) {
        System.out.println("[OK] " + mensaje);
    }

    /** Muestra un mensaje informativo. */
    public static void info(String mensaje) {
        System.out.println("[INFO] " + mensaje);
    }

    /** Muestra un mensaje de advertencia o error. */
    public static void error(String mensaje) {
        System.out.println("[!] " + mensaje);
    }

    public static void mostrarMensaje(String mensaje){
        if (mensaje == null || mensaje.isBlank()) {
            info("Operación finalizada.");
            return;
        }
        System.out.println();
        System.out.println(mensaje);
    }
    
    /** Imprime una tabla con encabezados y filas de texto. */
    public static void tabla(String[] encabezados, String[][] filas, int[] anchos) {
        if (encabezados == null || filas == null || anchos == null
                || encabezados.length != anchos.length) {
            throw new IllegalArgumentException("Los encabezados y los anchos deben coincidir.");
        }

        for (String[] fila : filas) {
            if (fila == null || fila.length != encabezados.length) {
                throw new IllegalArgumentException("Todas las filas deben tener las mismas columnas.");
            }
        }

        imprimirSeparador(anchos);
        imprimirFila(encabezados, anchos);
        imprimirSeparador(anchos);

        for (String[] fila : filas) {
            imprimirFila(fila, anchos);
        }

        imprimirSeparador(anchos);
    }

    private static void imprimirFila(String[] valores, int[] anchos) {
        for (int i = 0; i < valores.length; i++) {
            String valor = valores[i] == null ? "" : valores[i];
            int ancho = anchos[i];

            if (valor.length() > ancho) {
                valor = ancho > 3
                        ? valor.substring(0, ancho - 3) + "..."
                        : valor.substring(0, ancho);
            }

            System.out.printf("| %-" + ancho + "s ", valor);
        }
        System.out.println("|");
    }

    private static void imprimirSeparador(int[] anchos) {
        for (int ancho : anchos) {
            System.out.print("+-" + "-".repeat(ancho) + "-");
        }
        System.out.println("+");
    }
    
    /* Espera a que la persona presione Enter antes de continuar. */
    public static void pausar() {
        System.out.println();
        System.out.print("Presione Enter para continuar...");
        ENTRADA.nextLine();
    }
}
