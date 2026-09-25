/**
 * (Bonus) Contrato para entidades que representan un riesgo dentro del ecosistema.
 * La implementan Lobo, y opcionalmente PlantaVenenosa.
 */
public interface Peligroso {

    /** Devuelve un nivel de peligro numérico, usado para ordenar en el reporte final. */
    int getNivelPeligro();
}
