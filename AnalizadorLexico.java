import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analizador Léxico (Puro) para un DSL de Estructuras de Datos.
 * Se enfoca UNICAMENTE en la generación de Tokens.
 */
public class AnalizadorLexico {

    // --- CONSTANTES DE TIPOS DE TOKENS ---
    public static final String T_CREAR = "PC_CREAR";
    public static final String T_EN = "PREP_EN";
    public static final String T_PUNTO_COMA = "DELIMITADOR";
    public static final String T_PILA = "PILA";
    public static final String T_COLA = "COLA";
    public static final String T_LISTA_SIMPLE = "LISTA_SIMPLE";
    public static final String T_ARBOL = "ARBOL";
    public static final String T_GRAFO = "GRAFO";
    public static final String T_INSERTAR = "ACCION_INSERTAR";
    public static final String T_POP = "ACCION_POP";
    public static final String T_ENQUEUE = "ACCION_ENQUEUE";
    public static final String T_BUSCAR = "ACCION_BUSCAR";
    public static final String T_RECORRER = "ACCION_RECORRER";
    public static final String T_AGREGAR = "ACCION_AGREGAR";
    public static final String T_ID = "IDENTIFICADOR";
    public static final String T_LIT_NUMERO = "LITERAL_NUMERICO";
    public static final String T_ERROR = "ERROR_LEXICO";
    public static final String T_EOF = "FIN_DE_ARCHIVO";

    // Diccionario de Palabras Clave
    private static final Map<String, String> PALABRAS_CLAVE = new HashMap<>();
    static {
        // ... (Inicialización de PALABRAS_CLAVE - MISMA QUE ANTES) ...
        PALABRAS_CLAVE.put("CREAR", T_CREAR);
        PALABRAS_CLAVE.put("EN", T_EN);
        PALABRAS_CLAVE.put("PILA", T_PILA);
        PALABRAS_CLAVE.put("COLA", T_COLA);
        PALABRAS_CLAVE.put("ARBOL", T_ARBOL);
        PALABRAS_CLAVE.put("LISTA_SIMPLE", T_LISTA_SIMPLE);
        PALABRAS_CLAVE.put("GRAFO", T_GRAFO);
        PALABRAS_CLAVE.put("INSERTAR", T_INSERTAR);
        PALABRAS_CLAVE.put("POP", T_POP);
        PALABRAS_CLAVE.put("ENQUEUE", T_ENQUEUE);
        PALABRAS_CLAVE.put("BUSCAR", T_BUSCAR);
        PALABRAS_CLAVE.put("RECORRER", T_RECORRER);
        PALABRAS_CLAVE.put("AGREGAR", T_AGREGAR);
    }

    private final String codigoFuente;
    private int posicionActual;
    private int lineaActual;
    private final List<String> errores; // Solo almacena errores, no símbolos

    public AnalizadorLexico(String codigo) {
        this.codigoFuente = codigo;
        this.posicionActual = 0;
        this.lineaActual = 1;
        this.errores = new ArrayList<>();
    }

    // --- Funciones de Utilidad (avanzar, verSiguiente, ignorarEspacios) ---

    private Character avanzar() {
        if (posicionActual >= codigoFuente.length()) return null;
        char caracter = codigoFuente.charAt(posicionActual++);
        if (caracter == '\n') lineaActual++;
        return caracter;
    }

    private Character verSiguiente() {
        if (posicionActual < codigoFuente.length()) {
            return codigoFuente.charAt(posicionActual);
        }
        return null;
    }

    private Token reportarError(String descripcion, String lexema) {
        String error = String.format("ERROR LÉXICO (Línea %d): '%s' - %s", lineaActual, lexema, descripcion);
        this.errores.add(error);
        System.out.println(error);
        return new Token(T_ERROR, lexema, lineaActual);
    }

    private void ignorarEspaciosYComentarios() {
        while (posicionActual < codigoFuente.length()) {
            char c = codigoFuente.charAt(posicionActual);
            if (Character.isWhitespace(c)) {
                if (c == '\n') lineaActual++;
                posicionActual++;
            } else if (c == '#') {
                while (posicionActual < codigoFuente.length() && codigoFuente.charAt(posicionActual) != '\n') {
                    posicionActual++;
                }
            } else {
                break;
            }
        }
    }

    // --- Funciones de Reconocimiento (Mismas que antes) ---

    private Token reconocerIdentificadorOPalabraClave() {
        int inicio = posicionActual - 1; 
        
        while (verSiguiente() != null && 
               (Character.isLetterOrDigit(verSiguiente()) || verSiguiente() == '_')) {
            avanzar();
        }
        
        String lexema = codigoFuente.substring(inicio, posicionActual);
        String tipo;
        if (PALABRAS_CLAVE.containsKey(lexema.toUpperCase())) {
            tipo = PALABRAS_CLAVE.get(lexema.toUpperCase());
        } else {
            tipo = T_ID;
        }
        
        return new Token(tipo, lexema, lineaActual);
    }

    private Token reconocerLiteralNumerico() {
        int inicio = posicionActual - 1; 
        
        while (verSiguiente() != null && Character.isDigit(verSiguiente())) {
            avanzar();
        }
        
        if (verSiguiente() != null && verSiguiente() == '.') {
            avanzar();
            while (verSiguiente() != null && Character.isDigit(verSiguiente())) {
                avanzar();
            }
        }
                
        String lexema = codigoFuente.substring(inicio, posicionActual);
        
        return new Token(T_LIT_NUMERO, lexema, lineaActual);
    }


    // --- Función Central de Obtención de Token (getters removidos) ---

    public Token obtenerSiguienteToken() {
        ignorarEspaciosYComentarios();

        if (posicionActual >= codigoFuente.length()) {
            return new Token(T_EOF, "", lineaActual);
        }

        Character c = avanzar();
        if (c == null) return new Token(T_EOF, "", lineaActual);
        
        if (Character.isLetter(c) || c == '_') {
            return reconocerIdentificadorOPalabraClave();
        } else if (Character.isDigit(c)) {
            return reconocerLiteralNumerico();
        } else if (c == ';') {
            return new Token(T_PUNTO_COMA, ";", lineaActual);
        }
        else {
            return reportarError("Símbolo no permitido en el lenguaje.", String.valueOf(c));
        }
    }

    public List<String> getErrores() {
        return errores;
    }

    /**
     * Función de prueba que SOLO realiza el análisis léxico.
     */
    public static void analizarCodigoFuente(String codigo) {
        AnalizadorLexico lexer = new AnalizadorLexico(codigo);
        List<Token> tokens = new ArrayList<>();
        
        System.out.println("--- INICIO DEL ANÁLISIS LÉXICO (Puro) ---");
        
        while (true) {
            Token token = lexer.obtenerSiguienteToken();
            tokens.add(token);
            System.out.println(token);
            
            if (token.getTipo().equals(T_EOF)) {
                break;
            }
        }
            
        System.out.println("\n--- LISTA COMPLETA DE TOKENS ---");
        tokens.forEach(t -> System.out.print(t + "\n"));
        
        System.out.println("\n--- ERRORES LÉXICOS ENCONTRADOS ---");
        lexer.getErrores().forEach(System.out::println);
    }
    
    // CÓDIGO DE PRUEBA DSL
    public static void main(String[] args) {
        String codigoDsl = 
            "CREAR COLA ColaDePrueba; \n" +
            "INSERTAR 5.0 EN ColaDePrueba; # Esto es un comentario \n" +
            "CREAR PILA MiPila; \n" +
            "SímboloInválido @ 123";
            
        analizarCodigoFuente(codigoDsl);
    }
}