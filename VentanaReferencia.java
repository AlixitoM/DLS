import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

public class VentanaReferencia extends JFrame {

    public VentanaReferencia(String titulo, Component contenido) {
        setTitle(titulo);
        setSize(700, 750); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(contenido, BorderLayout.CENTER);
    }

    public static void mostrarTablaSimbolos() {

        Map<String, String> mapaOriginal = Automata.getPalabrasReservadas();
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
        scroll.setBorder(BorderFactory.createTitledBorder(" Diccionario de Palabras Clave y Tipos "));

        new VentanaReferencia("Referencia: Tabla de Símbolos", scroll).setVisible(true);
    }

    public static void mostrarGramatica() {
        JTextArea txtGramatica = new JTextArea();
        txtGramatica.setEditable(false);
        txtGramatica.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtGramatica.setText(getTextoGramatica());
        txtGramatica.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(txtGramatica);
        scroll.setBorder(BorderFactory.createTitledBorder(" Especificación Formal EBNF Actualizada "));

        new VentanaReferencia("Referencia: Gramática del DSL", scroll).setVisible(true);
    }

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
            "2. DECLARACIONES (Variables y Estructuras)\n" +
            "<Declaracion> ::= \"CREAR\" <Tipo> \"IDENTIFICADOR\" [ <Inicializacion> | LITERAL_NUMERICA ] \";\"\n" +
            "<Tipo>        ::= <Tipo_Estructura> | <Tipo_Primitivo>\n" +
            "<Tipo_Primitivo> ::= \"NUMERO\" | \"TEXTO\"\n" +
            "<Tipo_Estructura> ::= \"PILA\" | \"COLA\" | \"LISTA_ENLAZADA\" | \"ARBOL_BINARIO\" | \"TABLA_HASH\" | \"GRAFO\"\n" +
            "<Inicializacion>  ::= \"=\" <Expresion>\n\n" +
            "3. CONTROL DE FLUJO (Bucles e If)\n" +
            "<Control_Flujo> ::= <If_Statement> | <While_Statement> | <For_Statement> | <Do_While_Statement>\n\n" +
            "   A. Condicional IF:\n" +
            "      \"IF\" \"(\" <Condicion> \")\" \"{\" <Programa> \"}\" [ \"ELSE\" \"{\" <Programa> \"}\" ]\n\n" +
            "   B. Bucle WHILE:\n" +
            "      \"WHILE\" \"(\" <Condicion> \")\" \"{\" <Programa> \"}\"\n\n" +
            "   C. Bucle FOR:\n" +
            "      \"FOR\" \"(\" <Asignacion> \";\" <Condicion> \";\" <Incremento> \")\" \"{\" <Programa> \"}\"\n" +
            "      <Incremento> ::= \"IDENTIFICADOR\" \"=\" <Expresion>\n\n" +
            "   D. Bucle DO-WHILE:\n" +
            "      \"DO\" \"{\" <Programa> \"}\" \"WHILE\" \"(\" <Condicion> \")\" \";\"\n\n" +
            "4. OPERACIONES DE ESTRUCTURA\n" +
            "   <Op> ::= <Verbo_Insertar> <Expresion> \"EN\" \"IDENTIFICADOR\" \";\"\n" +
            "   <Op> ::= <Verbo_Eliminar> \"EN\" \"IDENTIFICADOR\" \";\"\n\n" +
            "5. EXPRESIONES Y CONDICIONES\n" +
            "<Expresion> ::= <Termino> { (\"+\" | \"-\") <Termino> }\n" +
            "<Condicion> ::= <Expresion> (\"==\"|\"!=\"|\"<\"|\">\"|\"<=\"|\">=\") <Expresion>\n\n" +
            "6. SALIDA Y ASIGNACIÓN\n" +
            "<Salida>     ::= \"MOSTRAR\" <Expresion> \";\"\n" +
            "<Asignacion> ::= \"IDENTIFICADOR\" \"=\" <Expresion>";
    }
}