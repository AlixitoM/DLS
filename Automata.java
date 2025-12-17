
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Crea la clase automata en la cual  validaremos las palabras reservadas 
public class Automata {

    // atributos de un automata los cuales serviran para simular 
    private final Set<String> estados;
    private final Set<Character> alfabeto;
    private final Map<String, Map<Character, String>> transiciones;
    private final String estadoInicial;
    private final Set<String> estadosAceptacion;

    // este mapa asigna una clasificacion a la palabra reservada
    private static final Map<String, String> TIPO_POR_PR = Map.ofEntries(
            Map.entry("PILA", "PALABRA_RESERVADA"),
            Map.entry("PILA_CIRCULAR", "PALABRA_RESERVADA"),
            Map.entry("COLA", "PALABRA_RESERVADA"),
            Map.entry("BICOLA", "PALABRA_RESERVADA"),
            Map.entry("LISTA_ENLAZADA", "PALABRA_RESERVADA"),
            Map.entry("LISTA_DOBLE_ENLAZADA", "PALABRA_RESERVADA"),
            Map.entry("LISTA_CIRCULAR", "PALABRA_RESERVADA"),
            Map.entry("ARBOL_BINARIO", "PALABRA_RESERVADA"),
            Map.entry("TABLAS_HASH", "PALABRA_RESERVADA"),
            Map.entry("GRAFO", "PALABRA_RESERVADA"),
            // Acciones/Operaciones 
            Map.entry("CREAR", "PALABRA_RESERVADA"),
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
            Map.entry("TAMANO", "PALABRA_RESERVADA"),
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

// constructor de la clase automata 
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

// e    
    public Token[] aceptar(Token[] tokensTokensIniciales) {
        List<Token> resultados = new ArrayList<>();
 // por cada roken 
        for (Token tk : tokensTokensIniciales) {
            // declaramos variables para el procesamiento del token 
            String lexema = tk.getLexema();
            String lexemaUpper = lexema.toUpperCase();
            int linea = tk.getLinea();

            // si es un token simple usamos el metodo determinar tipo lexema para poder evitar eimpos de compilacion largos 
            String tipoAuxiliar = determinarTipoLexema(lexema);

            // Si es un tipo clasificado por el auxiiar lo  agregamos al array list ya que no nescesitamos usarlo de nuevo
            if (!tipoAuxiliar.startsWith("ERROR") && !tipoAuxiliar.equals("IDENTIFICADOR")) {
                resultados.add(new Token(lexema, linea, tk.getColumna(), tipoAuxiliar, "N/A", true));
                // y continua a otra iteracion
                continue;
            }


            // se inicializa el estado inicial como el estado actual
            String estadoActual = estadoInicial;
            String ultimoEstadoAceptado = null;
            int ultimoCaracterAceptado = -1;

            // inicializamos por defecto la variable esPR en verdad
            boolean esPR = true;
            for (int j = 0; j < lexemaUpper.length(); j++) {
                char simbolo = lexemaUpper.charAt(j);

                
                // obetenemos las trancisiones desde el estado actual del automata  
                Map<Character, String> transicionesEstado = transiciones.get(estadoActual);

                
                // si obtiene un estado de aceptacion
                if (transicionesEstado != null && transicionesEstado.containsKey(simbolo)) {
                    // Transición exitosa
                    estadoActual = transicionesEstado.get(simbolo);

                    // Si el nuevo estado es de aceptación, registramos la posición y el estado
                    if (estadosAceptacion.contains(estadoActual)) {
                        ultimoEstadoAceptado = estadoActual;
                        ultimoCaracterAceptado = j;
                    }
                } 
                // sino no lo hacemos y sale del automata 
                else {
                    esPR = false;
                    break;
                }
            }

            //  vuelve a darle una repasada con el tipo auxiliar final
            String tipoFinalAuxiliar = determinarTipoLexema(lexema);
            // y define el tipo final tomando el recorrido del automata y le pone el tipo auxiliar i
             String tipoFinal = TIPO_POR_PR.getOrDefault(lexemaUpper, tipoFinalAuxiliar);

            String estadoReporte = "N/A";
            boolean reconocido = true;

            if (esPR && estadosAceptacion.contains(estadoActual)) {
                estadoReporte = estadoActual;

            } else if (tipoFinalAuxiliar.equals("IDENTIFICADOR")) {

                if (tipoFinal.equals("PALABRA_RESERVADA") || tipoFinal.equals("PC_IF") || tipoFinal.equals("PC_ELSE")) {
                    estadoReporte = lexemaUpper;
                } else {
                    estadoReporte = "N/A"; // ID genérico
                }

            } else {
                tipoFinal = tipoFinalAuxiliar;
                reconocido = false;

                resultados.add(new Token(lexema, linea, tk.getColumna(), tipoFinal, "N/A", reconocido));
                continue;
            }

            resultados.add(new Token(lexema, linea, tk.getColumna(), tipoFinal, estadoReporte, reconocido));
        }

        return resultados.toArray(new Token[0]);
    }

    private static boolean esEntero(String lexema) {
        if (lexema == null) {
            return false;
        }

        return lexema.matches("\\d+");
    }

    private static boolean esLiteralCadena(String lexema) {
        if (lexema == null) {
            return false;
        }

        return lexema.matches("\"[^\"]*\"");
    }

    private static boolean esIdentificador(String lexema) {
        if (lexema == null) {
            return false;
        }

        return lexema.matches("[A-Za-z_][A-Za-z0-9_]*");
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
            case "[":
                return "CORCHETE_IZQ";
            case "]":
                return "CORCHETE_DER";
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
                return "PC_IF";
            case "else":
                return "PC_ELSE";

        }

        if (esEntero(lexema)) {
            return "LITERAL_NUMERICA";
        }

        if (esLiteralCadena(lexema)) {
            return "LITERAL_CADENA";
        }

        // 4. Identificador - Implementado con AFD simulado
        if (esIdentificador(lexema)) {
            return "IDENTIFICADOR";
        }

        if (lexema.startsWith("\"") && !esLiteralCadena(lexema)) {
            return "ERROR_CADENA_INCOMPLETA";
        }

        if (lexema.length() == 1) {
            return "ERROR_SIMBOLO_INVALIDO";
        }

        return "ERROR_TOKEN_MALFORMADO";
    }
    
    public static Map<String, String> getPalabrasReservadas() {
    return TIPO_POR_PR;
}
    
    

}
