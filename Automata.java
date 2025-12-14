
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Autómata Finito Determinista (AFD) para reconocer palabras reservadas. Los
 * demás tipos léxicos (Números, Cadenas, Identificadores) son clasificados
 * usando funciones estáticas que simulan un AFD, reemplazando las RegEx.
 */
public class Automata {

    // los atributos que tendra el automata, estos simulan los componentes de un automata clasico 
    private final Set<String> estados;
    private final Set<Character> alfabeto;
    private final Map<String, Map<Character, String>> transiciones;
    private final String estadoInicial;
    private final Set<String> estadosAceptacion;

    // CREAACION DEl mapa de las palabras reservdas
    private static final Map<String, String> TIPO_POR_PR = Map.ofEntries(
            // ... Tu lista de mapeos de palabras reservadas (no la cambié)
            Map.entry("PILA", "PALABRA_RESERVADA"), Map.entry("PILA_CIRCULAR", "PALABRA_RESERVADA"),
            Map.entry("COLA", "PALABRA_RESERVADA"), Map.entry("BICOLA", "PALABRA_RESERVADA"),
            Map.entry("LISTA_ENLAZADA", "PALABRA_RESERVADA"), Map.entry("LISTA_DOBLE_ENLAZADA", "PALABRA_RESERVADA"),
            Map.entry("LISTA_CIRCULAR", "PALABRA_RESERVADA"), Map.entry("ARBOL_BINARIO", "PALABRA_RESERVADA"),
            Map.entry("TABLAS_HASH", "PALABRA_RESERVADA"), Map.entry("GRAFO", "PALABRA_RESERVADA"),
            Map.entry("CREAR", "PALABRA_RESERVADA"), Map.entry("INSERTAR", "PALABRA_RESERVADA"),
            Map.entry("INSERTAR_FINAL", "PALABRA_RESERVADA"), Map.entry("INSERTAR_INICIO", "PALABRA_RESERVADA"),
            Map.entry("INSERTAR_EN_POSICION", "PALABRA_RESERVADA"), Map.entry("INSERTARIZQUIERDA", "PALABRA_RESERVADA"),
            Map.entry("INSERTARDERECHA", "PALABRA_RESERVADA"), Map.entry("AGREGARNODO", "PALABRA_RESERVADA"),
            Map.entry("APILAR", "PALABRA_RESERVADA"), Map.entry("ENCOLAR", "PALABRA_RESERVADA"),
            Map.entry("PUSH", "PALABRA_RESERVADA"), Map.entry("ENQUEUE", "PALABRA_RESERVADA"),
            Map.entry("ELIMINAR", "PALABRA_RESERVADA"), Map.entry("ELIMINAR_INICIO", "PALABRA_RESERVADA"),
            Map.entry("ELIMINAR_FINAL", "PALABRA_RESERVADA"), Map.entry("ELIMINAR_FRENTE", "PALABRA_RESERVADA"),
            Map.entry("ELIMINAR_POSICION", "PALABRA_RESERVADA"), Map.entry("ELIMINARNODO", "PALABRA_RESERVADA"),
            Map.entry("DESAPILAR", "PALABRA_RESERVADA"), Map.entry("POP", "PALABRA_RESERVADA"),
            Map.entry("DESENCOLAR", "PALABRA_RESERVADA"), Map.entry("DEQUEUE", "PALABRA_RESERVADA"),
            Map.entry("BUSCAR", "PALABRA_RESERVADA"), Map.entry("TOPE", "PALABRA_RESERVADA"),
            Map.entry("FRENTE", "PALABRA_RESERVADA"), Map.entry("VERFILA", "PALABRA_RESERVADA"),
            Map.entry("FRONT", "PALABRA_RESERVADA"), Map.entry("CLAVE", "PALABRA_RESERVADA"),
            Map.entry("RECORRER", "PALABRA_RESERVADA"), Map.entry("RECORRERADELANTE", "PALABRA_RESERVADA"),
            Map.entry("RECORRERATRAS", "PALABRA_RESERVADA"), Map.entry("PREORDEN", "PALABRA_RESERVADA"),
            Map.entry("INORDEN", "PALABRA_RESERVADA"), Map.entry("POSTORDEN", "PALABRA_RESERVADA"),
            Map.entry("RECORRIDOPORNIVELES", "PALABRA_RESERVADA"), Map.entry("ACTUALIZAR", "PALABRA_RESERVADA"),
            Map.entry("REHASH", "PALABRA_RESERVADA"), Map.entry("AGREGARARISTA", "PALABRA_RESERVADA"),
            Map.entry("ELIMINARARISTA", "PALABRA_RESERVADA"), Map.entry("VECINOS", "PALABRA_RESERVADA"),
            Map.entry("BFS", "PALABRA_RESERVADA"), Map.entry("DFS", "PALABRA_RESERVADA"),
            Map.entry("CAMINOCORTO", "PALABRA_RESERVADA"), Map.entry("VACIAT", "PALABRA_RESERVADA"),
            Map.entry("LLENAT", "PALABRA_RESERVADA"), Map.entry("TAMANO", "PALABRA_RESERVADA"),
            Map.entry("ALTURA", "PALABRA_RESERVADA"), Map.entry("HOJAS", "PALABRA_RESERVADA"),
            Map.entry("NODOS", "PALABRA_RESERVADA"), Map.entry("EN", "PALABRA_RESERVADA"),
            Map.entry("CON", "PALABRA_RESERVADA"), Map.entry("VALOR", "PALABRA_RESERVADA"),
            Map.entry("IF", "PC_IF"), Map.entry("ELSE", "PC_ELSE"),
            Map.entry("MOSTRAR", "PALABRA_RESERVADA")
    );

