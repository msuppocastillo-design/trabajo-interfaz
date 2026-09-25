/**
 * Contrato para entidades que pueden reproducirse (Planta y Conejo).
 * Permite procesarlas de forma polimórfica desde el Ecosistema
 * usando un ArrayList<Reproducible>.
 */
public interface Reproducible {

    /** Ejecuta la reproducción efectiva, agregando la nueva entidad al ecosistema.
     * @param eco */
    void reproducirse(Ecosistema eco);

    /** Indica si, en su estado actual, la entidad reúne las condiciones internas para reproducirse.
     * @return  */
    boolean puedeReproducirse();

    /**
     * Método default: encapsula el chequeo + la acción.
     * Si puedeReproducirse() es true, dispara reproducirse(eco).
     * @param eco
     */
    default void intentarReproduccion(Ecosistema eco) {
        if (puedeReproducirse()) {
            reproducirse(eco);
        }
    }
}
