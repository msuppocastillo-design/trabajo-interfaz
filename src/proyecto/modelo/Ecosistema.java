package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Contiene y gobierna todas las entidades, ejecuta la lógica de cada turno y calcula
 * los datos del reporte final.
 *
 * Es el modelo: NO imprime, NO lee del usuario y NO usa Swing. Los eventos del turno
 * quedan guardados en una lista para que el controlador los pida y la vista los muestre.
 */
public class Ecosistema {

    // ---- Reglas y rangos de la configuración ----
    public static final int MAX_LOBOS_TOTAL = 5;
    public static final int MIN_PLANTAS = 5,  MAX_PLANTAS = 30;
    public static final int MIN_CONEJOS = 2,  MAX_CONEJOS = 15;
    public static final int MIN_LOBOS = 1,    MAX_LOBOS = 5;
    public static final int MIN_TURNOS = 10,  MAX_TURNOS = 50;
    public static final int INTERVALO_INTERVENCION = 3;

    private final ArrayList<Planta> plantas = new ArrayList<>();
    private final ArrayList<Conejo> conejos = new ArrayList<>();
    private final ArrayList<Lobo> lobos = new ArrayList<>();
    private Clima climaActual;
    private int turnoActual;
    private final int turnosTotales;

    private boolean terminada;
    private String causaFin;
    private boolean intervencionUsada;     // ya intervino en esta pausa de cada 3 turnos
    private int lobosAgregadosHistorico;   // iniciales + intervención, tope 5 en TODA la simulación

    private final Random random = new Random();
    private int contadorPlanta = 0;
    private int contadorConejo = 0;
    private int contadorLobo = 0;

    private final ArrayList<String> eventosTurno = new ArrayList<>();

    // Estadísticas acumuladas para el reporte final
    private final Map<String, Integer> nacimientos = new HashMap<>();
    private final Map<String, Integer> muertes = new HashMap<>();
    private int turnoDeMayorActividad = 0;
    private int cantidadEventosMayorActividad = -1;
    private final List<int[]> historialPoblaciones = new ArrayList<>(); // {turno, plantas, conejos, lobos}

    public Ecosistema(Clima climaInicial, int turnosTotales) {
        this.climaActual = climaInicial;
        this.turnosTotales = turnosTotales;
        this.turnoActual = 0;
        this.terminada = false;
        this.causaFin = "";
        for (String tipo : new String[]{"Planta", "Conejo", "Lobo"}) {
            nacimientos.put(tipo, 0);
            muertes.put(tipo, 0);
        }
    }

    // ---------------- Configuración inicial ----------------

    /** Crea las entidades iniciales con energía aleatoria (plantas 20-40, conejos 30-50, lobos 40-60). */
    public void inicializar(int cantPlantas, int cantConejos, int cantLobos) {
        for (int i = 0; i < cantPlantas; i++) {
            agregarEntidad("planta", 20 + random.nextInt(21));
        }
        for (int i = 0; i < cantConejos; i++) {
            agregarEntidad("conejo", 30 + random.nextInt(21));
        }
        for (int i = 0; i < cantLobos; i++) {
            agregarEntidad("lobo", 40 + random.nextInt(21));
        }
    }

    // ---------------- Nombres ----------------

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

    // ---------------- Agregar entidades ----------------

    /** Sobrecarga 1: energía inicial aleatoria por defecto. */
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

    /** Sobrecarga 2: energía inicial específica. Devuelve false si el tipo no existe o se superó el tope de lobos. */
    public boolean agregarEntidad(String tipo, double energiaInicial) {
        switch (tipo.toLowerCase()) {
            case "planta":
                plantas.add(new Planta(generarNombrePlanta(), energiaInicial, 1 + random.nextInt(5)));
                return true;
            case "conejo":
                conejos.add(new Conejo(generarNombreConejo(), energiaInicial,
                        3 + random.nextInt(5), 1.5 + random.nextDouble()));
                return true;
            case "lobo":
                if (!puedeAgregarLobo()) {
                    return false;
                }
                lobos.add(new Lobo(generarNombreLobo(), energiaInicial,
                        5 + random.nextInt(5), 25 + random.nextDouble() * 10));
                lobosAgregadosHistorico++;
                return true;
            default:
                return false;
        }
    }

