/**
 * (Bonus) Planta venenosa: visualmente indistinguible de una planta normal para
 * el conejo (que solo llama a serComida() de forma polimórfica, sin saber el
 * tipo real), pero en vez de nutrirlo, le hace perder energía.
 * Se guarda en el mismo ArrayList<Planta> que las plantas comunes (polimorfismo).
 */
public class PlantaVenenosa extends Planta implements Peligroso {

    private static final double PERDIDA_ENERGIA_CONEJO = 30.0;

    public PlantaVenenosa(String nombre, double energiaInicial, int tamanio) {
        super(nombre, energiaInicial, tamanio);
    }

    @Override
    public double serComida() {
        // Muere igual que una planta normal, pero el valor que retorna es negativo:
        // quien la come pierde energía en vez de ganarla.
        setEnergia(0);
        setViva(false);
        return -PERDIDA_ENERGIA_CONEJO;
    }

    @Override
    public void mostrarEstado() {
        // A propósito no delata que es venenosa en la salida "pública" del turno;
        // se podría usar un modo debug para mostrarlo distinto si se necesitara.
        System.out.printf("Planta '%s' (tamaño:%d, energia:%.1f) [venenosa]%n",
                getNombre(), getTamanio(), getEnergia());
    }

    @Override
    public int getNivelPeligro() {
        return 4; // fija, para el ranking del reporte final
    }
}
