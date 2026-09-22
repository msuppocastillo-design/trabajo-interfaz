import java.util.Random;
import java.util.Scanner;

/**
 * Punto de entrada: arma la configuración inicial, corre el loop de turnos
 * y maneja la intervención del jugador cada 3 turnos.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   SIMULADOR DE ECOSISTEMA POR TURNOS");
        System.out.println("=========================================\n");

        Ecosistema eco = configurarEcosistema();
        int turnosTotales = pedirEntero("Cantidad de turnos totales de la simulación (10-50): ", 10, 50);

        System.out.println("\n¿Confirmar configuración e iniciar la simulación? (s/n)");
        if (!leerConfirmacion()) {
            System.out.println("Simulación cancelada.");
            return;
        }

        String causaFin = null;

        for (int t = 1; t <= turnosTotales; t++) {
            eco.procesarTurno();

            if (eco.ecosistemaColapsado()) {
                causaFin = "Colapso del ecosistema (se extinguieron: " + eco.poblacionExtinta() + ")";
                break;
            }

            System.out.println(">>> Presione Enter para continuar...");
            scanner.nextLine();

            // Intervención del jugador cada 3 turnos
            if (eco.getTurnoActual() % 3 == 0 && eco.getTurnoActual() != turnosTotales) {
                menuIntervencion(eco);
            }
        }

        if (causaFin == null) {
            causaFin = "Se alcanzó el número de turnos configurado (" + turnosTotales + ")";
        }

        eco.generarReporteFinal(causaFin);
    }

    // ---------------- Configuración inicial ----------------

    private static Ecosistema configurarEcosistema() {
        System.out.println("--- Configuración inicial ---");
        int cantPlantas = pedirEntero("Cantidad inicial de plantas (5-30): ", 5, 30);
        int cantConejos = pedirEntero("Cantidad inicial de conejos (2-15): ", 2, 15);
        int cantLobos = pedirEntero("Cantidad inicial de lobos (1-5): ", 1, 5);
        Clima climaInicial = pedirClima();

        Ecosistema eco = new Ecosistema(climaInicial);

        for (int i = 0; i < cantPlantas; i++) {
            eco.agregarEntidad("planta", 20 + random.nextInt(21)); // 20-40
        }
        for (int i = 0; i < cantConejos; i++) {
            eco.agregarEntidad("conejo", 30 + random.nextInt(21)); // 30-50
        }
        for (int i = 0; i < cantLobos; i++) {
            eco.agregarEntidad("lobo", 40 + random.nextInt(21)); // 40-60
        }

        System.out.println("\nConfiguración creada: " + cantPlantas + " plantas, " + cantConejos
                + " conejos, " + cantLobos + " lobos. Clima inicial: " + climaInicial.getNombreLegible());
        return eco;
    }

    private static Clima pedirClima() {
        while (true) {
            System.out.println("Clima inicial: 1) Soleado  2) Lluvioso  3) Sequía  4) Invierno");
            System.out.print("Opción: ");
            String linea = scanner.nextLine().trim();
            switch (linea) {
                case "1": return Clima.SOLEADO;
                case "2": return Clima.LLUVIOSO;
                case "3": return Clima.SEQUIA;
                case "4": return Clima.INVIERNO;
                default: System.out.println("Opción inválida, ingrese un número del 1 al 4.");
            }
        }
    }

    private static int pedirEntero(String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            String linea = scanner.nextLine().trim();
            try {
                int valor = Integer.parseInt(linea);
                if (valor < min || valor > max) {
                    System.out.println("Valor fuera de rango (" + min + "-" + max + "). Intente de nuevo.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    private static boolean leerConfirmacion() {
        while (true) {
            String linea = scanner.nextLine().trim().toLowerCase();
            if (linea.equals("s") || linea.equals("si")) return true;
            if (linea.equals("n") || linea.equals("no")) return false;
            System.out.print("Responda 's' o 'n': ");
        }
    }

    // ---------------- Intervención del jugador ----------------

    private static void menuIntervencion(Ecosistema eco) {
        System.out.println("\n=== INTERVENCIÓN (cada 3 turnos) ===");
        System.out.println("1. Cambiar clima (actual: " + eco.getClimaActual().getNombreLegible() + ")");
        System.out.println("2. Agregar entidad");
        System.out.println("3. Solo avanzar");
        System.out.print("Opción: ");
        String opcion = scanner.nextLine().trim();

        switch (opcion) {
            case "1": {
                Clima nuevo = pedirClima();
                System.out.println("¿Confirmar cambio de clima a " + nuevo.getNombreLegible() + "? (s/n)");
                if (leerConfirmacion()) {
                    eco.cambiarClima(nuevo);
                }
                break;
            }
            case "2": {
                System.out.print("¿Qué entidad agregar? (planta/conejo/lobo): ");
                String tipo = scanner.nextLine().trim().toLowerCase();
                if (!tipo.equals("planta") && !tipo.equals("conejo") && !tipo.equals("lobo")) {
                    System.out.println("Tipo inválido, no se agregó nada.");
                    break;
                }
                if (tipo.equals("lobo") && eco.getLobosAgregadosHistorico() >= Ecosistema.MAX_LOBOS_TOTAL) {
                    System.out.println("No se puede: ya se alcanzó el máximo de "
                            + Ecosistema.MAX_LOBOS_TOTAL + " lobos en toda la simulación.");
                    break;
                }
                System.out.println("¿Confirmar agregar " + tipo + "? (s/n)");
                if (leerConfirmacion()) {
                    boolean ok = eco.agregarEntidad(tipo);
                    if (ok) {
                        System.out.println("Se agregó una nueva entidad de tipo '" + tipo + "' al ecosistema.");
                    }
                }
                break;
            }
            default:
                System.out.println("Se avanza sin intervenir.");
        }
    }
}