    /** Regla: máximo 5 lobos en TODA la simulación (cuenta iniciales + agregados). */
    public boolean puedeAgregarLobo() {
        return lobosAgregadosHistorico < MAX_LOBOS_TOTAL;
    }

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

    /** Usado cuando un conejo se come una planta (la planta muere). */
    public void registrarMuerte(Planta comida) {
        muertes.merge("Planta", 1, Integer::sum);
    }

    /** Guarda un evento del turno (quién comió a quién, quién nació, quién murió). */
    public void registrarEvento(String texto) {
        eventosTurno.add(texto);
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

    /** Procesa un turno completo y devuelve los eventos que ocurrieron. */
    public ArrayList<String> procesarTurno() {
        if (terminada) {
            return new ArrayList<>(eventosTurno);
        }
        turnoActual++;
        eventosTurno.clear();
        intervencionUsada = false;

        // 1) Plantas se reproducen (recorrido polimórfico con Reproducible)
        List<Reproducible> reproductoresPlantas = new ArrayList<>();
        for (Planta p : new ArrayList<>(plantas)) {
            if (p.isViva()) reproductoresPlantas.add(p);
        }
        for (Reproducible r : reproductoresPlantas) {
            r.intentarReproduccion(this);
        }

        // 2) Conejos comen y luego intentan reproducirse
        for (Conejo c : new ArrayList<>(conejos)) {
            if (c.isViva()) c.actuar(this);
        }

        // 3) Lobos intentan cazar un conejo
        for (Lobo l : new ArrayList<>(lobos)) {
            if (l.isViva()) l.actuar(this);
        }

        // 4) Efectos de energía "planos" según el clima
        aplicarEfectosClimaEnergia();

        // 5) Todas las entidades envejecen y gastan energía base
        for (Planta p : plantas) if (p.isViva()) p.envejecer();
        for (Conejo c : conejos) if (c.isViva()) c.envejecer();
        for (Lobo l : lobos) if (l.isViva()) l.envejecer();

        // 6) Las entidades sin energía mueren (Mortal.verificarMuerte, método default)
        for (Conejo c : conejos) {
            if (c.verificarMuerte()) {
                muertes.merge("Conejo", 1, Integer::sum);
                registrarEvento("Conejo '" + c.getNombre() + "' murió (sin energía)");
            }
        }
        for (Lobo l : lobos) {
            if (l.verificarMuerte()) {
                muertes.merge("Lobo", 1, Integer::sum);
                registrarEvento("Lobo '" + l.getNombre() + "' murió (sin energía)");
            }
        }
        for (Planta p : plantas) {
            if (p.isViva() && p.getEnergia() <= 0) {
                p.setViva(false);
                muertes.merge("Planta", 1, Integer::sum);
                registrarEvento("Planta '" + p.getNombre() + "' murió (sin energía)");
            }
        }

        historialPoblaciones.add(new int[]{turnoActual, contarVivas(plantas), contarVivas(conejos), contarVivas(lobos)});

        if (eventosTurno.size() > cantidadEventosMayorActividad) {
            cantidadEventosMayorActividad = eventosTurno.size();
            turnoDeMayorActividad = turnoActual;
        }

        verificarFin();
        return new ArrayList<>(eventosTurno);
    }

    /** Condiciones de fin: colapso (se extinguió alguna población) o turnos completos. */
    private void verificarFin() {
        if (ecosistemaColapsado()) {
            terminada = true;
            causaFin = "Colapso del ecosistema (se extinguió: " + poblacionesExtintas() + ")";
        } else if (turnoActual >= turnosTotales) {
            terminada = true;
            causaFin = "Se alcanzó el número de turnos configurado (" + turnosTotales + ")";
        }
    }

    /** Efectos de energía fijos de cada clima. */
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

    // ---------------- Intervención del jugador (cada 3 turnos) ----------------

    /** Se puede intervenir una vez cada 3 turnos, mientras la simulación no haya terminado. */
    public boolean puedeIntervenir() {
        return !terminada && turnoActual > 0
                && turnoActual % INTERVALO_INTERVENCION == 0 && !intervencionUsada;
    }

    /** Turno en el que se habilita la próxima intervención. */
    public int proximoTurnoIntervencion() {
        return (turnoActual / INTERVALO_INTERVENCION + 1) * INTERVALO_INTERVENCION;
    }

    public boolean intervenirCambiandoClima(Clima nuevo) {
        if (!puedeIntervenir() || nuevo == null) {
            return false;
        }
        this.climaActual = nuevo;
        intervencionUsada = true;
        return true;
    }

    public boolean intervenirAgregando(String tipo) {
        if (!puedeIntervenir()) {
            return false;
        }
        if (agregarEntidad(tipo)) {
            intervencionUsada = true;
            return true;
        }
        return false;
    }

    // ---------------- Consultas ----------------

    private int contarVivas(List<? extends Entidad> lista) {
        int total = 0;
        for (Entidad e : lista) if (e.isViva()) total++;
        return total;
    }

    public int getCantidadPlantasVivas() { return contarVivas(plantas); }
    public int getCantidadConejosVivos() { return contarVivas(conejos); }
    public int getCantidadLobosVivos() { return contarVivas(lobos); }

    /** Todas las entidades vivas (plantas, conejos, lobos), para mostrarlas en la tabla. */
    public ArrayList<Entidad> getEntidadesVivas() {
        ArrayList<Entidad> vivas = new ArrayList<>();
        for (Planta p : plantas) if (p.isViva()) vivas.add(p);
        for (Conejo c : conejos) if (c.isViva()) vivas.add(c);
        for (Lobo l : lobos) if (l.isViva()) vivas.add(l);
        return vivas;
    }

    public ArrayList<String> getEventosTurno() { return new ArrayList<>(eventosTurno); }

    public ArrayList<Planta> getPlantas() { return plantas; }
    public ArrayList<Conejo> getConejos() { return conejos; }
    public ArrayList<Lobo> getLobos() { return lobos; }
    public Clima getClimaActual() { return climaActual; }
    public int getTurnoActual() { return turnoActual; }
    public int getTurnosTotales() { return turnosTotales; }
    public boolean isTerminada() { return terminada; }
    public String getCausaFin() { return causaFin; }
    public boolean isIntervencionUsada() { return intervencionUsada; }
    public int getLobosAgregadosHistorico() { return lobosAgregadosHistorico; }
    public int getLobosRestantes() { return MAX_LOBOS_TOTAL - lobosAgregadosHistorico; }

    public boolean ecosistemaColapsado() {
        return contarVivas(plantas) == 0 || contarVivas(conejos) == 0 || contarVivas(lobos) == 0;
    }

    /** Nombres de las poblaciones extintas, separados por coma ("Ninguna" si no hay). */
    public String poblacionesExtintas() {
        List<String> extintas = new ArrayList<>();
        if (contarVivas(plantas) == 0) extintas.add("Plantas");
        if (contarVivas(conejos) == 0) extintas.add("Conejos");
        if (contarVivas(lobos) == 0) extintas.add("Lobos");
        return extintas.isEmpty() ? "Ninguna" : String.join(", ", extintas);
    }

    // ---------------- Reporte final ----------------

    /** Datos del reporte como filas {concepto, detalle}, listas para mostrar en una tabla. */
    public ArrayList<String[]> generarReporte() {
        ArrayList<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"Causa de finalización", terminada ? causaFin : "La simulación todavía está en curso"});
        filas.add(new String[]{"Turnos jugados", turnoActual + " de " + turnosTotales});
        filas.add(new String[]{"Turno de mayor actividad", turnoDeMayorActividad > 0
                ? turnoDeMayorActividad + " (" + cantidadEventosMayorActividad + " eventos)" : "Sin datos"});

