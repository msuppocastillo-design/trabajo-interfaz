package proyecto.modelo;

/**
 * Contrato para entidades que pueden morir de forma explícita (Conejo y Lobo).
 */
public interface Mortal {

    boolean estaVivo();

    void morir();

    double getEnergia();

    String getNombre();

    /**
     * Si la entidad sigue "viva" pero su energía llegó a 0, la mata.
     * Devuelve true si murió en esta llamada (el Ecosistema registra el evento;
     * el modelo no imprime nada).
     */
    default boolean verificarMuerte() {
        if (estaVivo() && getEnergia() <= 0) {
            morir();
            return true;
        }
        return false;
    }
}

