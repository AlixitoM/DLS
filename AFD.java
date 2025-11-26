import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Autómata Finito Determinista (AFD) para reconocer palabras reservadas.
 * Los demás tipos léxicos (Números, Cadenas, Identificadores) son
 * clasificados usando funciones estáticas que simulan un AFD, reemplazando las RegEx.
 */
public class AFD {
    private final Set<String> estados;
    private final Set<Character> alfabeto;
    private final Map<String, Map<Character, String>> transiciones;
    private final String estadoInicial;
    private final Set<String> estadosAceptacion;
    
    // Mapeo directo de Palabras Reservadas (PR) a sus Tipos
    private static final Map<String, String> TIPO_POR_PR = Map.ofEntries(
        // Estructuras de Datos
        Map.entry("PILA", "PALABRA_RESERVADA"),
        Map.entry("PILA_CIRCULAR", "PALABRA_RESERVADA"),
        Map.entry("COLA", "PALABRA_RESERVADA"),
        Map.entry("BICOLAS", "PALABRA_RESERVADA"),
        Map.entry("LISTA_ENLAZADAS", "PALABRA_RESERVADA"),
        Map.entry("LISTA_DOBLE_ENLAZADA", "PALABRA_RESERVADA"),
        Map.entry("LISTA_CIRCULAR", "PALABRA_RESERVADA"),
        Map.entry("ARBOL_BINARIO", "PALABRA_RESERVADA"),
        Map.entry("TABLAS_HASH", "PALABRA_RESERVADA"),
        Map.entry("GRAFOS", "PALABRA_RESERVADA"),
        
        // Acciones/Operaciones 
        Map.entry("INSERTAR", "PALABRA_RESERVADA"),
        Map.entry("INSERTAR_FINAL", "PALABRA_RESERVADA"),
        Map.entry("INSERTAR_INICIO", "PALABRA_RESERVADA"),
        Map.entry("INSERTAR_EN_POSICION", "PALABRA_RESERVADA"),
        Map.entry("INSERTARIZQUIERDA", "PALABRA_RESERVADA"),
        Map.entry("INSERTARDERECHA", "PALABRA_RESERVADA"),
        Map.entry("AGREGARNODO", "PALABRA_RESERVADA"),
        Map.entry("APILAR", "PALABRA_RESERVADA"),
        Map.entry("ENCOLAR", "PALABRA_RESERVADA"),
        Map.entry("PUSH", "PALABRA_RESERVADA"),
        Map.entry("ENQUEUE", "PALABRA_RESERVADA"),

        Map.entry("ELIMINAR", "PALABRA_RESERVADA"),
        Map.entry("ELIMINAR_INICIO", "PALABRA_RESERVADA"),
        Map.entry("ELIMINAR_FINAL", "PALABRA_RESERVADA"),
        Map.entry("ELIMINAR_FRENTE", "PALABRA_RESERVADA"),
        Map.entry("ELIMINAR_POSICION", "PALABRA_RESERVADA"),
        Map.entry("ELIMINARNODO", "PALABRA_RESERVADA"),
        Map.entry("DESAPILAR", "PALABRA_RESERVADA"),
        Map.entry("POP", "PALABRA_RESERVADA"),
        Map.entry("DESENCOLAR", "PALABRA_RESERVADA"),
        Map.entry("DEQUEUE", "PALABRA_RESERVADA"),

        Map.entry("BUSCAR", "PALABRA_RESERVADA"),
        Map.entry("TOPE", "PALABRA_RESERVADA"),
        Map.entry("FRENTE", "PALABRA_RESERVADA"),
        Map.entry("VERFILA", "PALABRA_RESERVADA"),
        Map.entry("FRONT", "PALABRA_RESERVADA"),
        Map.entry("CLAVE", "PALABRA_RESERVADA"),
        
        Map.entry("RECORRER", "PALABRA_RESERVADA"),
        Map.entry("RECORRERADELANTE", "PALABRA_RESERVADA"),
        Map.entry("RECORRERATRAS", "PALABRA_RESERVADA"),
        Map.entry("PREORDEN", "PALABRA_RESERVADA"),
        Map.entry("INORDEN", "PALABRA_RESERVADA"),
        Map.entry("POSTORDEN", "PALABRA_RESERVADA"),
        Map.entry("RECORRIDOPORNIVELES", "PALABRA_RESERVADA"),

        // Misceláneas
        Map.entry("ACTUALIZAR", "PALABRA_RESERVADA"),
        Map.entry("REHASH", "PALABRA_RESERVADA"),
        Map.entry("AGREGARARISTA", "PALABRA_RESERVADA"),
        Map.entry("ELIMINARARISTA", "PALABRA_RESERVADA"),
        Map.entry("VECINOS", "PALABRA_RESERVADA"),
        Map.entry("BFS", "PALABRA_RESERVADA"),
        Map.entry("DFS", "PALABRA_RESERVADA"),
        Map.entry("CAMINOCORTO", "PALABRA_RESERVADA"),

        // Propiedades/Verificación
        Map.entry("VACIAT", "PALABRA_RESERVADA"),
        Map.entry("LLENAT", "PALABRA_RESERVADA"),
        Map.entry("TAMAÑO", "PALABRA_RESERVADA"),
        Map.entry("ALTURA", "PALABRA_RESERVADA"),
        Map.entry("HOJAS", "PALABRA_RESERVADA"),
        Map.entry("NODOS", "PALABRA_RESERVADA"),
        
        // Palabras Auxiliares/Control
        Map.entry("EN", "PALABRA_RESERVADA"),
        Map.entry("CON", "PALABRA_RESERVADA"),
        Map.entry("VALOR", "PALABRA_RESERVADA"),
        Map.entry("IF", "PC_IF"),
        Map.entry("ELSE", "PC_ELSE"),
        Map.entry("MOSTRAR", "PALABRA_RESERVADA")
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
     * Procesa los lexemas tokenizados iniciales para determinar su tipo,
     * implementando el escaneo carácter por carácter para PR/ID.
     */
    public Token[] aceptar(Token[] tokensTokensIniciales) {
        List<Token> resultados = new ArrayList<>();

        for (Token tk : tokensTokensIniciales) {
            String lexema = tk.getLexema();
            String lexemaUpper = lexema.toUpperCase();
            int linea = tk.getLinea();
            
            // --- 1. CLASIFICACIÓN AUXILIAR RÁPIDA (Operadores/Delimitadores, Números, Cadenas) ---
            
            String tipoAuxiliar = determinarTipoLexema(lexema);

            // Si es un tipo clasificado por la lógica auxiliar, lo añadimos y pasamos al siguiente token.
            if (!tipoAuxiliar.startsWith("ERROR") && !tipoAuxiliar.equals("IDENTIFICADOR")) {
                resultados.add(new Token(lexema, linea, tipoAuxiliar, "N/A", true));
                continue;
            }
            
            // --- 2. ESCANEO DETALLADO CON EL AFD (Palabras Reservadas/Identificadores) ---
            
            String estadoActual = estadoInicial;
            String ultimoEstadoAceptado = null;
            int ultimoCaracterAceptado = -1; 
            
            boolean esPR = true;
            for (int j = 0; j < lexemaUpper.length(); j++) {
                char simbolo = lexemaUpper.charAt(j);
                
                Map<Character, String> transicionesEstado = transiciones.get(estadoActual);
                
                if (transicionesEstado != null && transicionesEstado.containsKey(simbolo)) {
                    // Transición exitosa
                    estadoActual = transicionesEstado.get(simbolo);
                    
                    // Si el nuevo estado es de aceptación, registramos la posición y el estado
                    if (estadosAceptacion.contains(estadoActual)) {
                        ultimoEstadoAceptado = estadoActual;
                        ultimoCaracterAceptado = j;
                    }
                } else {
                    // DETECCIÓN DE INCONSISTENCIA: No hay camino para el símbolo actual (j)
                    esPR = false;
                    break;
                }
            }
            
            // --- 3. CLASIFICACIÓN FINAL Y RECUPERACIÓN ---

            String tipoFinalAuxiliar = determinarTipoLexema(lexema);
            String tipoFinal = TIPO_POR_PR.getOrDefault(lexemaUpper, tipoFinalAuxiliar);

            String estadoReporte = "N/A";
            boolean reconocido = true;

            if (esPR && estadosAceptacion.contains(estadoActual)) {
                // Caso 1: Lexema consumido completamente y aceptado por el AFD.
                estadoReporte = estadoActual;
                
            } else if (tipoFinalAuxiliar.equals("IDENTIFICADOR")) {
                // Caso 2: El lexema es un ID estructuralmente válido (Incluye PRs largas que fallaron el Caso 1).
                
                // Si la clasificación final es PALABRA_RESERVADA (por TIPO_POR_PR),
                // usamos el lexema Upper como estado final para el reporte.
                if (tipoFinal.equals("PALABRA_RESERVADA") || tipoFinal.equals("PC_IF") || tipoFinal.equals("PC_ELSE")) {
                    estadoReporte = lexemaUpper;
                } else {
                    estadoReporte = "N/A"; // ID genérico
                }
                
            } else if (ultimoEstadoAceptado != null && ultimoCaracterAceptado < lexemaUpper.length() - 1) {
                // Caso 3: Falló en un carácter intermedio/final (Error con prefijo PR).
                
                String lexemaValido = lexema.substring(0, ultimoCaracterAceptado + 1);
                String lexemaRestante = lexema.substring(ultimoCaracterAceptado + 1);
                
                // 1. Clasificar la parte válida (la subcadena más larga)
                String tipoPR = TIPO_POR_PR.getOrDefault(lexemaValido.toUpperCase(), "IDENTIFICADOR");
                resultados.add(new Token(lexemaValido, linea, tipoPR, ultimoEstadoAceptado, true));
                
                // 2. Clasificar la parte restante como ERROR LÉXICO
                String tipoError = determinarTipoLexema(lexemaRestante);
                resultados.add(new Token(lexemaRestante, linea, tipoError, "N/A", false));
                
                continue; 
                
            } else {
                // Caso 4: Error Léxico simple (ej. '$', '234Inválido', 'ERROR_CADENA_INCOMPLETA')
                tipoFinal = tipoFinalAuxiliar;
                reconocido = false;
                
                // Si fue un error simple, lo añadimos y continuamos.
                resultados.add(new Token(lexema, linea, tipoFinal, "N/A", reconocido));
                continue; 
            }

            // Añadir el token final clasificado (solo para Casos 1 y 2)
            resultados.add(new Token(lexema, linea, tipoFinal, estadoReporte, reconocido));
        }

        return resultados.toArray(new Token[0]);
    }

    // -------------------------------------------------------------------
    // --- LÓGICA AFD SIMULADA PARA TIPOS BÁSICOS (Implementada con AFD) ---
    // -------------------------------------------------------------------

    /**
     * Implementa un AFD simulado para reconocer números enteros con signo (^([+-]?)\d+$).
     */
    private static boolean esEnteroConSigno(String lexema) {
        if (lexema == null || lexema.isEmpty()) return false;
        
        // Q0: Inicial, Q1: Después de signo, Q2: Aceptación (después de dígito)
        String estadoActual = "Q0";
        
        for (char c : lexema.toCharArray()) {
            switch (estadoActual) {
                case "Q0":
                    if (c == '+' || c == '-') {
                        estadoActual = "Q1";
                    } else if (Character.isDigit(c)) {
                        estadoActual = "Q2";
                    } else {
                        return false; 
                    }
                    break;
                    
                case "Q1":
                    if (Character.isDigit(c)) {
                        estadoActual = "Q2";
                    } else {
                        return false; 
                    }
                    break;
                    
                case "Q2":
                    if (!Character.isDigit(c)) {
                        return false; 
                    }
                    // Si es dígito, permanece en Q2
                    break;
            }
        }
        
        // Solo es aceptado si terminó en el estado de aceptación (Q2)
        return estadoActual.equals("Q2");
    }

    /**
     * Implementa un AFD simulado para reconocer literales de cadena ("[^"]*").
     */
    private static boolean esLiteralCadena(String lexema) {
        if (lexema == null || lexema.isEmpty()) return false;
        
        // Q0: Inicial, Q1: Dentro de la cadena, Q2: Aceptación (después de la comilla final)
        String estadoActual = "Q0";

        for (char c : lexema.toCharArray()) {
            switch (estadoActual) {
                case "Q0":
                    if (c == '"') {
                        estadoActual = "Q1"; // Inicia la cadena
                    } else {
                        return false; // No empieza con comilla
                    }
                    break;
                case "Q1":
                    if (c == '"') {
                        estadoActual = "Q2"; // Cierra la cadena
                    } else {
                        // Permanece en Q1 (contenido de la cadena)
                    }
                    break;
                case "Q2":
                    return false; // Carácter extra después de la comilla de cierre
            }
        }
        
        // Solo es aceptado si terminó en el estado de aceptación (Q2)
        return estadoActual.equals("Q2");
    }

    /**
     * Implementa un AFD simulado para reconocer identificadores.
     * Patrón: [A-Za-z_][A-Za-z0-9_]*
     */
    private static boolean esIdentificador(String lexema) {
        if (lexema == null || lexema.isEmpty()) return false;

        // Q0: Inicial, Q1: Aceptación (después de leer el primer carácter válido)
        String estadoActual = "Q0";

        for (char c : lexema.toCharArray()) {
            switch (estadoActual) {
                case "Q0":
                    // El primer carácter debe ser letra o guion bajo
                    if (Character.isLetter(c) || c == '_') {
                        estadoActual = "Q1"; // Pasa al estado de aceptación
                    } else {
                        return false; // Inválido
                    }
                    break;
                case "Q1":
                    // Los caracteres siguientes pueden ser letras, dígitos o guion bajo
                    if (Character.isLetterOrDigit(c) || c == '_') {
                        // Permanece en Q1
                    } else {
                        return false; // Carácter inválido para identificador
                    }
                    break;
            }
        }

        // Es aceptado si terminó en el estado Q1 (al menos un carácter válido)
        return estadoActual.equals("Q1");
    }

    /**
     * Clasifica un lexema que no fue reconocido como Palabra Reservada.
     * Añade la distinción entre los distintos tipos de ERROR_LEXICO.
     */
    public static String determinarTipoLexema(String lexema) {
        // 1. Delimitadores y Símbolos Simples y Compuestos (Switch)
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
            case ".": return "OP_PUNTO"; 
        }

        // 2. Literales Numéricos (SOLO ENTEROS) - Implementado con AFD simulado
        if (esEnteroConSigno(lexema)) {
            return "LITERAL_NUMERICA";
        }
        
        // 3. Literales de Cadena - Implementado con AFD simulado
        if (esLiteralCadena(lexema)) {
            return "LITERAL_CADENA";
        }

        // 4. Identificador - Implementado con AFD simulado
        if (esIdentificador(lexema)) {
            return "IDENTIFICADOR";
        }
        
        // --- MANEJO DE ERRORES ESPECÍFICOS ---
        
        // ERROR A: Cadena incompleta
        // Si el lexema comienza con " pero falló la validación completa (es decir, no terminó en Q2)
        if (lexema.startsWith("\"") && !esLiteralCadena(lexema)) {
            return "ERROR_CADENA_INCOMPLETA";
        }
        
        // ERROR B: Símbolo inválido (token de longitud 1 que no fue reconocido en el switch)
        if (lexema.length() == 1) {
            return "ERROR_SIMBOLO_INVALIDO";
        }

        // ERROR C: Token malformado (ej. 234Inválido, secuencia inválida larga)
        return "ERROR_TOKEN_MALFORMADO";
    }
}