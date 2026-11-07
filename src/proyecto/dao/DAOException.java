package proyecto.dao;

/**
 * Error de lectura/escritura del archivo o de datos inválidos.
 * El DAO no muestra mensajes: lanza esto y el controlador decide cómo avisar.
 */
public class DAOException extends Exception {

    public DAOException(String mensaje) {
        super(mensaje);
    }

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
