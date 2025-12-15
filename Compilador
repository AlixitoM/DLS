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

public class Compilador extends JFrame {

    private JTextPane txtEntrada;         
    private JTextArea txtNumerosLineas;   
    private StyledDocument doc;           
    private JTextArea txtSintactico;      
    private JTable tablaSimbolos;         
    private JTable tablaErrores;          

    private DefaultTableModel modeloSimbolos;
    private DefaultTableModel modeloErrores;
    private JLabel lblResumen;

    private Style normal;
    private Style reservada;
    private Style numero;
    private Style operador;
    private Style errorStyle;
    private Style verdeComentario;
    private Style estructuraDato;
    private Style cadenaStyle;
    
    private Timer timerColoreo;
    private boolean coloreando = false;

    // Definimos las palabras para colorear azul
    private static final Set<String> PALABRAS_RESERVADAS = Set.of(
        "CREAR", "INSERTAR", "APILAR", "ENCOLAR", "MOSTRAR", "IF", "ELSE", 
        "ELIMINAR", "BUSCAR", "EN", "CON", "VALOR", "PUSH", "POP", "DEQUEUE", "ENQUEUE",
        "TOPE", "FRENTE", "TAMANO", "VACIAT"
    );
    // palabras para colorear naranja
    private static final Set<String> ESTRUCTURAS_DATOS = Set.of(
        "PILA", "COLA", "BICOLA", "LISTA_ENLAZADA", "LISTA_CIRCULAR", 
        "ARBOL_BINARIO", "TABLA_HASH", "GRAFO", "PILA_CIRCULAR"
    );

    public Compilador() {
        // Configuración de la Ventana Principal
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        JPanel panelCodigo = new JPanel(new BorderLayout(5, 5));
        panelCodigo.setBorder(BorderFactory.createTitledBorder(" Editor de Código DSL "));        
        txtEntrada = new JTextPane();
        txtEntrada.setFont(new Font("Consolas", Font.PLAIN, 14));
        doc = txtEntrada.getStyledDocument(); 
        
        // Configuración del Área de Números de Línea
        txtNumerosLineas = new JTextArea("1");
        txtNumerosLineas.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtNumerosLineas.setBackground(new Color(230, 230, 230));
        txtNumerosLineas.setForeground(Color.GRAY);
        txtNumerosLineas.setEditable(false);
        txtNumerosLineas.setMargin(new Insets(0, 5, 0, 5));

        JScrollPane scrollCodigo = new JScrollPane(txtEntrada);
        scrollCodigo.setRowHeaderView(txtNumerosLineas);
        scrollCodigo.setPreferredSize(new Dimension(1000, 200));
        
        JButton btnAnalizar = new JButton("Compilar (Analizar)");
        btnAnalizar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.setBackground(new Color(0, 120, 215)); // Azul Windows
        btnAnalizar.setFocusPainted(false);      // Quita el recuadro de foco feo
        btnAnalizar.setBorderPainted(false);     // Quita el borde 3D antiguo
        btnAnalizar.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Pone la manita al pasar el mouse
        btnAnalizar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        btnAnalizar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAnalizar.setBackground(new Color(0, 90, 170)); // Color más oscuro
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAnalizar.setBackground(new Color(0, 120, 215)); // Color original
            }
        });
        
        panelCodigo.add(scrollCodigo, BorderLayout.CENTER);
        panelCodigo.add(btnAnalizar, BorderLayout.EAST);

        // --- 2. PANEL CENTRAL: Pestañas de Resultados ---
        JTabbedPane pestañas = new JTabbedPane();

        // Pestaña A: Tabla de Símbolos (Léxico)
        String[] colsSimbolos = {"Lexema", "Línea", "Col", "Tipo Token"};
        modeloSimbolos = new DefaultTableModel(colsSimbolos, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaSimbolos = new JTable(modeloSimbolos);
        tablaSimbolos.setFillsViewportHeight(true);
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        pestañas.addTab("Análisis Léxico (Tokens)", scrollSimbolos);

        // aparatdo del arbol sintactico
        txtSintactico = new JTextArea();
        txtSintactico.setEditable(false);
        txtSintactico.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtSintactico.setForeground(new Color(40, 40, 40));
        JScrollPane scrollSintactico = new JScrollPane(txtSintactico);
        pestañas.addTab("Árbol de Derivación (Sintaxis)", scrollSintactico);

   // apartado del salida
        String[] colsErrores = {"Línea", "Tipo", "Descripción del Error"};
        modeloErrores = new DefaultTableModel(colsErrores, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaErrores = new JTable(modeloErrores);
        tablaErrores.setForeground(new Color(200, 0, 0)); // Rojo oscuro
        tablaErrores.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaErrores.setRowHeight(20);
        
        // Ajustar ancho de columnas de error
        tablaErrores.getColumnModel().getColumn(0).setPreferredWidth(60);  // Línea
        tablaErrores.getColumnModel().getColumn(0).setMaxWidth(80);
        tablaErrores.getColumnModel().getColumn(1).setPreferredWidth(100); // Tipo
        tablaErrores.getColumnModel().getColumn(1).setMaxWidth(150);
        
        JScrollPane scrollErrores = new JScrollPane(tablaErrores);
        scrollErrores.setBorder(BorderFactory.createTitledBorder(" Consola de Problemas "));
        scrollErrores.setPreferredSize(new Dimension(1000, 180));

        JSplitPane splitCentral = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pestañas, scrollErrores);
        splitCentral.setResizeWeight(0.60); 

        add(panelCodigo, BorderLayout.NORTH);
        add(splitCentral, BorderLayout.CENTER);

        lblResumen = new JLabel(" Listo para analizar.");
        lblResumen.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(lblResumen, BorderLayout.SOUTH);

        btnAnalizar.addActionListener(e -> ejecutarAnalisis());

        inicializarEstilos();
        habilitarColoreadoTiempoReal();
        colorearTexto(); // Coloreada inicial
        actualizarNumerosDeLinea(); // Actualización inicial de números
    }

    private void actualizarNumerosDeLinea() {
        int lineas = doc.getDefaultRootElement().getElementCount(); 
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lineas; i++) {
            sb.append(i).append(System.lineSeparator());
        }
        txtNumerosLineas.setText(sb.toString());
    }
