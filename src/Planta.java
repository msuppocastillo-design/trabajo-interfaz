import java.util.Random;


 
public class Planta extends Entidad implements Reproducible {

    private int tamanio; // 1 a 5, afecta la energía que da al ser comida
    private static final double ENERGIA_MIN_PARA_REPRODUCIR = 25.0;
    private static final double COSTO_REPRODUCCION = 10.0;
    private static final double PROB_BASE_REPRODUCCION = 0.40;
    private static final Random RANDOM = new Random();
 // mejoras a las pendejas de las plantas 
    public Planta(String nombre, double energiaInicial, int tamanio) {
        super(nombre, energiaInicial);
        setTamanio(tamanio);
    }

    public int getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = Math.max(1, Math.min(5, tamanio));
    }

    @Override
    public void actuar(Ecosistema eco) {
        // El default de Reproducible ya hace: si puedeReproducirse() -> reproducirse(eco)
        intentarReproduccion(eco);
    }

    @Override
    public void mostrarEstado() {
        System.out.printf("Planta '%s' (tamaño:%d, energia:%.1f)%n", getNombre(), tamanio, getEnergia());
    }

    /**
     * La planta es comida: pierde toda su energía y muere, y retorna el valor
     * nutritivo que le da a quien la come (tamanio * 10).
     */
    public double serComida() {
        double valorNutritivo = tamanio * 10.0;
        setEnergia(0);
        setViva(false);
        return valorNutritivo;
    }

    @Override
    public boolean puedeReproducirse() {
        return isViva() && getEnergia() > ENERGIA_MIN_PARA_REPRODUCIR;
    }

    @Override
    public void reproducirse(Ecosistema eco) {
        double factorClima;
        switch (eco.getClimaActual()) {
            case SOLEADO:  factorClima = 1.5; break;
            case LLUVIOSO: factorClima = 2.0; break;
            case SEQUIA:   factorClima = 0.5; break;
            case INVIERNO: factorClima = 0.0; break;
            default:       factorClima = 1.0;
        }

        if (factorClima <= 0.0) {
            return; // en Invierno las plantas no se reproducen
        }

        double probabilidad = Math.min(1.0, PROB_BASE_REPRODUCCION * factorClima);
        if (RANDOM.nextDouble() < probabilidad) {
            setEnergia(getEnergia() - COSTO_REPRODUCCION);
            int tamanioHija = Math.max(1, Math.min(5, tamanio + (RANDOM.nextInt(3) - 1)));
            double energiaHija = 20 + RANDOM.nextInt(21); // 20-40
            Planta hija = new Planta(eco.generarNombrePlanta(), energiaHija, tamanioHija);
            eco.registrarNacimiento(hija);
            System.out.println("Planta '" + getNombre() + "' se reprodujo -> nueva planta '"
                    + hija.getNombre() + "' (energia: " + (int) energiaHija + ")");
            eco.contarEvento();
        }
    }
}