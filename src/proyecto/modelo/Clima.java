package proyecto.modelo;

/**
 * Climas posibles del ecosistema. Cada clima afecta distinto a plantas, conejos y lobos.
 */
public enum Clima {
    SOLEADO,
    LLUVIOSO,
    SEQUIA,
    INVIERNO;

    /** Texto para mostrar en pantalla. */
    public String getNombreLegible() {
        switch (this) {
            case SOLEADO:  return "Soleado";
            case LLUVIOSO: return "Lluvioso";
            case SEQUIA:   return "Sequía";
            case INVIERNO: return "Invierno";
            default:       return name();
        }
    }

    /** Convierte el texto que viene de la vista ("Soleado", "Sequía"...) en el enum. Devuelve null si no coincide. */
    public static Clima desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        for (Clima c : values()) {
            if (c.getNombreLegible().equalsIgnoreCase(texto.trim())) {
                return c;
            }
        }
        return null;
    }
}

