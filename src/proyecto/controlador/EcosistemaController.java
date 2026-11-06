package controlador;

import dao.DAOException;
import dao.EcosistemaDAO;
import java.util.ArrayList;
import modelo.Clima;
import modelo.Ecosistema;
import modelo.Entidad;
import vista.EcosistemaView;

/**
 * CONTROLADOR: intermediario entre la Vista, el Modelo y el DAO.
 * Valida lo que escribe la persona, aplica la acción en el modelo, guarda con el DAO
 * y le entrega a la Vista los datos actuales. No tiene código Swing.
 *
 *   // →  "voy a otra capa"        // ←  "me vuelve una respuesta"
 */
public class EcosistemaController {

    private Ecosistema modelo;                 // null = todavía no hay simulación en curso
    private final EcosistemaView vista;
    private final EcosistemaDAO dao;

    public EcosistemaController(EcosistemaView vista, EcosistemaDAO dao) {
        this.vista = vista;
        this.dao = dao;
    }

    // ═════════════════════════════════════════════════════════
    //  FLUJO 1 · Arranque: retomar la simulación guardada (si hay)
    // ═════════════════════════════════════════════════════════
    public void iniciar() {
        try {
            modelo = dao.cargar();                 // → DAO / ← Ecosistema armado, o null si no hay archivo
        } catch (DAOException e) {
            modelo = null;
            vista.mostrarError("No se pudo retomar la simulación guardada: " + e.getMessage()
                    + "\nSe empieza una simulación nueva.");
        }

        refrescarVista();
        if (modelo == null) {
            vista.mostrarPantalla("config");
        } else {
            vista.mostrarPantalla("simulacion");
            vista.mostrarMensaje("Simulación retomada en el turno " + modelo.getTurnoActual() + ".");
        }
    }

    // ═════════════════════════════════════════════════════════
    //  FLUJO 2 · Configuración inicial (todo llega como texto)
    // ═════════════════════════════════════════════════════════
    public void iniciarSimulacion(String plantasTxt, String conejosTxt, String lobosTxt,
                                  String climaTxt, String turnosTxt) {
        Integer plantas = leerEntero(plantasTxt, "plantas", Ecosistema.MIN_PLANTAS, Ecosistema.MAX_PLANTAS);
        if (plantas == null) return;
        Integer conejos = leerEntero(conejosTxt, "conejos", Ecosistema.MIN_CONEJOS, Ecosistema.MAX_CONEJOS);
        if (conejos == null) return;
        Integer lobos = leerEntero(lobosTxt, "lobos", Ecosistema.MIN_LOBOS, Ecosistema.MAX_LOBOS);
        if (lobos == null) return;
        Integer turnos = leerEntero(turnosTxt, "turnos totales", Ecosistema.MIN_TURNOS, Ecosistema.MAX_TURNOS);
        if (turnos == null) return;

        Clima clima = Clima.desdeTexto(climaTxt);
        if (clima == null) {
            vista.mostrarError("Elegí un clima inicial.");
            return;
        }

        modelo = new Ecosistema(clima, turnos);        // → Modelo
        modelo.inicializar(plantas, conejos, lobos);   // → Modelo crea las entidades iniciales

        guardarCambios();
        refrescarVista();
        vista.limpiarFormulario();
        vista.mostrarPantalla("simulacion");
        vista.mostrarMensaje("Simulación creada: " + plantas + " plantas, " + conejos + " conejos, "
                + lobos + " lobos. Presioná «Avanzar turno».");
    }

    // ═════════════════════════════════════════════════════════
    //  FLUJO 3 · Avanzar turno
    // ═════════════════════════════════════════════════════════
    public void avanzarTurno() {
        if (!hayEnCurso()) return;

        modelo.procesarTurno();     // ② → Modelo procesa el turno (guarda sus eventos)
        guardarCambios();           // ④ → DAO guarda el estado
        refrescarVista();           // ⑥ → Vista: tabla, contadores, eventos, intervención

        if (modelo.isTerminada()) { // ⑦ → fin por turnos o por colapso
            vista.mostrarMensaje("La simulación terminó: " + modelo.getCausaFin());
            irAlReporte();
        } else {
            vista.mostrarMensaje("Turno " + modelo.getTurnoActual() + " procesado.");
        }
    }

    // ═════════════════════════════════════════════════════════
    //  FLUJO 4 · Intervención (cada 3 turnos)
    // ═════════════════════════════════════════════════════════
    public void cambiarClima(String climaTxt) {
        if (!hayEnCurso() || !puedeIntervenirOAvisar()) return;

        Clima nuevo = Clima.desdeTexto(climaTxt);
        if (nuevo == null) {
            vista.mostrarError("Elegí un clima.");
            return;
        }
        if (nuevo == modelo.getClimaActual()) {
            vista.mostrarError("Ese ya es el clima actual (" + nuevo.getNombreLegible() + ").");
            return;
        }

        modelo.intervenirCambiandoClima(nuevo);   // → Modelo aplica la regla
        guardarCambios();
        refrescarVista();
        vista.mostrarMensaje("El clima cambió a " + nuevo.getNombreLegible() + ".");
    }