// crea los estilos que tendran las palabras que sean coloreadas
    private void inicializarEstilos() {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        normal = sc.addStyle("normal", null);
        StyleConstants.setForeground(normal, Color.BLACK);

        reservada = sc.addStyle("reservada", null);
        StyleConstants.setForeground(reservada, new Color(0, 0, 180)); // Azul
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
        StyleConstants.setForeground(cadenaStyle, new Color(200, 20, 20)); // Rojo ladrillo
    }

    private void colorearTexto() {
        if (coloreando) return;
        coloreando = true;

        SwingUtilities.invokeLater(() -> {
            try {
                String texto = txtEntrada.getText();
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
                m = Pattern.compile("[=+\\-*/<>;(){},]").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), 1, operador, false);
                }
                
                // 4. Cadenas
                m = Pattern.compile("\"[^\"]*\"").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), cadenaStyle, false);
                }

                // 5. Comentarios
                m = Pattern.compile("//.*").matcher(texto);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), verdeComentario, false);
                }

            } catch (Exception e) {
                // Ignorar
            } finally {
                coloreando = false;
            }
        });
    }

    private void habilitarColoreadoTiempoReal() {
       // usamos un timer para que solo se coloreen ya que s etermine de colrear, ya que sinopuede causar conflictos con el color y causar incongruencias
        timerColoreo = new Timer(300, e -> colorearTexto());
        timerColoreo.setRepeats(false);
// agrega un manejador de eventos al documento 
        txtEntrada.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { 
                // si se inserta se actualizan los numeros de linea y se vulve a colorear
                actualizarNumerosDeLinea(); 
                timerColoreo.restart(); 
            }
            public void removeUpdate(DocumentEvent e) { 
                actualizarNumerosDeLinea(); 
                timerColoreo.restart(); 
            }
            public void changedUpdate(DocumentEvent e) { }
        });
    }


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
           // genera los tokens con el codigo recibido 
            Token[] tokens = DSLCore.tokenizador(codigo);
           // crea el automata 
            Automata auto = new Automata(DSLCore.getEstadosDSL(),DSLCore.getAlfabetoDSL(),DSLCore.getTransicionesDSL(),"Inicio",DSLCore.getEstadosAceptacionDSL());
            
            // valida los tokens con el automata
            Token[] resultadosLexicos = auto.aceptar(tokens);

            
            // los tokens validos los agrega a un arraylist
            List<Token> tokensValidos = new ArrayList<>();

            
            // recorre los tokens aceptados
            for (Token t : resultadosLexicos) {
                // Si el token es un error léxico
                if (t.getTipoToken().startsWith("ERROR") || !t.existeSimbolo()) {
                    
                // les asigna su error
                    String codigoError = "DSL(100)"; // Genérico
                    if (t.getTipoToken().contains("CADENA")) codigoError = "DSL(102)"; // Cadena incompleta
                    else if (t.getTipoToken().contains("SIMBOLO")) codigoError = "DSL(101)"; // Símbolo raro
                    else if (t.getTipoToken().contains("MALFORMADO")) codigoError = "DSL(103)";
                    
                    String desc = String.format("%s Lexema '%s' no válido. Causa: %s", 
                                                codigoError, t.getLexema(), t.getTipoToken());
                    
                    listaErroresUnificada.add(new ErrorReporte(t.getLinea(), "LÉXICO", desc));
                } else {
                    // Token válido, lo añadimos para el parser
                    tokensValidos.add(t);
                    modeloSimbolos.addRow(new Object[]{
                        t.getLexema(), t.getLinea(), t.getColumna(), t.getTipoToken()
                    });
                }
            }

            if (!tokensValidos.isEmpty()) {
                Token[] arrayTokensValidos = tokensValidos.toArray(new Token[0]);
                
                AnalizadorSintactico sintactico = new AnalizadorSintactico(arrayTokensValidos);
                sintactico.analizar(); 

                // crea el arbol de derivacion
                List<String> log = sintactico.getLogDerivacion();
                StringBuilder sb = new StringBuilder();
                for (String paso : log) {
                    sb.append(paso).append("\n");
                }
                txtSintactico.setText(sb.toString());
                txtSintactico.setCaretPosition(0); 

                // le da formato a los errores con la asignacion del parser 
                List<String> erroresSin = sintactico.getErrores();
                for (String errStr : erroresSin) {
                    int linea = 0;
                    String descripcion = errStr;

                    try {
                        if (errStr.contains("[Línea ")) {
                            int inicioNum = errStr.indexOf("[Línea ") + 7;
                            int finNum = errStr.indexOf("]");
                            
                            if (inicioNum < finNum) {
                                String numStr = errStr.substring(inicioNum, finNum);
                                linea = Integer.parseInt(numStr.trim());
                            }
                            
                       
                            if (errStr.contains("]: ")) {
                                String idPart = errStr.substring(0, errStr.indexOf("[")); // "DSL(201) "
                                String msgPart = errStr.substring(errStr.indexOf("]: ") + 3); // "Mensaje"
                                descripcion = idPart + msgPart;
                            }
                        }
                    } catch (Exception ex) {
                        linea = 0; 
                    }
                    
                    listaErroresUnificada.add(new ErrorReporte(linea, "SINTÁCTICO", descripcion));
                }
            } else {
                txtSintactico.setText("No hay tokens válidos para analizar sintácticamente.");
            }

            Collections.sort(listaErroresUnificada);

            for (ErrorReporte err : listaErroresUnificada) {
                modeloErrores.addRow(new Object[]{
                    (err.linea > 0 ? err.linea : "-"), 
                    err.tipo, 
                    err.descripcion 
                });
            }

                // glosario de errores
            int totalErrores = listaErroresUnificada.size();
            if (totalErrores == 0) {
                lblResumen.setText(" Analisis Finalizado Exitosamente. El código es correcto.");
                lblResumen.setForeground(new Color(0, 128, 0)); 
            } else {
                lblResumen.setText("  Se encontraron " + totalErrores + " errores.");
                lblResumen.setForeground(Color.RED);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error crítico en el compilador: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Look and Feel nativo para que se vea moderno
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> new Compilador().setVisible(true));
    }

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
            // Ordenar por línea ascendente
            return Integer.compare(this.linea, o.linea);
        }
    }
}
