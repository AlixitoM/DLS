import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*; 
import javax.swing.event.DocumentEvent; 
import javax.swing.event.DocumentListener; 
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set; 
import java.util.regex.Matcher; 
import java.util.regex.Pattern; 

public class AnalizadorGUI extends JFrame {

    // --- Componentes de la Interfaz ---
    private JTextPane txtEntrada;         
    private JTextArea txtNumerosLineas;   // --- NUEVO: Componente para los números de línea
    private StyledDocument doc;           
    private JTextArea txtSintactico;      
    private JTable tablaSimbolos;         
    private JTable tablaErrores;          

    // --- Modelos de datos ---
    private DefaultTableModel modeloSimbolos;
    private DefaultTableModel modeloErrores;
    private JLabel lblResumen;

    // --- Estilos y Colores ---
    private Style normal;
    private Style reservada;
    private Style numero;
    private Style operador;
    private Style errorStyle;
    private Style verdeComentario;
    private Style estructuraDato;
    private Style cadenaStyle; // Agregado por si acaso lo usas después
    
    // --- Control de Coloreado ---
    private Timer timerColoreo;
    private boolean coloreando = false;

    // Definimos las palabras para colorear
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
        setTitle("Compilador DSLabstrae - Léxico y Sintáctico (Con Colores y Líneas)");
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
        
        doc = txtEntrada.getStyledDocument(); 
        
        // --- NUEVO: Configuración del Área de Números de Línea ---
        txtNumerosLineas = new JTextArea("1");
        txtNumerosLineas.setFont(new Font("Consolas", Font.PLAIN, 14)); // Misma fuente que el código
        txtNumerosLineas.setBackground(new Color(230, 230, 230)); // Gris claro
        txtNumerosLineas.setForeground(Color.GRAY); // Números en gris oscuro
        txtNumerosLineas.setEditable(false); // IMPORTANTE: El usuario no puede borrar esto
        txtNumerosLineas.setMargin(new Insets(0, 5, 0, 5)); // Margen para que se vea ordenado

        JScrollPane scrollCodigo = new JScrollPane(txtEntrada);
        scrollCodigo.setRowHeaderView(txtNumerosLineas); // --- NUEVO: Esto coloca los números a la izquierda
        scrollCodigo.setPreferredSize(new Dimension(1000, 150));
        
        JButton btnAnalizar = new JButton("Compilar (Analizar)");
        btnAnalizar.setBackground(new Color(0, 120, 215)); 
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

        // --- INICIALIZAR LÓGICA DE COLORES Y NÚMEROS ---
        inicializarEstilos();
        habilitarColoreadoTiempoReal();
        colorearTexto(); // Coloreada inicial
        actualizarNumerosDeLinea(); // Actualización inicial de números
    }

    // --- NUEVO: MÉTODO PARA CALCULAR LOS NÚMEROS DE LÍNEA ---
    private void actualizarNumerosDeLinea() {
        int lineas = doc.getDefaultRootElement().getElementCount(); // Obtenemos cantidad real de renglones
        StringBuilder sb = new StringBuilder();
        
        for (int i = 1; i <= lineas; i++) {
            sb.append(i).append(System.lineSeparator());
        }
        
        txtNumerosLineas.setText(sb.toString());
    }

    // --- DEFINICIÓN DE COLORES ---
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
        
        cadenaStyle = sc.addStyle("cadena", null);
        StyleConstants.setForeground(cadenaStyle, new Color(200, 20, 20));
    }

    // --- LÓGICA DE COLOREADO AUTOMÁTICO ---
    private void colorearTexto() {
        if (coloreando) return;
        coloreando = true;

        SwingUtilities.invokeLater(() -> {
            try {
                String texto = txtEntrada.getText();
                // Resetear a normal
                doc.setCharacterAttributes(0, texto.length(), normal, true);

                Matcher m;

                // 1. Palabras Reservadas y Estructuras
                m = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b").matcher(texto);
                while (m.find()) {
                    String palabra = m.group().toUpperCase();
                    if (ESTRUCTURAS_DATOS.contains(palabra)) {
                        doc.setCharacterAttributes(m.start(), m.end() - m.start(), estructuraDato, false);
                    } else if (PALABRAS_RESERVADAS.contains(palabra)) {
                        doc.setCharacterAttributes(m.start(), m.end() - m.start(), reservada, false);
                    }
                }

                // 2. Números
                m = Pattern.compile("\\b\\d+\\b").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), numero, false);
                }

                // 3. Operadores
                m = Pattern.compile("[=+\\-*/<>;(){}]").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), 1, operador, false);
                }
                
                // 4. Cadenas de texto
                m = Pattern.compile("\"[^\"]*\"").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), cadenaStyle, false);
                }

                // 5. Comentarios (Al final para que tenga prioridad)
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
            public void insertUpdate(DocumentEvent e) { 
                actualizarNumerosDeLinea(); // --- NUEVO: Actualizar números al escribir/dar enter
                timerColoreo.restart(); 
            }
            public void removeUpdate(DocumentEvent e) { 
                actualizarNumerosDeLinea(); // --- NUEVO: Actualizar números al borrar
                timerColoreo.restart(); 
            }
            public void changedUpdate(DocumentEvent e) { }
        });
    }

    // --- LÓGICA DE ANÁLISIS ---
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