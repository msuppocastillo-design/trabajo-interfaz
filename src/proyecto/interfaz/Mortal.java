/**
 * Contrato para entidades que pueden morir de forma explícita (Conejo y Lobo).
 * getEnergia() y getNombre() ya vienen implementados por Entidad, así que
 * cualquier Animal los cumple automáticamente al implementar esta interface.
 */
public interface Mortal {

    boolean estaVivo();

    void morir();

    double getEnergia();

    String getNombre();

    /**
     * Método default: si la entidad sigue "viva" pero su energía llegó a 0,
     * dispara morir() e informa el evento por consola.
     */
    default void verificarMuerte() {
        if (estaVivo() && getEnergia() <= 0) {
            morir();
            System.out.println("Se murió " + getNombre() + " (sin energía).");
        }
    }
}
