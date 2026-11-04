package modelo;

import java.util.Random;

/**
 * Depredador del ecosistema. No implementa Reproducible. Implementa Peligroso
 * porque representa un riesgo para los conejos.
 */
public class Lobo extends Animal implements Peligroso {

    private int exitosCaza;
    private static final double GANANCIA_CAZA_EXITOSA = 40.0;
    private static final double PROB_BASE_CAZA = 0.20;
    private static final double PROB_MAX_CAZA = 0.90;
    private static final Random RANDOM = new Random();

    public Lobo(String nombre, double energiaInicial, int velocidad, double peso) {
        super(nombre, energiaInicial, velocidad, peso);
        this.exitosCaza = 0;
    }

    @Override
    public void actuar(Ecosistema eco) {
        comer(eco);
    }

    @Override
    public void comer(Ecosistema eco) {
        Conejo objetivo = eco.buscarConejoVivoAleatorio();
        if (objetivo == null) {
            eco.registrarEvento("Lobo '" + getNombre() + "' no encontró sus presas.");
            return;
        }

        // La probabilidad de éxito aumenta con la energía del lobo.
        double probabilidad = PROB_BASE_CAZA + (getEnergia() / 150.0);
        if (eco.getClimaActual() == Clima.INVIERNO) {
            probabilidad += 0.20; // Invierno: caza con +20% de éxito
        }
        probabilidad = Math.min(PROB_MAX_CAZA, probabilidad);

        if (RANDOM.nextDouble() < probabilidad) {
            objetivo.morir();
            setEnergia(getEnergia() + GANANCIA_CAZA_EXITOSA);
            exitosCaza++;
            eco.registrarMuerte(objetivo);
            eco.registrarEvento("Lobo '" + getNombre() + "' cazó al conejo '" + objetivo.getNombre()
                    + "' (+" + (int) GANANCIA_CAZA_EXITOSA + " energía) [cacerías: " + exitosCaza + "]");
        } else {
            eco.registrarEvento("Lobo '" + getNombre() + "' falló en la cacería");
        }
    }

    @Override
    public String getTipo() { return "Lobo"; }

    @Override
    public String getDetalle() { return "Cacerías exitosas: " + exitosCaza; }

    public int getExitosCaza() { return exitosCaza; }

    public void setExitosCaza(int exitosCaza) { this.exitosCaza = Math.max(0, exitosCaza); }

    @Override
    public int getNivelPeligro() {
        return exitosCaza * 2 + (int) (getEnergia() / 10);
    }
}
