package proyecto.modelo;

/**
 * Contrato para entidades que pueden reproducirse (Planta y Conejo).
 */
public interface Reproducible {

    /** Ejecuta la reproducción efectiva, agregando la nueva entidad al ecosistema. */
    void reproducirse(Ecosistema eco);

    /** Indica si, en su estado actual, la entidad reúne las condiciones internas para reproducirse. */
    boolean puedeReproducirse();

    /** Si puedeReproducirse() es true, dispara reproducirse(eco). */
    default void intentarReproduccion(Ecosistema eco) {
        if (puedeReproducirse()) {
            reproducirse(eco);
        }
    }
}

