import java.util.Random;

/**
 * Depredador del ecosistema. No implementa Reproducible: los lobos no se
 * reproducen en esta simulación. Implementa Peligroso (bonus) porque
 * representa un riesgo para los conejos.
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
            System.out.println("Lobo '" + getNombre() + "' no encontró sus presas.");
            return;
        }

        // La probabilidad de éxito AUMENTA con la energía del lobo (no es fija).
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
            System.out.println("Lobo '" + getNombre() + "' cazó a un Conejo '" + objetivo.getNombre()
                    + "' (+" + (int) GANANCIA_CAZA_EXITOSA + " energia) [cacerías: " + exitosCaza + "]");
            eco.contarEvento();
        } else {
            System.out.println("Lobo '" + getNombre() + "' falló en la caceria");
            eco.contarEvento();
        }
    }

    @Override
    public void mostrarEstado() {
        System.out.printf("Lobo '%s' (energia:%.1f, cacerías exitosas:%d)%n",
                getNombre(), getEnergia(), exitosCaza);
    }

    public int getExitosCaza() {
        return exitosCaza;
    }

    public void setExitosCaza(int exitosCaza) {
        this.exitosCaza = Math.max(0, exitosCaza);
    }

    @Override
    public int getNivelPeligro() {
        // A más cacerías exitosas y más energía, más peligroso.
        return exitosCaza * 2 + (int) (getEnergia() / 10);
    }
}
