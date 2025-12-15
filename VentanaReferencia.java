import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

public class VentanaReferencia extends JFrame {

    public VentanaReferencia(String titulo, Component contenido) {
        setTitle(titulo);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Solo cierra esta ventana, no la app
        setLayout(new BorderLayout());
        add(contenido, BorderLayout.CENTER);
    }

    // --- OPCIÓN 1: TABLA DE SÍMBOLOS (Extraída del AFD) ---
    public static void mostrarTablaSimbolos() {
        // Obtenemos el mapa del AFD
        Map<String, String> mapaOriginal = AFD.getPalabrasReservadas();
        
        // Usamos TreeMap para que se ordenen alfabéticamente automáticamente
        Map<String, String> mapaOrdenado = new TreeMap<>(mapaOriginal);

        String[] columnas = {"Palabra Reservada / Token", "Categoría / Tipo"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Map.Entry<String, String> entrada : mapaOrdenado.entrySet()) {
            modelo.addRow(new Object[]{entrada.getKey(), entrada.getValue()});
        }

        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setRowHeight(25);
        
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder(" Palabras Reservadas Disponibles (Análisis Léxico) "));

        // Crear y mostrar la ventana
        new VentanaReferencia("Referencia: Tabla de Símbolos", scroll).setVisible(true);
    }

    // --- OPCIÓN 2: GRAMÁTICAS (Texto estático) ---
    public static void mostrarGramatica() {
        JTextArea txtGramatica = new JTextArea();
        txtGramatica.setEditable(false);
        txtGramatica.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtGramatica.setText(getTextoGramatica());
        txtGramatica.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(txtGramatica);
        scroll.setBorder(BorderFactory.createTitledBorder(" Especificación BFN / EBNF (Análisis Sintáctico) "));

        new VentanaReferencia("Referencia: Gramática del DSL", scroll).setVisible(true);
    }

    // El texto enorme de la gramática se guarda aquí para no ensuciar el código principal
    private static String getTextoGramatica() {
        return 
            "=== GRAMÁTICA LIBRE DE CONTEXTO (Formato EBNF) ===\n\n" +
            "Símbolo Inicial: <Programa>\n\n" +
            "1. ESTRUCTURA GENERAL\n" +
            "<Programa> ::= <Sentencia> <Programa> | ε\n" +
            "<Sentencia> ::= <Declaracion> \n" +
            "              | <Operacion_Estructura> \n" +
            "              | <Control_Flujo> \n" +
            "              | <Salida> \n" +
            "              | <Asignacion>\n" +
            "              | \";\"\n\n" +
            "2. DECLARACIONES\n" +
            "<Declaracion> ::= \"CREAR\" <Tipo_Estructura> \"IDENTIFICADOR\" [LITERAL_NUMERICA] \";\"\n" +
            "<Tipo_Estructura> ::= \"PILA\" | \"PILA_CIRCULAR\" | \"COLA\" | \"BICOLA\" \n" +
            "                    | \"LISTA_ENLAZADA\" | \"LISTA_DOBLE_ENLAZADA\" | \"LISTA_CIRCULAR\" \n" +
            "                    | \"ARBOL_BINARIO\" | \"TABLA_HASH\" | \"GRAFO\"\n\n" +
            "3. OPERACIONES DE ESTRUCTURA\n" +
            "   A. Inserciones Simples:\n" +
            "      <Op> ::= <Verbo_Insertar> <Expresion> \"EN\" \"IDENTIFICADOR\" \";\"\n" +
            "      <Verbo_Insertar> ::= APILAR | PUSH | ENCOLAR | ENQUEUE | INSERTAR...\n\n" +
            "   B. Inserciones Complejas:\n" +
            "      <Op> ::= \"INSERTAR_EN_POSICION\" <Expresion> \"EN\" ID \"CON\" \"VALOR\" <Expresion> \";\"\n" +
            "      <Op> ::= (\"INSERTARIZQUIERDA\" | \"INSERTARDERECHA\") <Expresion> \"EN\" ID \";\"\n" +
            "      <Op> ::= \"AGREGARARISTA\" <Expresion> <Expresion> \"EN\" ID \";\"\n\n" +
            "   C. Eliminaciones:\n" +
            "      <Op> ::= <Verbo_Eliminar> \"EN\" \"IDENTIFICADOR\" \";\"\n" +
            "      <Op> ::= \"ELIMINAR_POSICION\" <Expresion> \"EN\" ID \";\"\n\n" +
            "4. RECORRIDOS Y BÚSQUEDAS\n" +
            "<Op> ::= <Verbo_Recorrido> \"EN\" \"IDENTIFICADOR\" \";\"\n" +
            "<Verbo_Recorrido> ::= RECORRER | PREORDEN | INORDEN | POSTORDEN | BFS | DFS...\n\n" +
            "5. EXPRESIONES Y PROPIEDADES\n" +
            "<Expresion> ::= <Termino> { (\"+\" | \"-\") <Termino> }\n" +
            "<Termino>   ::= <Factor>  { (\"*\" | \"/\") <Factor> }\n" +
            "<Factor>    ::= ID | NUMERO | CADENA | \"(\" <Expresion> \")\" | <Propiedad>\n" +
            "<Propiedad> ::= <Verbo_Prop> \"EN\" ID\n" +
            "<Verbo_Prop> ::= TOPE | FRENTE | TAMANO | ALTURA | HOJAS...\n\n" +
            "6. CONTROL DE FLUJO\n" +
            "<Control_Flujo> ::= \"IF\" \"(\" <Condicion> \")\" \"{\" <Programa> \"}\" [ \"ELSE\" \"{\" <Programa> \"}\" ]\n" +
            "<Condicion> ::= <Expresion> (\"==\"|\"!=\"|\"<\"|\">\"|\"<=\"|\">=\") <Expresion>\n\n" +
            "7. SALIDA Y ASIGNACIÓN\n" +
            "<Salida> ::= \"MOSTRAR\" <Expresion> \";\"\n" +
            "<Asignacion> ::= \"IDENTIFICADOR\" \"=\" <Expresion> \";\"";
    }
}