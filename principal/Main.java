package proyecto.principal;

import javax.swing.JFrame;
import proyecto.controlador.EcosistemaController;
import dao.EcosistemaDAOArchivo;
import proyecto.vista.EcosistemaView;

/** Ensambla MVC y muestra la ventana principal. */
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        EcosistemaView vista = new EcosistemaView();
        EcosistemaController controlador = new EcosistemaController(vista, new EcosistemaDAOArchivo());
        vista.setControlador(controlador);
        JFrame ventana = new JFrame("Simulador de Ecosistema");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setContentPane(vista);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
        controlador.iniciar();
    }
}
