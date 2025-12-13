import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*; // --- NUEVO: Para estilos de texto
import javax.swing.event.DocumentEvent; // --- NUEVO: Para detectar escritura
import javax.swing.event.DocumentListener; // --- NUEVO: Para detectar escritura
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set; // --- NUEVO
import java.util.regex.Matcher; // --- NUEVO: Para buscar palabras a colorear
import java.util.regex.Pattern; // --- NUEVO

public class AnalizadorGUI extends JFrame {

    // --- Componentes de la Interfaz ---
    private JTextPane txtEntrada;         // --- CAMBIO: JTextArea -> JTextPane
    private StyledDocument doc;           // --- NUEVO: Para manejar los estilos del texto
    private JTextArea txtSintactico;      // Donde sale el árbol de derivación
    private JTable tablaSimbolos;         // Tabla de Tokens válidos
    private JTable tablaErrores;          // Tabla unificada de Errores

    // --- Modelos de datos ---
    private DefaultTableModel modeloSimbolos;
    private DefaultTableModel modeloErrores;
    private JLabel lblResumen;

    // --- Estilos y Colores (NUEVO) ---
    private Style normal;
    private Style reservada;
    private Style numero;
    private Style operador;
    private Style errorStyle;
    private Style verdeComentario;
    private Style estructuraDato;
    
    // --- Control de Coloreado (NUEVO) ---
    private Timer timerColoreo;
    private boolean coloreando = false;

    // Definimos las palabras para colorear (Hardcoded para asegurar que funcione visualmente)
    private static final Set<String> PALABRAS_RESERVADAS = Set.of(
        "CREAR", "INSERTAR", "APILAR", "ENCOLAR", "MOSTRAR", "IF", "ELSE", 
        "ELIMINAR", "BUSCAR", "EN", "CON", "VALOR"
    );
    
    private static final Set<String> ESTRUCTURAS_DATOS = Set.of(
        "PILA", "COLA", "BICOLA", "LISTA_ENLAZADA", "LISTA_CIRCULAR", 
        "ARBOL_BINARIO", "TABLA_HASH", "GRAFO", "PILA_CIRCULAR", "COLA_CIRCULAR"
    );

