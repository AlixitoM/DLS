import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Autómata Finito Determinista (AFD) para reconocer palabras reservadas.
 * Los demás tipos léxicos (Identificadores, Números, Operadores)
 * se clasifican de forma auxiliar.
 */
public class AFD {
    private final Set<String> estados; 
    private final Set<Character> alfabeto; 
    private final Map<String, Map<Character, String>> transiciones;
    private final String estadoInicial; 
    private final Set<String> estadosAceptacion; 
    
    // Mapeo directo de Palabras Reservadas (PR) a sus Tipos (usado después de aceptar por el AFD)
    private static final Map<String, String> TIPO_POR_PR = Map.ofEntries(
        // Estructuras de Datos
        Map.entry("PILA", "ESTR_PILA"),
        Map.entry("PILA_CIRCULAR", "ESTR_PILA_CIRCULAR"),
        Map.entry("PILA_SIMPLE", "ESTR_PILA_SIMPLE"),
        Map.entry("COLA", "ESTR_COLA"),
        Map.entry("LISTA_SIMPLE", "ESTR_LISTA_SIMPLE"),
        Map.entry("LISTA_DOBLE", "ESTR_LISTA_DOBLE"),
        Map.entry("LISTA_CIRCULAR", "ESTR_LISTA_CIRCULAR"),
        Map.entry("ARBOL", "ESTR_ARBOL"),
        Map.entry("GRAFO", "ESTR_GRAFO"),
        // Acciones/Operaciones
        Map.entry("INSERTAR", "ACCION_GENERICA"),
        Map.entry("ELIMINAR", "ACCION_GENERICA"),
        Map.entry("BUSCAR", "ACCION_GENERICA"),
        Map.entry("MOSTRAR", "ACCION_GENERICA"),
        Map.entry("RECORRER", "ACCION_GENERICA"),
        Map.entry("PUSH", "ACCION_PILA"),
        Map.entry("POP", "ACCION_PILA"),
        Map.entry("ENQUEUE", "ACCION_COLA"),
        Map.entry("DEQUEUE", "ACCION_COLA"),
        Map.entry("FRONT", "ACCION_COLA"),
        Map.entry("AGREGAR_VERTICE", "ACCION_GRAFO"),
        Map.entry("AGREGAR_ARISTA", "ACCION_GRAFO"),
        Map.entry("ELIMINAR_VERTICE", "ACCION_GRAFO"),
        Map.entry("ELIMINAR_ARISTA", "ACCION_GRAFO"),
        Map.entry("BFS", "ACCION_GRAFO"),
        Map.entry("DFS", "ACCION_GRAFO"),
        Map.entry("CAMINO_MINIMO", "ACCION_GRAFO"),
        Map.entry("RECORRER_INORDEN", "ACCION_ARBOL"),
        Map.entry("RECORRER_PREORDEN", "ACCION_ARBOL"),
        Map.entry("RECORRER_POSTORDEN", "ACCION_ARBOL"),
        // Palabras Auxiliares
        Map.entry("EN", "AUXILIAR"),
        Map.entry("CON", "AUXILIAR"),
        Map.entry("VALOR", "AUXILIAR")
    );


    public AFD(Set<String> estados,
               Set<Character> alfabeto,
               Map<String, Map<Character, String>> transiciones,
               String estadoInicial,
               Set<String> estadosAceptacion) {
        this.estados = estados;
        this.alfabeto = alfabeto;
        this.transiciones = transiciones;
        this.estadoInicial = estadoInicial;
        this.estadosAceptacion = estadosAceptacion;
    }

    /**
     * Procesa los lexemas tokenizados para determinar su tipo.
     * Si es una Palabra Reservada (PR), usa el AFD.
     * Si no es una PR, usa la función auxiliar determinarTipoLexema.
     * @param tokens Arreglo de tokens con solo lexema y línea.
     * @return Arreglo de tokens clasificados.
     */
    public Token[] aceptar(Token[] tokens) {
        Token[] resultados = new Token[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            Token tk = tokens[i];
            String lexema = tk.getLexema();
            String estadoActual = estadoInicial;
            boolean esPR = true;
            
            // 1. Intentar procesar como Palabra Reservada (PR)
            for (char simbolo : lexema.toUpperCase().toCharArray()) { 
                Map<Character, String> transicionesEstado = transiciones.get(estadoActual);
                
                // Si el símbolo no está en el alfabeto o no hay transición
                if (transicionesEstado == null || !transicionesEstado.containsKey(simbolo)) {
                    esPR = false;
                    break; 
                }
                estadoActual = transicionesEstado.get(simbolo);
            }
            
            // 2. Clasificación final del Token
            if (esPR && estadosAceptacion.contains(estadoActual)) {
                // Es una Palabra Reservada reconocida por el AFD
                String tipo = TIPO_POR_PR.getOrDefault(lexema.toUpperCase(), "PR_DESCONOCIDA");
                resultados[i] = new Token(lexema, tk.getLinea(), tipo, estadoActual, true);
            } else {
                // No es una PR, usar la clasificación auxiliar (Regex y Switch)
                String tipo = determinarTipoLexema(lexema);
                boolean reconocido = !tipo.equals("ERROR_LEXICO");

                // El estado final solo es relevante para PR, si no, se deja el último estado alcanzado o "N/A"
                String estadoReporte = esPR ? estadoActual : "N/A";
                resultados[i] = new Token(lexema, tk.getLinea(), tipo, estadoReporte, reconocido);
            }
        }

        return resultados;
    }

    /**
     * Clasifica un lexema que no fue reconocido como Palabra Reservada.
     * Se usa para Identificadores, Números, Operadores y Errores.
     */
    public static String determinarTipoLexema(String lexema) {
        // 1. Delimitadores y Símbolos Simples y Compuestos
        switch (lexema) {
            case ";": return "DELIMITADOR";
            case "(": return "PARENTESIS_IZQ";
            case ")": return "PARENTESIS_DER";
            case "[": return "CORCHETE_IZQ";
            case "]": return "CORCHETE_DER";
            case ",": return "COMA";
            case "=": return "ASIGNACION";
            case "+": return "OP_SUMA";
            case "-": return "OP_RESTA";
            case "*": return "OP_MULTIPLICACION";
            case "/": return "OP_DIVISION";
            case "<": return "OP_MENOR_QUE";
            case ">": return "OP_MAYOR_QUE";
            case "==": return "OP_IGUAL";
            case "!=": return "OP_DIFERENTE";
            case "<=": return "OP_MENOR_IGUAL";
            case ">=": return "OP_MAYOR_IGUAL";
        }

        // 2. Literales Numéricos (Enteros y Flotantes)
        // Patrón: ^[+-]?(\d+\.?\d*|\.\d+)$ (Permite 10, 10.5, .5, +10, -10.5)
        if (lexema.matches("^[+-]?(\\d+\\.?\\d*|\\.\\d+)$")) {
            return "LITERAL_NUMERICO";
        }
        
        // 3. Literales de Cadena (opcional, si se usa en el DSL)
        // Patrón: ^\"[^\"]*\"$
        if (lexema.matches("^\"[^\"]*\"$")) {
            return "LITERAL_CADENA";
        }

        // 4. Identificador (comienza con letra o _, seguido de letras, dígitos o _)
        // Patrón: ^[A-Za-z_][A-Za-z0-9_]*$
        if (lexema.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            return "IDENTIFICADOR";
        }
        
        // 5. Error
        return "ERROR_LEXICO";
    }
}