    // constructor del afd
    public Automata(Set<String> estados,
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

    public Token[] aceptar(Token[] tokens) {
        //Crea un arraylist en el que se guardaran los tokens 
        List<Token> resultados = new ArrayList<>();

        // usa los tokens que han salido de tokenizador 
        for (Token tk : tokens) {
            // consigue el lexema conseguido y lo pone en mayusculas porque definimos el mapa de estados 
            //de aceptacion en mayusculas
            String lexema = tk.getLexema().toUpperCase();

            //
            int linea = tk.getLinea();

            //obtiene una clasificacion rapida  para ahorrar tiempo ya que ciertos componentes como 123 o <
            // con rapidos de clasificr con regexs y no teiene sentido perder mucho tiempo con ellos
            String tipoAuxiliar = determinarTipoLexema(lexema);

            // Si es un tipo clasificado por la lógica auxiliar, lo añadimos y pasamos al siguiente token.
            if (!tipoAuxiliar.startsWith("ERROR") && !tipoAuxiliar.equals("IDENTIFICADOR")) {
                resultados.add(new Token(lexema, linea, tk.getColumna(), tipoAuxiliar, "N/A", true));
                continue;
            }

// si no se puede clasificar con la logica auxiliar , ahora usamos el automata 
// inicializamos los atributos de automata
            String estadoActual = estadoInicial;
            String ultimoEstadoAceptado = null;
            int ultimoCaracterAceptado = -1;
            boolean esPR = true;
            // se hace un for  en el cual se recorren las letras del lexema
            for (int j = 0; j < lexema.length(); j++) {
                char simbolo = lexema.charAt(j);

                // delimita las posibles transiciones desde el estado actual 
                Map<Character, String> transicionesEstado = transiciones.get(estadoActual);

                // si se obtiene transiciones que no sean nula y tengan el simbolo
                if (transicionesEstado != null && transicionesEstado.containsKey(simbolo)) {
                    // transiciona a la transicion en l que esta el metodo
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
            String tipoFinal = TIPO_POR_PR.getOrDefault(lexema, tipoFinalAuxiliar);
            String estadoReporte = "N/A";
            boolean reconocido = true;

            if (esPR && estadosAceptacion.contains(estadoActual)) {
                // Caso 1: Lexema consumido completamente y aceptado por el AFD.
                estadoReporte = estadoActual;
                   // sie le automata no acepta el token, entonces es vuelve verifica la clasificacion con el primer tipo que usamos
                   
            } else if (tipoFinalAuxiliar.equals("IDENTIFICADOR")) {

                // Si la clasificación final es PALABRA_RESERVADA (por TIPO_POR_PR),
                // usamos el lexema Upper como estado final para el reporte.
                if (tipoFinal.equals("PALABRA_RESERVADA") || tipoFinal.equals("PC_IF") || tipoFinal.equals("PC_ELSE")) {
                    estadoReporte = lexema;
                } else {
                    estadoReporte = "N/A"; // ID genérico
                }
                    // si el ultimo estado aceptano existe y el ultimo caracter aceptado es inferior a la longitud de la cadena
                    // quiere decir que hubo un caracter invalido en medio de la cadena 
            } else if (ultimoEstadoAceptado != null && ultimoCaracterAceptado < lexema.length() - 1) {
                
                String lexemaValido = lexema.substring(0, ultimoCaracterAceptado + 1);
                String lexemaRestante = lexema.substring(ultimoCaracterAceptado + 1);

                String tipoPR = TIPO_POR_PR.getOrDefault(lexemaValido.toUpperCase(), "IDENTIFICADOR");
                resultados.add(new Token(lexemaValido, linea, tk.getColumna(), tipoPR, ultimoEstadoAceptado, true));

                String tipoError = determinarTipoLexema(lexemaRestante);
                resultados.add(new Token(lexemaRestante, linea, tk.getColumna(), tipoError, "N/A", false));
                
                
                
                
                

                continue;

                // en caso de que no sea valido con los metodos anteriores se descarta 
            } else {
                tipoFinal = tipoFinalAuxiliar;
                reconocido = false;

                resultados.add(new Token(lexema, linea, tk.getColumna(), tipoFinal, "N/A", reconocido));
                continue;
            }

            // si si clasifica lo agregamos al array list 
            resultados.add(new Token(lexema, linea, tk.getColumna(), tipoFinal, estadoReporte, reconocido));
        }

        return resultados.toArray(new Token[0]);
    } // fin aceptar 

    private static boolean esEntero(String lexema) {
        if (lexema == null || lexema.isEmpty()) {
            return false;
        }

        return Pattern.matches("^[+-]?\\d+$", lexema);
    }

    /**
     * Reconoce literales de cadena ("[^"]*"). (Usando Regex Simplificada)
     */
    private static boolean esLiteralCadena(String lexema) {
        if (lexema == null || lexema.isEmpty()) {
            return false;
        }

        return Pattern.matches("^\".*\"$", lexema)
                && lexema.length() >= 2;
    }

    private static boolean esIdentificador(String lexema) {
        if (lexema == null || lexema.isEmpty()) {
            return false;
        }

        return Pattern.matches("^[A-Za-z_][A-Za-z0-9_]*$", lexema);
    }

    public static String determinarTipoLexema(String lexema) {
        // 1. Delimitadores y Símbolos Simples y Compuestos (Switch)
        switch (lexema) {
            case ";":
                return "DELIMITADOR";
            case "(":
                return "PARENTESIS_IZQ";
            case ")":
                return "PARENTESIS_DER";
            case ",":
                return "COMA";
            case "=":
                return "ASIGNACION";
            case "+":
                return "OP_SUMA";
            case "-":
                return "OP_RESTA";
            case "*":
                return "OP_MULTIPLICACION";
            case "/":
                return "OP_DIVISION";
            case "<":
                return "OP_MENOR_QUE";
            case ">":
                return "OP_MAYOR_QUE";
            case "==":
                return "OP_IGUAL";
            case "!=":
                return "OP_DIFERENTE";
            case "<=":
                return "OP_MENOR_IGUAL";
            case ">=":
                return "OP_MAYOR_IGUAL";
            case "{":
                return "LLAVE_IZQ";
            case "}":
                return "LLAVE_DER";
            case "if":
                return "IF";
            case "else":
                return "ELSE";
        }

        // 2. Literales Numéricos (SOLO ENTEROS)
        if (esEntero(lexema)) {
            return "LITERAL_NUMERICA";
        }

        // 3. Literales de Cadena
        if (esLiteralCadena(lexema)) {
            return "LITERAL_CADENA";
        }

        // 4. Identificador
        if (esIdentificador(lexema)) {
            return "IDENTIFICADOR";
        }

        // --- Manejo de Errores ---
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