        filas.add(new String[]{"Planta más longeva", masLongeva(plantas)});
        filas.add(new String[]{"Conejo más longevo", masLongeva(conejos)});
        filas.add(new String[]{"Lobo más longevo", masLongeva(lobos)});

        Lobo mejorCazador = null;
        for (Lobo l : lobos) {
            if (mejorCazador == null || l.getExitosCaza() > mejorCazador.getExitosCaza()) {
                mejorCazador = l;
            }
        }
        filas.add(new String[]{"Lobo con más cacerías exitosas", mejorCazador != null
                ? mejorCazador.getNombre() + " (" + mejorCazador.getExitosCaza() + " cacerías)"
                : "No hubo lobos"});

        filas.add(new String[]{"Plantas: nacimientos / muertes", nacimientos.get("Planta") + " / " + muertes.get("Planta")});
        filas.add(new String[]{"Conejos: nacimientos / muertes", nacimientos.get("Conejo") + " / " + muertes.get("Conejo")});
        filas.add(new String[]{"Lobos: nacimientos / muertes", nacimientos.get("Lobo") + " / " + muertes.get("Lobo")});

        // Bonus: máximos y mínimos de población por turno
        filas.add(new String[]{"Plantas: máximo / mínimo", maxMin(1)});
        filas.add(new String[]{"Conejos: máximo / mínimo", maxMin(2)});
        filas.add(new String[]{"Lobos: máximo / mínimo", maxMin(3)});

