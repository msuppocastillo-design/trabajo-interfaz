package dao;

import modelo.Ecosistema;

/**
 * Contrato del DAO: qué le puede pedir el Controlador, sin importar DÓNDE se guarda.
 */
public interface EcosistemaDAO {

    /**
     * Lee la simulación guardada y la devuelve armada.
     * Devuelve null si todavía no hay nada guardado.
     * Lanza DAOException si el archivo no se puede leer o tiene datos inválidos.
     */
    Ecosistema cargar() throws DAOException;

    /** Guarda el estado completo. Devuelve true si salió bien y false si falló. */
    boolean guardar(Ecosistema eco);

    /** Borra la simulación guardada. Devuelve true si no queda archivo. */
    boolean eliminar();
}
