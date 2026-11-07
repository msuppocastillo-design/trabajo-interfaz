package proyecto.modelo;

import java.util.List;
import java.util.Random;

/**
 * Herbívoro del ecosistema. Come plantas y, si le queda energía, intenta reproducirse.
 * puedeReproducirse() solo valida la condición interna (energía > 60); que exista otro
 * conejo vivo se valida en reproducirse(eco), donde hay acceso al Ecosistema.
 */
public class Conejo extends Animal implements Reproducible {

    private static final double ENERGIA_MIN_REPRODUCCION = 60.0;
    private static final double COSTO_REPRODUCCION = 30.0;
    private static final double PERDIDA_SIN_COMIDA = 15.0;
    private static final double UMBRAL_PELIGRO = 20.0;
    private static final Random RANDOM = new Random();

    public Conejo(String nombre, double energiaInicial, int velocidad, double peso) {
        super(nombre, energiaInicial, velocidad, peso);
    }

    @Override
    public void actuar(Ecosistema eco) {
        comer(eco);
        if (isViva()) {
            intentarReproduccion(eco);
        }
    }

    @Override
    public void comer(Ecosistema eco) {
        Planta objetivo = eco.buscarPlantaVivaAleatoria();
        if (objetivo != null) {
            double valor = objetivo.serComida();
            eco.registrarMuerte(objetivo);
            setEnergia(getEnergia() + valor);
            eco.registrarEvento("Conejo '" + getNombre() + "' comió '" + objetivo.getNombre()
                    + "' (+" + (int) valor + " energía)");
        } else {
            setEnergia(getEnergia() - PERDIDA_SIN_COMIDA);
            String texto = "Conejo '" + getNombre() + "' no encontró comida (-"
                    + (int) PERDIDA_SIN_COMIDA + " energía)";
            if (getEnergia() < UMBRAL_PELIGRO) {
                texto += " [PELIGRO: energía=" + (int) getEnergia() + "]";
            }
            eco.registrarEvento(texto);
        }
    }

    @Override
    public String getTipo() { return "Conejo"; }

    @Override
    public String getDetalle() {
        return getEnergia() < UMBRAL_PELIGRO ? "EN PELIGRO" : "Estable";
    }

    @Override
    public boolean puedeReproducirse() {
        return isViva() && getEnergia() > ENERGIA_MIN_REPRODUCCION;
    }

    @Override
    public void reproducirse(Ecosistema eco) {
        List<Conejo> conejos = eco.getConejos();
        boolean hayOtroConejoVivo = false;
        for (Conejo c : conejos) {
            if (c != this && c.isViva()) {
                hayOtroConejoVivo = true;
                break;
            }
        }
        if (!hayOtroConejoVivo) {
            return;
        }

        setEnergia(getEnergia() - COSTO_REPRODUCCION);
        double energiaHijo = 25 + RANDOM.nextInt(16); // 25-40
        Conejo hijo = new Conejo(eco.generarNombreConejo(), energiaHijo, getVelocidad(), getPeso());
        eco.registrarNacimiento(hijo);
        eco.registrarEvento("Conejo '" + getNombre() + "' se reprodujo -> nuevo conejo '"
                + hijo.getNombre() + "' (energía: " + (int) energiaHijo + ")");
    }
}