    public void agregarEntidad(String tipo) {
        if (!hayEnCurso() || !puedeIntervenirOAvisar()) return;

        if (tipo == null || tipo.trim().isEmpty()) {
            vista.mostrarError("Elegí qué entidad agregar (planta, conejo o lobo).");
            return;
        }
        if (tipo.trim().equalsIgnoreCase("lobo") && !modelo.puedeAgregarLobo()) {
            vista.mostrarError("No se pueden agregar más lobos: ya se alcanzó el máximo de "
                    + Ecosistema.MAX_LOBOS_TOTAL + " en toda la simulación.");
            return;
        }

        if (!modelo.intervenirAgregando(tipo.trim())) {   // → Modelo
            vista.mostrarError("No se pudo agregar la entidad '" + tipo + "'.");
            return;
        }
        guardarCambios();
        refrescarVista();
        vista.mostrarMensaje("Se agregó un nuevo " + tipo.trim().toLowerCase() + " al ecosistema.");
    }

    // ═════════════════════════════════════════════════════════
    //  FLUJO 5 · Nueva simulación / navegación / reporte
    // ═════════════════════════════════════════════════════════
    public void nuevaSimulacion() {
        if (modelo != null && !modelo.isTerminada()) {
            if (!vista.confirmar("Hay una simulación en curso. ¿Querés descartarla y empezar una nueva?")) {
                return;
            }
        }
        modelo = null;
        if (!dao.eliminar()) {
            vista.mostrarError("No se pudo borrar la simulación guardada. Se sobrescribirá al empezar la nueva.");
        }
        refrescarVista();
        vista.limpiarFormulario();
        vista.mostrarPantalla("config");
    }

    public void verReporte() {
        if (modelo == null) {
            vista.mostrarError("Todavía no hay una simulación. Configurá una nueva.");
            return;
        }
        if (!modelo.isTerminada()) {
            vista.mostrarError("El reporte está disponible cuando termina la simulación.");
            return;
        }
        irAlReporte();
    }

    public void irASimulacion() {
        vista.mostrarPantalla("simulacion");
    }

    // ═════════════════════════════════════════════════════════
    //  Auxiliares (privados)
    // ═════════════════════════════════════════════════════════

    private void irAlReporte() {
        vista.actualizarReporte(modelo.generarReporte());   // ← Modelo calcula, → Vista muestra
        vista.mostrarPantalla("reporte");
    }

    /** Le pasa a la Vista los datos ACTUALES. La Vista nunca los busca sola. */
    private void refrescarVista() {
        if (modelo == null) {
            vista.actualizarContadores(0, 0, "-", 0, 0, 0);
            vista.actualizarTabla(new ArrayList<String[]>());
            vista.actualizarEventos(new ArrayList<String>());
            vista.actualizarIntervencion("Todavía no hay una simulación en curso.", false, Ecosistema.MAX_LOBOS_TOTAL);
            vista.habilitarControles(false, false);
            return;
        }

        vista.actualizarContadores(modelo.getTurnoActual(), modelo.getTurnosTotales(),
                modelo.getClimaActual().getNombreLegible(),
                modelo.getCantidadPlantasVivas(), modelo.getCantidadConejosVivos(), modelo.getCantidadLobosVivos());

        ArrayList<String[]> filas = new ArrayList<>();
        for (Entidad e : modelo.getEntidadesVivas()) {
            filas.add(new String[]{e.getTipo(), e.getNombre(), String.valueOf(e.getEdad()),
                String.format("%.1f", e.getEnergia()), e.getDetalle()});
        }
        vista.actualizarTabla(filas);
        vista.actualizarEventos(modelo.getEventosTurno());
        vista.actualizarIntervencion(textoIntervencion(), modelo.puedeIntervenir(), modelo.getLobosRestantes());
        vista.habilitarControles(!modelo.isTerminada(), modelo.isTerminada());
    }

    private String textoIntervencion() {
        if (modelo.isTerminada()) {
            return "La simulación terminó: ya no se puede intervenir.";
        }
        if (modelo.puedeIntervenir()) {
            return "Podés intervenir ahora (cambiar clima o agregar una entidad) o simplemente avanzar.";
        }
        if (modelo.getTurnoActual() > 0 && modelo.getTurnoActual() % Ecosistema.INTERVALO_INTERVENCION == 0) {
            return "Ya usaste tu intervención de este turno. Avanzá al siguiente.";
        }
        return "Próxima intervención al terminar el turno " + modelo.proximoTurnoIntervencion() + ".";
    }

    private boolean hayEnCurso() {
        if (modelo == null) {
            vista.mostrarError("Todavía no hay una simulación en curso. Configurá una nueva.");
            return false;
        }
        if (modelo.isTerminada()) {
            vista.mostrarError("La simulación ya terminó. Podés ver el reporte o empezar una nueva.");
            return false;
        }
        return true;
    }

    private boolean puedeIntervenirOAvisar() {
        if (!modelo.puedeIntervenir()) {
            vista.mostrarError("Solo se puede intervenir una vez cada " + Ecosistema.INTERVALO_INTERVENCION
                    + " turnos. " + textoIntervencion());
            return false;
        }
        return true;
    }

    /** Valida campo vacío, formato y rango. Devuelve null (y avisa a la Vista) si algo está mal. */
    private Integer leerEntero(String texto, String campo, int min, int max) {
        if (texto == null || texto.trim().isEmpty()) {
            vista.mostrarError("La cantidad de " + campo + " es obligatoria.");
            return null;
        }
        int valor;
        try {
            valor = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            vista.mostrarError("La cantidad de " + campo + " tiene que ser un número entero.");
            return null;
        }
        if (valor < min || valor > max) {
            vista.mostrarError("La cantidad de " + campo + " tiene que estar entre " + min + " y " + max + ".");
            return null;
        }
        return valor;
    }

    private void guardarCambios() {
        if (!dao.guardar(modelo)) {   // → DAO / ← true o false
            vista.mostrarError("No se pudo guardar la simulación en el archivo.");
        }
    }
}
