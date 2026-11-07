package proyecto.modelo;

/**
 * Contrato para entidades que representan un riesgo dentro del ecosistema (Lobo).
 */
public interface Peligroso {

    /** Nivel de peligro numérico, usado para ordenar en el reporte final. */
    int getNivelPeligro();
}

