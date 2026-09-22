import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Contiene y gobierna todas las entidades del ecosistema, y ejecuta la
 * lógica de cada turno en el orden pedido por la consigna.
 */
public class Ecosistema {

    private ArrayList<Planta> plantas;
    private ArrayList<Conejo> conejos;
    private ArrayList<Lobo> lobos;
    private Clima climaActual;
    private int turnoActual;

    // --- límites y contadores para el reporte final / reglas del juego ---
    public static final int MAX_LOBOS_TOTAL = 5;
    private int lobosAgregadosHistorico; // cuenta iniciales + intervención, tope 5 en TODA la simulación

    private final Random random = new Random();
    private int contadorPlanta = 0;
    private int contadorConejo = 0;
    private int contadorLobo = 0;

    private int eventosTurnoActual;

    // Estadísticas acumuladas para el reporte final
    private final Map<String, Integer> nacimientos = new HashMap<>();
    private final Map<String, Integer> muertes = new HashMap<>();
    private int turnoDeMayorActividad = 0;
    private int cantidadEventosMayorActividad = -1;

    // Bonus: historial de poblaciones turno a turno (para máximos/mínimos)
    private final List<int[]> historialPoblaciones = new ArrayList<>(); // {turno, plantas, conejos, lobos}

    public Ecosistema(Clima climaInicial) {
        this.plantas = new ArrayList<>();
        this.conejos = new ArrayList<>();
        this.lobos = new ArrayList<>();
        this.climaActual = climaInicial;
        this.turnoActual = 0;
        this.lobosAgregadosHistorico = 0;
        nacimientos.put("Planta", 0);
        nacimientos.put("Conejo", 0);
        nacimientos.put("Lobo", 0);
        muertes.put("Planta", 0);
        muertes.put("Conejo", 0);
        muertes.put("Lobo", 0);
    }

    // ---------------- Generación de nombres únicos ----------------

    public String generarNombrePlanta() {
        return "Helecho-" + (++contadorPlanta);
    }

    public String generarNombreConejo() {
        String[] base = {"Blas", "Luna", "Rex", "Topo", "Nube", "Coco", "Pipa", "Kira"};
        return base[random.nextInt(base.length)] + "-" + (++contadorConejo);
    }

    public String generarNombreLobo() {
        String[] base = {"Fang", "Sombra", "Kaiser", "Yuki", "Bruma"};
        return base[random.nextInt(base.length)] + "-" + (++contadorLobo);
    }

    // ---------------- Alta de entidades (config inicial + intervención) ----------------

    /** Sobrecarga 1: agrega una entidad con energía inicial aleatoria por defecto. */
    public boolean agregarEntidad(String tipo) {
        double energiaDefault;
        switch (tipo.toLowerCase()) {
            case "planta": energiaDefault = 25 + random.nextInt(21); break; // 25-45
            case "conejo": energiaDefault = 35 + random.nextInt(21); break; // 35-55
            case "lobo":   energiaDefault = 45 + random.nextInt(21); break; // 45-65
            default: return false;
        }
        return agregarEntidad(tipo, energiaDefault);
    }

    /** Sobrecarga 2: agrega una entidad con energía inicial específica. */
    public boolean agregarEntidad(String tipo, double energiaInicial) {
        switch (tipo.toLowerCase()) {
            case "planta": {
                int tamanio = 1 + random.nextInt(5);
                Planta p = new Planta(generarNombrePlanta(), energiaInicial, tamanio);
                plantas.add(p);
                return true;
            }
            case "conejo": {
                Conejo c = new Conejo(generarNombreConejo(), energiaInicial, 3 + random.nextInt(5), 1.5 + random.nextDouble());
                conejos.add(c);
                return true;
            }
            case "lobo": {
                if (lobosAgregadosHistorico >= MAX_LOBOS_TOTAL) {
                    System.out.println("No se puede agregar más lobos: ya se alcanzó el máximo de "
                            + MAX_LOBOS_TOTAL + " en toda la simulación.");
                    return false;
                }
                Lobo l = new Lobo(generarNombreLobo(), energiaInicial, 5 + random.nextInt(5), 25 + random.nextDouble() * 10);
                lobos.add(l);
                lobosAgregadosHistorico++;
                return true;
            }
            default:
                System.out.println("Tipo de entidad desconocido: " + tipo);
                return false;
        }
    }

    /** Usado por reproducción de plantas/conejos: registra el nacimiento y agrega la entidad. */
    public void registrarNacimiento(Planta hija) {
        plantas.add(hija);
        nacimientos.merge("Planta", 1, Integer::sum);
    }

    public void registrarNacimiento(Conejo hijo) {
        conejos.add(hijo);
        nacimientos.merge("Conejo", 1, Integer::sum);
    }

    /** Usado cuando un lobo caza exitosamente a un conejo. */
    public void registrarMuerte(Conejo victima) {
        muertes.merge("Conejo", 1, Integer::sum);
    }

    public void contarEvento() {
        eventosTurnoActual++;
    }

