import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnalizadorGUI extends JFrame {

    private JTextArea txtEntrada;
    private JTable tablaSimbolos;
    private JTable tablaErrores; 
    private DefaultTableModel modeloSimbolos;
    private DefaultTableModel modeloErrores;
    private JLabel lblResumen;

    public AnalizadorGUI() {
        setTitle("Analizador Léxico - DSL");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelCodigo = new JPanel(new BorderLayout(5, 5));
        panelCodigo.setBorder(BorderFactory.createTitledBorder(" Código Fuente "));
        
        txtEntrada = new JTextArea();
        txtEntrada.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtEntrada.setText("INSERTAR 10 EN PILA;\nENCOLAR @Error EN COLA;\nINSERTARZ 5;");
        JScrollPane scrollCodigo = new JScrollPane(txtEntrada);
        scrollCodigo.setPreferredSize(new Dimension(800, 150));
        
        JButton btnAnalizar = new JButton("Analizar");
        btnAnalizar.setBackground(new Color(0, 120, 215));
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.setFont(new Font("Arial", Font.BOLD, 14));
        
        panelCodigo.add(scrollCodigo, BorderLayout.CENTER);
        panelCodigo.add(btnAnalizar, BorderLayout.EAST);

        String[] colsSimbolos = {"Lexema", "Línea", "Col", "Tipo Token", "Estado"};
        modeloSimbolos = new DefaultTableModel(colsSimbolos, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaSimbolos = new JTable(modeloSimbolos);
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        scrollSimbolos.setBorder(BorderFactory.createTitledBorder(" Tabla de Símbolos (Válidos) "));

        String[] colsErrores = {"Descripción del Error"};
        modeloErrores = new DefaultTableModel(colsErrores, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaErrores = new JTable(modeloErrores);
        tablaErrores.setForeground(Color.RED); 
        tablaErrores.setFont(new Font("SansSerif", Font.BOLD, 13));
        JScrollPane scrollErrores = new JScrollPane(tablaErrores);
        scrollErrores.setBorder(BorderFactory.createTitledBorder(" Lista de Errores "));
        scrollErrores.setPreferredSize(new Dimension(800, 150));


        JSplitPane splitTablas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollSimbolos, scrollErrores);
        splitTablas.setResizeWeight(0.7);

        btnAnalizar.addActionListener(e -> ejecutarAnalisis());

        add(panelCodigo, BorderLayout.NORTH);
        add(splitTablas, BorderLayout.CENTER);
        
        lblResumen = new JLabel(" Esperando análisis...");
        lblResumen.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(lblResumen, BorderLayout.SOUTH);
    }

    private void ejecutarAnalisis() {
        String codigo = txtEntrada.getText();
        modeloSimbolos.setRowCount(0);
        modeloErrores.setRowCount(0);

        try {
            Token[] tokens = DSLCore.tokenizador(codigo);
            AFD afd = DSLCore.obtenerInstanciaAFD();
            Token[] resultados = afd.aceptar(tokens);

            int contadorErrores = 0;

            for (Token t : resultados) {
                if (t.getTipoToken().startsWith("ERROR") || !t.existeSimbolo()) {
                    contadorErrores++;
                    String descripcion = String.format(
                        "Error léxico en línea %d, columna %d: El lexema '%s' es inválido (%s)", 
                        t.getLinea(), 
                        t.getColumna(), 
                        t.getLexema(),
                        t.getTipoToken()
                    );
                    modeloErrores.addRow(new Object[]{descripcion});
                } else {
                    modeloSimbolos.addRow(new Object[]{
                        t.getLexema(), t.getLinea(), t.getColumna(), t.getTipoToken(), t.getEstadoFinal()
                    });
                }
            }

            lblResumen.setText(" Análisis finalizado. Se encontraron " + contadorErrores + " errores.");
            lblResumen.setForeground(contadorErrores > 0 ? Color.RED : new Color(0, 100, 0));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error crítico: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnalizadorGUI().setVisible(true));
    }
}