        // Bonus: ranking de entidades peligrosas
        List<Peligroso> peligrosos = new ArrayList<>(lobos);
        peligrosos.sort((a, b) -> Integer.compare(b.getNivelPeligro(), a.getNivelPeligro()));
        int puesto = 1;
        for (Peligroso p : peligrosos) {
            String nombre = (p instanceof Entidad) ? ((Entidad) p).getNombre() : p.toString();
            filas.add(new String[]{"Ranking de peligro #" + puesto++, nombre + " (nivel " + p.getNivelPeligro() + ")"});
        }
        return filas;
    }

    private String masLongeva(List<? extends Entidad> lista) {
        Entidad mejor = null;
        for (Entidad e : lista) {
            if (mejor == null || e.getEdad() > mejor.getEdad()) {
                mejor = e;
            }
        }
        return mejor != null ? mejor.getNombre() + " (edad: " + mejor.getEdad() + ")" : "No hubo entidades de este tipo";
    }

    private String maxMin(int indice) {
        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE, turnoMax = -1, turnoMin = -1;
        for (int[] fila : historialPoblaciones) {
            if (fila[indice] > max) { max = fila[indice]; turnoMax = fila[0]; }
            if (fila[indice] < min) { min = fila[indice]; turnoMin = fila[0]; }
        }
        if (turnoMax < 0) {
            return "Sin datos";
        }
        return max + " (turno " + turnoMax + ") / " + min + " (turno " + turnoMin + ")";
    }

    // ---------------- Estado para guardar / restaurar (lo usa el DAO) ----------------

    public int getContadorPlanta() { return contadorPlanta; }
    public int getContadorConejo() { return contadorConejo; }
    public int getContadorLobo() { return contadorLobo; }
    public int getNacimientos(String tipo) { return nacimientos.getOrDefault(tipo, 0); }
    public int getMuertes(String tipo) { return muertes.getOrDefault(tipo, 0); }
    public int getTurnoDeMayorActividad() { return turnoDeMayorActividad; }
    public int getCantidadEventosMayorActividad() { return cantidadEventosMayorActividad; }
    public List<int[]> getHistorialPoblaciones() { return new ArrayList<>(historialPoblaciones); }

    /** Vuelve a poner los valores simples que se guardaron en el archivo. */
    public void restaurarEstado(int turnoActual, int lobosAgregadosHistorico, boolean terminada,
                                String causaFin, boolean intervencionUsada,
                                int turnoMayorActividad, int cantidadEventosMayorActividad,
                                int contadorPlanta, int contadorConejo, int contadorLobo) {
        this.turnoActual = turnoActual;
        this.lobosAgregadosHistorico = lobosAgregadosHistorico;
        this.terminada = terminada;
        this.causaFin = causaFin;
        this.intervencionUsada = intervencionUsada;
        this.turnoDeMayorActividad = turnoMayorActividad;
        this.cantidadEventosMayorActividad = cantidadEventosMayorActividad;
        this.contadorPlanta = contadorPlanta;
        this.contadorConejo = contadorConejo;
        this.contadorLobo = contadorLobo;
    }

    public void restaurarContadorEstadistico(String tipo, int nac, int mue) {
        nacimientos.put(tipo, nac);
        muertes.put(tipo, mue);
    }

    public void restaurarHistorial(int[] fila) {
        historialPoblaciones.add(fila);
    }

    public void restaurarEventos(List<String> eventos) {
        eventosTurno.clear();
        eventosTurno.addAll(eventos);
    }

    /** Agrega una entidad ya armada (viva o muerta) a la lista que le corresponde. */
    public void restaurarEntidad(Entidad e) {
        if (e instanceof Planta) {
            plantas.add((Planta) e);
        } else if (e instanceof Conejo) {
            conejos.add((Conejo) e);
        } else if (e instanceof Lobo) {
            lobos.add((Lobo) e);
        }
    }
}
