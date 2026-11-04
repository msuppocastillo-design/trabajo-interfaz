package modelo;

/**
 * Clase abstracta base de todas las entidades del ecosistema (Planta, Conejo, Lobo).
 */
public abstract class Entidad {

    private String nombre;
    private double energia;
    private int edad;
    private boolean viva;

    /** Energía base que toda entidad gasta por existir cada turno. */
    protected static final double ENERGIA_BASE_EXISTIR = 2.0;

    public Entidad(String nombre, double energiaInicial) {
        this.nombre = nombre;
        this.edad = 0;
        this.viva = true;
        setEnergia(energiaInicial);
    }

    /** Define qué hace la entidad en su turno. */
    public abstract void actuar(Ecosistema eco);

    /** Nombre del tipo ("Planta", "Conejo", "Lobo"). Lo usan la tabla y el archivo. */
    public abstract String getTipo();

    /** Dato propio de cada tipo, en texto (tamaño, cacerías...). Reemplaza al viejo mostrarEstado() por consola. */
    public abstract String getDetalle();

    public void envejecer() {
        this.edad++;
        setEnergia(this.energia - ENERGIA_BASE_EXISTIR);
    }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getEnergia() { return energia; }

    /** La energía nunca puede ser negativa. */
    public void setEnergia(double energia) { this.energia = Math.max(0.0, energia); }

    public int getEdad() { return edad; }

    public void setEdad(int edad) { this.edad = Math.max(0, edad); }

    public boolean isViva() { return viva; }

    public void setViva(boolean viva) { this.viva = viva; }
}
