package proyecto.modelo;

/**
 * Capa intermedia entre Entidad y los animales concretos (Conejo, Lobo).
 * Implementa Mortal de forma concreta: estaVivo()/morir() son iguales para todo animal.
 */
public abstract class Animal extends Entidad implements Mortal {

    private int velocidad;
    private double peso;

    public Animal(String nombre, double energiaInicial, int velocidad, double peso) {
        super(nombre, energiaInicial);
        this.velocidad = velocidad;
        this.peso = peso;
    }

    /** Cada animal concreto define cómo consigue energía (comiendo plantas o cazando). */
    public abstract void comer(Ecosistema eco);

    /** Todo animal puede desplazarse. Devuelve el texto en lugar de imprimirlo. */
    public String moverse() {
        return getNombre() + " se movió por el ecosistema.";
    }

    @Override
    public boolean estaVivo() { return isViva(); }

    @Override
    public void morir() {
        setViva(false);
        setEnergia(0);
    }

    public int getVelocidad() { return velocidad; }

    public void setVelocidad(int velocidad) { this.velocidad = Math.max(0, velocidad); }

    public double getPeso() { return peso; }

    public void setPeso(double peso) { this.peso = Math.max(0, peso); }
}

