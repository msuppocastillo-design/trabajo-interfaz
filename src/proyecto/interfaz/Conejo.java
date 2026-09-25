import java.util.List;
import java.util.Random;


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
            double valor = objetivo.serComida(); // puede ser negativo si era venenosa
            setEnergia(getEnergia() + valor);
            if (valor >= 0) {
                System.out.println("Conejo '" + getNombre() + "' comió '" + objetivo.getNombre()
                        + "' (+" + (int) valor + " energia)");
            } else {
                System.out.println("Conejo '" + getNombre() + "' comió '" + objetivo.getNombre()
                        + "' y era venenosa (" + (int) valor + " energia)");
            }
            eco.contarEvento();
        } else {
            setEnergia(getEnergia() - PERDIDA_SIN_COMIDA);
            System.out.print("Conejo '" + getNombre() + "' no encontró comida (-"
                    + (int) PERDIDA_SIN_COMIDA + " energia)");
            imprimirPeligroSiCorresponde();
            eco.contarEvento();
        }
    }

    private void imprimirPeligroSiCorresponde() {
        if (getEnergia() < UMBRAL_PELIGRO) {
            System.out.println(" [PELIGRO: energia=" + (int) getEnergia() + "]");
        } else {
            System.out.println();
        }
    }

    @Override
    public void mostrarEstado() {
        String peligro = getEnergia() < UMBRAL_PELIGRO ? " [EN PELIGRO]" : "";
        System.out.printf("Conejo '%s' (energia:%.1f)%s%n", getNombre(), getEnergia(), peligro);
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
        System.out.println("Conejo '" + getNombre() + "' se reprodujo -> nuevo conejo '"
                + hijo.getNombre() + "' (energia: " + (int) energiaHijo + ")");
        eco.contarEvento();
    }
}