    // ---------------- Búsquedas para comer / cazar ----------------

    public Planta buscarPlantaVivaAleatoria() {
        List<Planta> vivas = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.isViva()) vivas.add(p);
        }
        if (vivas.isEmpty()) return null;
        return vivas.get(random.nextInt(vivas.size()));
    }

    public Conejo buscarConejoVivoAleatorio() {
        List<Conejo> vivos = new ArrayList<>();
        for (Conejo c : conejos) {
            if (c.isViva()) vivos.add(c);
        }
        if (vivos.isEmpty()) return null;
        return vivos.get(random.nextInt(vivos.size()));
    }

    // ---------------- Turno principal ----------------

    public void procesarTurno() {
        turnoActual++;
        eventosTurnoActual = 0;

        System.out.println("\n=== TURNO " + turnoActual + " | Clima: " + climaActual.getNombreLegible() + " ===");
        System.out.println("Plantas: " + contarVivas(plantas) + "  Conejos: " + contarVivas(conejos)
                + "  Lobos: " + contarVivas(lobos));
        System.out.println("-- Eventos --");

        // 1) Plantas se reproducen (usando el mismo recorrido polimórfico ArrayList<Reproducible>)
        List<Reproducible> reproductoresPlantas = new ArrayList<>();
        for (Planta p : new ArrayList<>(plantas)) {
            if (p.isViva()) reproductoresPlantas.add(p);
        }
        for (Reproducible r : reproductoresPlantas) {
            r.intentarReproduccion(this);
        }

        // 2) Conejos comen y luego intentan reproducirse (mismo mecanismo polimórfico)
        for (Conejo c : new ArrayList<>(conejos)) {
            if (c.isViva()) {
                c.actuar(this);
            }
        }

        // 3) Lobos intentan cazar un conejo
        for (Lobo l : new ArrayList<>(lobos)) {
            if (l.isViva()) {
                l.actuar(this);
            }
        }

        // 4) Efectos de energía "planos" según el clima
        aplicarEfectosClimaEnergia();

        // 5) Todas las entidades envejecen y gastan energía base por existir
        for (Planta p : plantas) if (p.isViva()) p.envejecer();
        for (Conejo c : conejos) if (c.isViva()) c.envejecer();
        for (Lobo l : lobos) if (l.isViva()) l.envejecer();

        // 6) Entidades sin energía mueren (usando Mortal.verificarMuerte(), método default)
        for (Conejo c : conejos) {
            if (c.isViva()) {
                c.verificarMuerte();
                if (!c.isViva()) {
                    muertes.merge("Conejo", 1, Integer::sum);
                    eventosTurnoActual++;
                }
            }
        }
        for (Lobo l : lobos) {
            if (l.isViva()) {
                l.verificarMuerte();
                if (!l.isViva()) {
                    muertes.merge("Lobo", 1, Integer::sum);
                    eventosTurnoActual++;
                }
            }
        }
        // Las plantas no implementan Mortal en esta versión: mueren directamente al ser
        // comidas (serComida) o simplemente no vuelven a reproducirse si su energía cae a 0.
        for (Planta p : plantas) {
            if (p.isViva() && p.getEnergia() <= 0) {
                p.setViva(false);
                muertes.merge("Planta", 1, Integer::sum);
                eventosTurnoActual++;
            }
        }

        System.out.println("Estado: Plantas: " + contarVivas(plantas) + "  Conejos: " + contarVivas(conejos)
                + "  Lobos: " + contarVivas(lobos));

        // Bonus: guardar historial de poblaciones
        historialPoblaciones.add(new int[]{turnoActual, contarVivas(plantas), contarVivas(conejos), contarVivas(lobos)});

        if (eventosTurnoActual > cantidadEventosMayorActividad) {
            cantidadEventosMayorActividad = eventosTurnoActual;
            turnoDeMayorActividad = turnoActual;
        }
    }

    /** Aplica los efectos de energía "fijos" de cada clima (tabla de la consigna). */
    private void aplicarEfectosClimaEnergia() {
        switch (climaActual) {
            case SOLEADO:
                for (Conejo c : conejos) if (c.isViva()) c.setEnergia(c.getEnergia() + 5);
                break;
            case LLUVIOSO:
                for (Conejo c : conejos) if (c.isViva()) c.setEnergia(c.getEnergia() + 3);
                for (Lobo l : lobos) if (l.isViva()) l.setEnergia(l.getEnergia() - 5);
                break;
            case SEQUIA:
                for (Conejo c : conejos) if (c.isViva()) c.setEnergia(c.getEnergia() - 5);
                break;
            case INVIERNO:
                for (Conejo c : conejos) if (c.isViva()) c.setEnergia(c.getEnergia() - 8);
                break;
        }
    }

    private int contarVivas(List<? extends Entidad> lista) {
        int total = 0;
        for (Entidad e : lista) if (e.isViva()) total++;
        return total;
    }

    // ---------------- Getters / intervención ----------------

    public ArrayList<Planta> getPlantas() { return plantas; }
    public ArrayList<Conejo> getConejos() { return conejos; }
    public ArrayList<Lobo> getLobos() { return lobos; }
    public Clima getClimaActual() { return climaActual; }
    public int getTurnoActual() { return turnoActual; }
    public int getLobosAgregadosHistorico() { return lobosAgregadosHistorico; }

    public void cambiarClima(Clima nuevo) {
        this.climaActual = nuevo;
        System.out.println("El clima cambió a: " + nuevo.getNombreLegible());
    }

    public boolean ecosistemaColapsado() {
        return contarVivas(plantas) == 0 || contarVivas(conejos) == 0 || contarVivas(lobos) == 0;
    }

    /** Indica cuál población se extinguió (para el reporte final). */
    public String poblacionExtinta() {
        if (contarVivas(plantas) == 0) return "Plantas";
        if (contarVivas(conejos) == 0) return "Conejos";
        if (contarVivas(lobos) == 0) return "Lobos";
        return "Ninguna";
    }

    public void mostrarEstado() {
        System.out.println("Plantas: " + contarVivas(plantas) + "  Conejos: " + contarVivas(conejos)
                + "  Lobos: " + contarVivas(lobos) + "  Clima: " + climaActual.getNombreLegible());
    }

    // ---------------- Reporte final ----------------

    public void generarReporteFinal(String causaFin) {
        System.out.println("\n================= REPORTE FINAL =================");
        System.out.println("Causa de finalización: " + causaFin);
        System.out.println("Turnos jugados: " + turnoActual);
        System.out.println("Turno de mayor actividad: " + turnoDeMayorActividad
                + " (" + cantidadEventosMayorActividad + " eventos)");

        System.out.println("\n-- Entidad más longeva por tipo --");
        imprimirMasLongeva("Planta", plantas);
        imprimirMasLongeva("Conejo", conejos);
        imprimirMasLongeva("Lobo", lobos);

        System.out.println("\n-- Lobo con más cacerías exitosas --");
        Lobo mejorCazador = null;
        for (Lobo l : lobos) {
            if (mejorCazador == null || l.getExitosCaza() > mejorCazador.getExitosCaza()) {
                mejorCazador = l;
            }
        }
        if (mejorCazador != null) {
            System.out.println(mejorCazador.getNombre() + " (" + mejorCazador.getExitosCaza() + " cacerías)");
        } else {
            System.out.println("No hubo lobos en la simulación.");
        }

        System.out.println("\n-- Nacimientos y muertes por tipo --");
        for (String tipo : new String[]{"Planta", "Conejo", "Lobo"}) {
            System.out.println(tipo + " -> nacimientos: " + nacimientos.get(tipo)
                    + ", muertes: " + muertes.get(tipo));
        }

        // Bonus: máximos y mínimos de población por turno
        System.out.println("\n-- (Bonus) Máximos y mínimos de población por turno --");
        imprimirMaxMin("Plantas", 1);
        imprimirMaxMin("Conejos", 2);
        imprimirMaxMin("Lobos", 3);

        // Bonus: ranking de elementos peligrosos
        System.out.println("\n-- (Bonus) Elementos peligrosos, ordenados por nivel --");
        List<Peligroso> peligrosos = new ArrayList<>();
        peligrosos.addAll(lobos);
        for (Planta p : plantas) {
            if (p instanceof Peligroso) peligrosos.add((Peligroso) p);
        }
        peligrosos.sort((a, b) -> Integer.compare(b.getNivelPeligro(), a.getNivelPeligro()));
        for (Peligroso p : peligrosos) {
            String nombre = (p instanceof Entidad) ? ((Entidad) p).getNombre() : p.toString();
            System.out.println("- " + nombre + " (nivel de peligro: " + p.getNivelPeligro() + ")");
        }

        System.out.println("===================================================");
    }

    private void imprimirMasLongeva(String tipo, List<? extends Entidad> lista) {
        Entidad masLongeva = null;
        for (Entidad e : lista) {
            if (masLongeva == null || e.getEdad() > masLongeva.getEdad()) {
                masLongeva = e;
            }
        }
        if (masLongeva != null) {
            System.out.println(tipo + ": " + masLongeva.getNombre() + " (edad: " + masLongeva.getEdad() + ")");
        } else {
            System.out.println(tipo + ": no hubo entidades de este tipo.");
        }
    }

    private void imprimirMaxMin(String etiqueta, int indice) {
        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE, turnoMax = -1, turnoMin = -1;
        for (int[] fila : historialPoblaciones) {
            if (fila[indice] > max) { max = fila[indice]; turnoMax = fila[0]; }
            if (fila[indice] < min) { min = fila[indice]; turnoMin = fila[0]; }
        }
        if (turnoMax >= 0) {
            System.out.println(etiqueta + " -> máximo: " + max + " (turno " + turnoMax
                    + "), mínimo: " + min + " (turno " + turnoMin + ")");
        }
    }
}
