package principal;

import dao.EcosistemaDAOArchivo;
import proyecto.controlador.EcosistemaController;
import proyecto.vista.EcosistemaView;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class EcosistemaMVC {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EcosistemaView vista = new EcosistemaView();
            EcosistemaController controlador = new EcosistemaController(vista, new EcosistemaDAOArchivo());
            vista.setControlador(controlador);
            JFrame ventana = new JFrame("Simulador de ecosistema");
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.setContentPane(vista);
            ventana.pack();
            ventana.setLocationRelativeTo(null);
            controlador.iniciar();
            ventana.setVisible(true);
        });
    }
}
