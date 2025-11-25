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
        Map.entry("COLA", "ESTR_COLA"),
        Map.entry("BICOLAS", "ESTR_DEQUE"),
        Map.entry("LISTA_ENLAZADAS", "ESTR_LISTA_SIMPLE"),
        Map.entry("LISTA_DOBLE_ENLAZADA", "ESTR_LISTA_DOBLE"),
        Map.entry("LISTA_CIRCULAR", "ESTR_LISTA_CIRCULAR"),
        Map.entry("ARBOL_BINARIO", "ESTR_ARBOL"),
        Map.entry("TABLAS_HASH", "ESTR_TABLA_HASH"),
        Map.entry("GRAFOS", "ESTR_GRAFO"),
        // Acciones/Operaciones (Combinado y Renombrado)
        Map.entry("INSERTAR", "ACCION_INSERCION"),
        Map.entry("INSERTAR_FINAL", "ACCION_INSERCION"),
        Map.entry("INSERTAR_INICIO", "ACCION_INSERCION"),
        Map.entry("INSERTAR_EN_POSICION", "ACCION_INSERCION"),
        Map.entry("INSERTARIZQUIERDA", "ACCION_ARBOL"),
        Map.entry("INSERTARDERECHA", "ACCION_ARBOL"),
        Map.entry("AGREGARNODO", "ACCION_GRAFO"),
        Map.entry("APILAR", "ACCION_PILA"),
        Map.entry("ENCOLAR", "ACCION_COLA"),
        Map.entry("PUSH", "ACCION_PILA"), // Mantenido para compatibilidad
        Map.entry("ENQUEUE", "ACCION_COLA"), // Mantenido para compatibilidad

        Map.entry("ELIMINAR", "ACCION_ELIMINACION"),
        Map.entry("ELIMINAR_INICIO", "ACCION_ELIMINACION"),
        Map.entry("ELIMINAR_FINAL", "ACCION_ELIMINACION"),
        Map.entry("ELIMINAR_FRENTE", "ACCION_ELIMINACION"),
        Map.entry("ELIMINAR_POSICION", "ACCION_ELIMINACION"),
        Map.entry("ELIMINARNODO", "ACCION_GRAFO"),
        Map.entry("DESAPILAR", "ACCION_PILA"),
        Map.entry("POP", "ACCION_PILA"), // Mantenido para compatibilidad
        Map.entry("DESENCOLAR", "ACCION_COLA"),
        Map.entry("DEQUEUE", "ACCION_COLA"), // Mantenido para compatibilidad

        Map.entry("BUSCAR", "ACCION_BUSQUEDA"),
        Map.entry("TOPE", "ACCION_ACCESO"),
        Map.entry("FRENTE", "ACCION_ACCESO"),
        Map.entry("PEEK", "ACCION_ACCESO"), // Mantenido para compatibilidad
        Map.entry("VERFILA", "ACCION_ACCESO"),
        Map.entry("FRONT", "ACCION_ACCESO"), // Mantenido para compatibilidad
        Map.entry("CLAVE", "ACCION_ACCESO"),
        
        Map.entry("RECORRER", "ACCION_RECORRIDO"),
        Map.entry("RECORRERADELANTE", "ACCION_RECORRIDO"),
        Map.entry("RECORRERATRAS", "ACCION_RECORRIDO"),
        Map.entry("PREORDEN", "ACCION_ARBOL"),
        Map.entry("INORDEN", "ACCION_ARBOL"),
        Map.entry("POSTORDEN", "ACCION_ARBOL"),
        Map.entry("RECORRIDOPORNIVELES", "ACCION_ARBOL"),

        // Acciones Genéricas/Misceláneas
        Map.entry("ACTUALIZAR", "ACCION_MISC"),
        Map.entry("REHASH", "ACCION_MISC"),
        Map.entry("AGREGARARISTA", "ACCION_GRAFO"),
        Map.entry("ELIMINARARISTA", "ACCION_GRAFO"),
        Map.entry("VECINOS", "ACCION_GRAFO"),
        Map.entry("BFS", "ACCION_GRAFO"),
        Map.entry("DFS", "ACCION_GRAFO"),
        Map.entry("CAMINOCORTO", "ACCION_GRAFO"),

        // Propiedades/Verificación
        Map.entry("VACIAT", "ESTADO_BOOLEANO"),
        Map.entry("LLENAT", "ESTADO_BOOLEANO"),
        Map.entry("TAMAÑO", "PROPIEDAD"),
        Map.entry("ALTURA", "PROPIEDAD"),
        Map.entry("HOJAS", "PROPIEDAD"),
        Map.entry("NODOS", "PROPIEDAD"),
        
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
            case "{": return "LLAVE_IZQ";
            case "}": return "LLAVE_DER";
            case "if": return "PC_IF";
            case "else": return "PC_ELSE";
            case "&&": return "OP_LOGICO";
            case "||": return "OP_LOGICO";
        }

        // 2. Literales Numéricos (Enteros y Flotantes)
        if (lexema.matches("^[+-]?(\\d+\\.?\\d*|\\.\\d+)$")) {
            return "LITERAL_NUMERICO";
        }
        
        // 3. Literales de Cadena (si el tokenizador lo soporta, aunque aquí solo se separa por espacio)
        if (lexema.matches("^\"[^\"]*\"$")) {
            return "LITERAL_CADENA";
        }

        // 4. Identificador (comienza con letra o _, seguido de letras, dígitos o _)
        if (lexema.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            return "IDENTIFICADOR";
        }
        
        // 5. Error
        return "ERROR_LEXICO";
    }
}