/**
 * Representa los climas posibles del ecosistema.
 * Cada clima afecta de forma distinta a plantas, conejos y lobos.
 */
public enum Clima {
    SOLEADO,
    LLUVIOSO,
    SEQUIA,
    INVIERNO;

    /** Texto lindo para mostrar en consola. */
    public String getNombreLegible() {
        switch (this) {
            case SOLEADO:  return "Soleado";
            case LLUVIOSO: return "Lluvioso";
            case SEQUIA:   return "Sequía";
            case INVIERNO: return "Invierno";
            default:       return name();
        }
    }
}
