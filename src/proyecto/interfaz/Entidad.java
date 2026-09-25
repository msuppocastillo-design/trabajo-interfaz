/**
 * Clase abstracta base de todas las entidades del ecosistema
 * (Planta, Conejo, Lobo). Define el estado y comportamiento común.
 */
public abstract class Entidad {

    private String nombre;
    private double energia;
    private int edad;
    private boolean viva;

    /** Energía base que toda entidad gasta por el simple hecho de existir cada turno. */
    protected static final double ENERGIA_BASE_EXISTIR = 2.0;

    public Entidad(String nombre, double energiaInicial) {
        this.nombre = nombre;
        this.edad = 0;
        this.viva = true;
        setEnergia(energiaInicial); // pasa por el setter para validar
    }

    // ---- Métodos abstractos que cada subclase concreta debe implementar ----

    /** Define qué hace la entidad en su turno (comer, reproducirse, cazar, etc.). */
    public abstract void actuar(Ecosistema eco);

    /** Imprime el estado particular de la entidad. */
    public abstract void mostrarEstado();

    // ---- Método concreto compartido ----

    /**
     * Incrementa la edad y descuenta la energía base por existir.
     * Todas las entidades envejecen igual, sin importar el tipo.
     */
    public void envejecer() {
        this.edad++;
        setEnergia(this.energia - ENERGIA_BASE_EXISTIR);
    }

    // ---- Getters y setters con validación ----

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getEnergia() {
        return energia;
    }

    /** La energía nunca puede ser negativa: si el valor es menor a 0, se lleva a 0. */
    public void setEnergia(double energia) {
        this.energia = Math.max(0.0, energia);
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = Math.max(0, edad);
    }

    public boolean isViva() {
        return viva;
    }

    public void setViva(boolean viva) {
        this.viva = viva;
    }
}