    public AnalizadorGUI() {
        // Configuración de la Ventana Principal
        setTitle("Compilador DSLabstrae - Léxico y Sintáctico (Con Colores)");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- 1. PANEL SUPERIOR: Código Fuente y Botón ---
        JPanel panelCodigo = new JPanel(new BorderLayout(5, 5));
        panelCodigo.setBorder(BorderFactory.createTitledBorder(" Editor de Código "));
        
        // --- CAMBIO: Inicialización de JTextPane ---
        txtEntrada = new JTextPane();
        txtEntrada.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtEntrada.setText("// Ejemplo con colores\nCREAR COLA_CIRCULAR miCola 10;\nAPILAR 10 EN miPila;\nIF (VACIAT EN miPila) { MOSTRAR 0; }");
        
        doc = txtEntrada.getStyledDocument(); // Obtenemos el documento para aplicar estilos
        
        JScrollPane scrollCodigo = new JScrollPane(txtEntrada);
        scrollCodigo.setPreferredSize(new Dimension(1000, 150));
        
        JButton btnAnalizar = new JButton("Compilar (Analizar)");
        btnAnalizar.setBackground(new Color(0, 120, 215)); // Azul estilo VS Code
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.setFont(new Font("Arial", Font.BOLD, 14));
        btnAnalizar.setFocusPainted(false);
        
        panelCodigo.add(scrollCodigo, BorderLayout.CENTER);
        panelCodigo.add(btnAnalizar, BorderLayout.EAST);

        // --- 2. PANEL CENTRAL: Pestañas de Resultados ---
        JTabbedPane pestañas = new JTabbedPane();

        // Pestaña A: Tabla de Símbolos (Léxico)
        String[] colsSimbolos = {"Lexema", "Línea", "Col", "Tipo Token", "Estado"};
        modeloSimbolos = new DefaultTableModel(colsSimbolos, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaSimbolos = new JTable(modeloSimbolos);
        tablaSimbolos.setFillsViewportHeight(true);
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        pestañas.addTab("Análisis Léxico (Tokens)", scrollSimbolos);

        // Pestaña B: Árbol Sintáctico (Reglas)
        txtSintactico = new JTextArea();
        txtSintactico.setEditable(false);
        txtSintactico.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtSintactico.setForeground(new Color(40, 40, 40));
        JScrollPane scrollSintactico = new JScrollPane(txtSintactico);
        pestañas.addTab("Árbol de Derivación (Sintaxis)", scrollSintactico);

        // --- 3. PANEL INFERIOR: Lista de Errores ---
        String[] colsErrores = {"Línea", "Tipo", "Descripción del Error"};
        modeloErrores = new DefaultTableModel(colsErrores, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaErrores = new JTable(modeloErrores);
        tablaErrores.setForeground(Color.RED);
        tablaErrores.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        // Ajustar ancho de columnas de error
        tablaErrores.getColumnModel().getColumn(0).setPreferredWidth(50);  // Línea
        tablaErrores.getColumnModel().getColumn(0).setMaxWidth(80);
        tablaErrores.getColumnModel().getColumn(1).setPreferredWidth(100); // Tipo
        tablaErrores.getColumnModel().getColumn(1).setMaxWidth(150);
        
        JScrollPane scrollErrores = new JScrollPane(tablaErrores);
        scrollErrores.setBorder(BorderFactory.createTitledBorder(" Consola de Errores Unificada "));
        scrollErrores.setPreferredSize(new Dimension(1000, 150));

        // --- ORGANIZACIÓN FINAL ---
        JSplitPane splitCentral = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pestañas, scrollErrores);
        splitCentral.setResizeWeight(0.65); 

        add(panelCodigo, BorderLayout.NORTH);
        add(splitCentral, BorderLayout.CENTER);

        // Barra de estado inferior
        lblResumen = new JLabel(" Listo para analizar.");
        lblResumen.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(lblResumen, BorderLayout.SOUTH);

        // --- ACCIÓN DEL BOTÓN ---
        btnAnalizar.addActionListener(e -> ejecutarAnalisis());

        // --- NUEVO: INICIALIZAR LÓGICA DE COLORES ---
        inicializarEstilos();
        habilitarColoreadoTiempoReal();
        colorearTexto(); // Coloreada inicial
    }

    // --- NUEVO: DEFINICIÓN DE COLORES ---
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

        estructuraDato = sc.addStyle("estructura", null);
        StyleConstants.setForeground(estructuraDato, new Color(255, 140, 0)); // Naranja
        StyleConstants.setBold(estructuraDato, true);
    }

