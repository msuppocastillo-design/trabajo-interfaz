package proyecto.vista;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import proyecto.controlador.EcosistemaController;

/** Interfaz Swing: presenta datos y traduce acciones del usuario en llamadas al controlador. */
public class EcosistemaView extends JPanel {
    private EcosistemaController controlador;
    private final CardLayout cards = new CardLayout();
    private final JPanel pantallas = new JPanel(cards);
    private final JTextField plantas = new JTextField("12", 5), conejos = new JTextField("6", 5), lobos = new JTextField("2", 5), turnos = new JTextField("20", 5);
    private final JComboBox<String> climaInicial = new JComboBox<>(new String[]{"Soleado", "Lluvioso", "Sequía", "Invierno"});
    private final JComboBox<String> climaIntervencion = new JComboBox<>(new String[]{"Soleado", "Lluvioso", "Sequía", "Invierno"});
    private final JComboBox<String> tipoIntervencion = new JComboBox<>(new String[]{"planta", "conejo", "lobo"});
    private final JLabel estado = new JLabel("Configurá el ecosistema para comenzar.");
    private final JLabel contadores = new JLabel("Turno: 0 | Clima: — | Plantas: 0 | Conejos: 0 | Lobos: 0");
    private final JLabel textoIntervencion = new JLabel();
    private final JButton avanzar = new JButton("Avanzar turno"), cambiarClima = new JButton("Cambiar clima"), agregar = new JButton("Agregar entidad");
    private final DefaultTableModel tablaModelo = new DefaultTableModel(new String[]{"Tipo", "Nombre", "Edad", "Energía", "Detalle"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    private final JTextArea eventos = new JTextArea(8, 50);
    private final DefaultTableModel reporteModelo = new DefaultTableModel(new String[]{"Concepto", "Detalle"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };

    public EcosistemaView() {
        setLayout(new BorderLayout(12, 12)); setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        JLabel titulo = new JLabel("Simulador de Ecosistema", SwingConstants.LEFT); titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 24f));
        add(titulo, BorderLayout.NORTH);
        pantallas.add(crearConfiguracion(), "config"); pantallas.add(crearSimulacion(), "simulacion"); pantallas.add(crearReporte(), "reporte");
        add(pantallas, BorderLayout.CENTER); add(estado, BorderLayout.SOUTH);
    }
    public void setControlador(EcosistemaController c) { controlador = c; }
    private JPanel crearConfiguracion() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        JPanel form = new JPanel(new GridBagLayout()); form.setBorder(BorderFactory.createTitledBorder("Nuevo ecosistema"));
        String[] etiquetas={"Plantas (5–30)","Conejos (2–15)","Lobos (1–5)","Turnos (10–50)","Clima inicial"};
        JComponent[] campos={plantas,conejos,lobos,turnos,climaInicial};
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(8,10,8,10); g.anchor=GridBagConstraints.WEST;
        for(int i=0;i<etiquetas.length;i++){g.gridx=0;g.gridy=i;form.add(new JLabel(etiquetas[i]),g);g.gridx=1;form.add(campos[i],g);}
        JButton iniciar=new JButton("Crear simulación"); iniciar.addActionListener(e->controlador.iniciarSimulacion(plantas.getText(),conejos.getText(),lobos.getText(),(String)climaInicial.getSelectedItem(),turnos.getText()));
        JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT)); acciones.add(iniciar); p.add(form,BorderLayout.NORTH);p.add(acciones,BorderLayout.CENTER);
        p.add(new JLabel("La simulación se guarda automáticamente y puede retomarse al volver a abrirla."),BorderLayout.SOUTH); return p;
    }
    private JPanel crearSimulacion() {
        JPanel p=new JPanel(new BorderLayout(8,8)); JPanel top=new JPanel(new BorderLayout());top.add(contadores,BorderLayout.NORTH);top.add(textoIntervencion,BorderLayout.SOUTH);p.add(top,BorderLayout.NORTH);
        JTable tabla=new JTable(tablaModelo);tabla.setFillsViewportHeight(true);p.add(new JScrollPane(tabla),BorderLayout.CENTER);
        eventos.setEditable(false);eventos.setLineWrap(true);eventos.setWrapStyleWord(true);JPanel inferior=new JPanel(new BorderLayout(6,6));inferior.setBorder(BorderFactory.createTitledBorder("Eventos del último turno"));inferior.add(new JScrollPane(eventos),BorderLayout.CENTER);
        JPanel botones=new JPanel(new FlowLayout(FlowLayout.LEFT));botones.add(avanzar);botones.add(climaIntervencion);botones.add(cambiarClima);botones.add(tipoIntervencion);botones.add(agregar);
        JButton nueva=new JButton("Nueva simulación"),ver=new JButton("Ver reporte");botones.add(nueva);botones.add(ver);inferior.add(botones,BorderLayout.SOUTH);p.add(inferior,BorderLayout.SOUTH);
        avanzar.addActionListener(e->controlador.avanzarTurno());cambiarClima.addActionListener(e->controlador.cambiarClima((String)climaIntervencion.getSelectedItem()));agregar.addActionListener(e->controlador.agregarEntidad((String)tipoIntervencion.getSelectedItem()));nueva.addActionListener(e->controlador.nuevaSimulacion());ver.addActionListener(e->controlador.verReporte());return p;
    }
    private JPanel crearReporte(){JPanel p=new JPanel(new BorderLayout(8,8));p.add(new JScrollPane(new JTable(reporteModelo)),BorderLayout.CENTER);JButton volver=new JButton("Volver a la simulación");volver.addActionListener(e->controlador.irASimulacion());p.add(volver,BorderLayout.SOUTH);return p;}
    public void mostrarPantalla(String nombre){cards.show(pantallas,nombre);}
    public void mostrarMensaje(String m){estado.setText(m);}
    public void mostrarError(String m){JOptionPane.showMessageDialog(this,m,"Simulador de Ecosistema",JOptionPane.WARNING_MESSAGE);}
    public boolean confirmar(String m){return JOptionPane.showConfirmDialog(this,m,"Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;}
    public void actualizarContadores(int turno,int total,String clima,int p,int c,int l){contadores.setText("Turno: "+turno+" / "+total+"   |   Clima: "+clima+"   |   Plantas: "+p+"   |   Conejos: "+c+"   |   Lobos: "+l);}
    public void actualizarTabla(ArrayList<String[]> filas){tablaModelo.setRowCount(0);for(String[] f:filas)tablaModelo.addRow(f);}
    public void actualizarEventos(ArrayList<String> lista){eventos.setText(String.join("\n",lista));}
    public void actualizarIntervencion(String texto,boolean habilitada,int lobosRestantes){textoIntervencion.setText(texto+"   Lobos disponibles en toda la partida: "+lobosRestantes);cambiarClima.setEnabled(habilitada);agregar.setEnabled(habilitada);climaIntervencion.setEnabled(habilitada);tipoIntervencion.setEnabled(habilitada);}
    public void habilitarControles(boolean partidaActiva,boolean terminada){avanzar.setEnabled(partidaActiva);}
    public void actualizarReporte(ArrayList<String[]> filas){reporteModelo.setRowCount(0);for(String[] f:filas)reporteModelo.addRow(f);}
    public void limpiarFormulario(){plantas.setText("12");conejos.setText("6");lobos.setText("2");turnos.setText("20");}
}
