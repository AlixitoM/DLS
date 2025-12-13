
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorGUI extends JFrame {

    // --- Componentes de la Interfaz ---
    private JTextPane txtEntrada; // Cambiado de JTextArea a JTextPane para estilos
    private StyledDocument doc;   // Necesario para manipular los colores
    private JTable tablaSimbolos;
    private JTable tablaErrores;
    private DefaultTableModel modeloSimbolos;
    private DefaultTableModel modeloErrores;
    private JLabel lblResumen;
    private Style verdeComentario;
    // --- Estilos y Colores ---
    private Style normal;
    private Style reservada;
    private Style numero;
    private Style operador;
    private Style errorStyle; // Renombrado para evitar confusión con lógica de errores
    private Style estructuraDato;
    // --- Control de Coloreado (Timer para no congelar la app) ---
    private Timer timerColoreo;
    private boolean coloreando = false;

    private static final Set<String> PALABRAS_RESERVADAS = DSLCore.getEstadosAceptacionDSL();

    public AnalizadorGUI() {
        setTitle("Analizador Léxico - DSL (Con Sintaxis)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Panel de Código (Modificado para usar JTextPane)
        JPanel panelCodigo = new JPanel(new BorderLayout(5, 5));
        panelCodigo.setBorder(BorderFactory.createTitledBorder(" Código Fuente "));

        txtEntrada = new JTextPane();
        txtEntrada.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtEntrada.setText("INSERTAR 10 EN PILA;\nENCOLAR @Error EN COLA;\nINSERTARZ 5;");
        doc = txtEntrada.getStyledDocument(); // Obtener el documento para estilar

        JScrollPane scrollCodigo = new JScrollPane(txtEntrada);
        scrollCodigo.setPreferredSize(new Dimension(800, 150));

        JButton btnAnalizar = new JButton("Analizar");
        btnAnalizar.setBackground(new Color(0, 120, 215));
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.setFont(new Font("Arial", Font.BOLD, 14));

        panelCodigo.add(scrollCodigo, BorderLayout.CENTER);
        panelCodigo.add(btnAnalizar, BorderLayout.EAST);

        // 2. Tablas (Tu diseño original intacto)
        String[] colsSimbolos = {"Lexema", "Línea", "Col", "Tipo Token", "Estado"};
        modeloSimbolos = new DefaultTableModel(colsSimbolos, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaSimbolos = new JTable(modeloSimbolos);
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        scrollSimbolos.setBorder(BorderFactory.createTitledBorder(" Tabla de Símbolos (Válidos) "));

        String[] colsErrores = {"Descripción del Error"};
        modeloErrores = new DefaultTableModel(colsErrores, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaErrores = new JTable(modeloErrores);
        tablaErrores.setForeground(Color.RED);
        tablaErrores.setFont(new Font("SansSerif", Font.BOLD, 13));
        JScrollPane scrollErrores = new JScrollPane(tablaErrores);
        scrollErrores.setBorder(BorderFactory.createTitledBorder(" Lista de Errores "));
        scrollErrores.setPreferredSize(new Dimension(800, 150));

        JSplitPane splitTablas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollSimbolos, scrollErrores);
        splitTablas.setResizeWeight(0.7);

        // 3. Eventos y Lógica
        btnAnalizar.addActionListener(e -> ejecutarAnalisis());

        add(panelCodigo, BorderLayout.NORTH);
        add(splitTablas, BorderLayout.CENTER);

        lblResumen = new JLabel(" Esperando análisis...");
        lblResumen.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(lblResumen, BorderLayout.SOUTH);

        // 4. Inicializar Colores
        inicializarEstilos();
        habilitarColoreadoTiempoReal();
        colorearTexto(); // Colorear el texto inicial
    }

    private void inicializarEstilos() {
        StyleContext sc = StyleContext.getDefaultStyleContext();

        normal = sc.addStyle("normal", null);
        StyleConstants.setForeground(normal, Color.BLACK);

        reservada = sc.addStyle("reservada", null);
        StyleConstants.setForeground(reservada, new Color(0, 0, 180)); // Azul oscuro
        StyleConstants.setBold(reservada, true);

        numero = sc.addStyle("numero", null);
        StyleConstants.setForeground(numero, new Color(150, 0, 150)); // Morado

        operador = sc.addStyle("operador", null);
        StyleConstants.setForeground(operador, Color.DARK_GRAY);

        errorStyle = sc.addStyle("error", null);
        StyleConstants.setForeground(errorStyle, Color.RED);

        verdeComentario = sc.addStyle("comentario", null);
        StyleConstants.setForeground(verdeComentario, new Color(0, 128, 0)); // Verde
        StyleConstants.setItalic(verdeComentario, true);

        // NUEVO: Estilo para estructuras de datos
        estructuraDato = sc.addStyle("estructura", null);
        StyleConstants.setForeground(estructuraDato, new Color(255, 140, 0)); // Naranja
        StyleConstants.setBold(estructuraDato, true);
    }

    private void colorearTexto() {
        if (coloreando) {
            return;
        }
        coloreando = true; // Flag para evitar recursión infinita

        SwingUtilities.invokeLater(() -> {
            try {
                String texto = txtEntrada.getText();
                doc.setCharacterAttributes(0, texto.length(), normal, true);

                Matcher m;

                // 0. Comentarios "//..."
                m = Pattern.compile("//.*").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), verdeComentario, false);
                }

                // 1. Números
                m = Pattern.compile("\\b\\d+\\b").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), numero, false);
                }

                // 2. Operadores y puntuación
                m = Pattern.compile("[=+\\-*/<>;()]").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), 1, operador, false);
                }

                // 3. Palabras Reservadas
                m = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b").matcher(texto);
                while (m.find()) {
                    String palabra = m.group().toUpperCase();

                    if (ESTRUCTURAS_DATOS.contains(palabra)) {
                        // Es una estructura de datos
                        doc.setCharacterAttributes(m.start(), m.end() - m.start(), estructuraDato, false);
                    } else if (PALABRAS_RESERVADAS.contains(palabra)) {
                        // Palabra reservada normal
                        doc.setCharacterAttributes(m.start(), m.end() - m.start(), reservada, false);
                    }
                }

            } catch (Exception e) {
                // Ignorar errores de concurrencia
            } finally {
                coloreando = false;
            }
        });
    }

    private void habilitarColoreadoTiempoReal() {
        // Timer espera 300ms después de que dejas de escribir para colorear
        // Esto evita que la app se trabe si escribes muy rápido
        timerColoreo = new Timer(300, e -> colorearTexto());
        timerColoreo.setRepeats(false);

        txtEntrada.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                timerColoreo.restart();
            }

            public void removeUpdate(DocumentEvent e) {
                timerColoreo.restart();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    // --- TU LÓGICA DE ANÁLISIS (Intacta) ---
    private void ejecutarAnalisis() {
        String codigo = txtEntrada.getText();
        modeloSimbolos.setRowCount(0);
        modeloErrores.setRowCount(0);

        try {
            // Asumo que tienes la clase DSLCore y sus métodos estáticos disponibles
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

    private static final Set<String> ESTRUCTURAS_DATOS = Set.of(
            "PILA", "COLA", "BICOLA", "LISTA", "LISTA_DOBLE", "LISTA_CIRCULAR", "ARBOL_BINARIO", "TABLA_HASH", "GRAFO"
    );

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnalizadorGUI().setVisible(true));
    }
}