    // --- NUEVO: LÓGICA DE COLOREADO AUTOMÁTICO ---
    private void colorearTexto() {
        if (coloreando) return;
        coloreando = true;

        SwingUtilities.invokeLater(() -> {
            try {
                String texto = txtEntrada.getText();
                // Resetear a normal
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
                    // Verificamos si no está dentro de un comentario (lógica simple)
                    // Para un IDE real se requiere un parser más complejo, pero esto funciona visualmente
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), numero, false);
                }

                // 2. Operadores
                m = Pattern.compile("[=+\\-*/<>;(){}]").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), 1, operador, false);
                }

                // 3. Palabras Reservadas y Estructuras
                m = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b").matcher(texto);
                while (m.find()) {
                    String palabra = m.group().toUpperCase();
                    if (ESTRUCTURAS_DATOS.contains(palabra)) {
                        doc.setCharacterAttributes(m.start(), m.end() - m.start(), estructuraDato, false);
                    } else if (PALABRAS_RESERVADAS.contains(palabra)) {
                        doc.setCharacterAttributes(m.start(), m.end() - m.start(), reservada, false);
                    }
                }
                
                // Re-aplicar color de comentarios al final para que ganen prioridad sobre keywords dentro de ellos
                m = Pattern.compile("//.*").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), verdeComentario, false);
                }

            } catch (Exception e) {
                // Ignorar errores durante el tecleo rápido
            } finally {
                coloreando = false;
            }
        });
    }

    private void habilitarColoreadoTiempoReal() {
        // Timer espera 300ms después de que dejas de escribir para colorear
        timerColoreo = new Timer(300, e -> colorearTexto());
        timerColoreo.setRepeats(false);

        txtEntrada.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { timerColoreo.restart(); }
            public void removeUpdate(DocumentEvent e) { timerColoreo.restart(); }
            public void changedUpdate(DocumentEvent e) { }
        });
    }

    // --- LÓGICA DE ANÁLISIS (Mantenemos tu lógica original exacta) ---
    private void ejecutarAnalisis() {
        String codigo = txtEntrada.getText();
        
        // 1. Limpiar resultados anteriores
        modeloSimbolos.setRowCount(0);
        modeloErrores.setRowCount(0);
        txtSintactico.setText("");
        lblResumen.setForeground(Color.BLACK);
        lblResumen.setText("Analizando...");

        List<ErrorReporte> listaErroresUnificada = new ArrayList<>();

        try {
            // --- FASE 1: ANÁLISIS LÉXICO ---
            Token[] tokens = DSLCore.tokenizador(codigo);
            AFD afd = DSLCore.obtenerInstanciaAFD();
            Token[] resultadosLexicos = afd.aceptar(tokens);

            List<Token> tokensValidos = new ArrayList<>();

            for (Token t : resultadosLexicos) {
                if (t.getTipoToken().startsWith("ERROR") || !t.existeSimbolo()) {
                    String desc = String.format("Lexema '%s' no reconocido (%s)", 
                                                t.getLexema(), t.getTipoToken());
                    listaErroresUnificada.add(new ErrorReporte(t.getLinea(), "LÉXICO", desc));
                } else {
                    tokensValidos.add(t);
                    modeloSimbolos.addRow(new Object[]{
                        t.getLexema(), t.getLinea(), t.getColumna(), t.getTipoToken(), t.getEstadoFinal()
                    });
                }
            }

            // --- FASE 2: ANÁLISIS SINTÁCTICO ---
            Token[] arrayTokensValidos = tokensValidos.toArray(new Token[0]);
            AnalizadorSintactico sintactico = new AnalizadorSintactico(arrayTokensValidos);
            sintactico.analizar(); 

            // Mostrar el árbol de derivación
            List<String> log = sintactico.getLogDerivacion();
            StringBuilder sb = new StringBuilder();
            for (String paso : log) {
                sb.append(paso).append("\n");
            }
            txtSintactico.setText(sb.toString());

            // Recolectar errores sintácticos
            List<String> erroresSin = sintactico.getErrores();
            for (String errStr : erroresSin) {
                int linea = 0;
                try {
                    if(errStr.contains("Línea ")) {
                        String num = errStr.substring(errStr.indexOf("Línea ") + 6, errStr.indexOf("]"));
                        linea = Integer.parseInt(num.trim());
                    }
                } catch (Exception ignore) { linea = -1; }
                listaErroresUnificada.add(new ErrorReporte(linea, "SINTÁCTICO", errStr));
            }

            // --- FASE 3: ORDENAR Y MOSTRAR ---
            Collections.sort(listaErroresUnificada);

            for (ErrorReporte err : listaErroresUnificada) {
                modeloErrores.addRow(new Object[]{err.linea, err.tipo, err.descripcion});
            }

            // Resumen Final
            int totalErrores = listaErroresUnificada.size();
            if (totalErrores == 0) {
                lblResumen.setText(" Análisis Finalizado con ÉXITO. El código es correcto.");
                lblResumen.setForeground(new Color(0, 100, 0));
            } else {
                lblResumen.setText(" Se encontraron " + totalErrores + " errores en total.");
                lblResumen.setForeground(Color.RED);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error crítico en el compilador: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnalizadorGUI().setVisible(true));
    }

    // --- CLASE INTERNA PARA GESTIÓN DE ERRORES ---
    private static class ErrorReporte implements Comparable<ErrorReporte> {
        int linea;
        String tipo;
        String descripcion;

        public ErrorReporte(int linea, String tipo, String descripcion) {
            this.linea = linea;
            this.tipo = tipo;
            this.descripcion = descripcion;
        }

        @Override
        public int compareTo(ErrorReporte o) {
            return Integer.compare(this.linea, o.linea);
        }
    }
}