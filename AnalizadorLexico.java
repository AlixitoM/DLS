import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analizador Léxico para un DSL de Estructuras de Datos.
 */
public class AnalizadorLexico {

    // --- CONSTANTES DE TIPOS DE TOKENS ---
    // Palabras Clave de Control
    public static final String T_CREAR = "PC_CREAR";
    public static final String T_EN = "PREP_EN";
    public static final String T_PUNTO_COMA = "DELIMITADOR";

    // Palabras Clave de Estructuras (PC_ESTRUCTURA)
    public static final String T_PILA = "PILA";
    public static final String T_COLA = "COLA";
    public static final String T_LISTA_SIMPLE = "LISTA_SIMPLE";
    public static final String T_ARBOL = "ARBOL";
    public static final String T_GRAFO = "GRAFO";

    // Palabras Clave de Acciones (PC_ACCION)
    public static final String T_INSERTAR = "ACCION_INSERTAR";
    public static final String T_POP = "ACCION_POP";
    public static final String T_ENQUEUE = "ACCION_ENQUEUE";
    public static final String T_BUSCAR = "ACCION_BUSCAR";
    public static final String T_RECORRER = "ACCION_RECORRER";
    public static final String T_AGREGAR = "ACCION_AGREGAR";

    // Tokens Genéricos
    public static final String T_ID = "IDENTIFICADOR";
    public static final String T_LIT_NUMERO = "LITERAL_NUMERICO";
    public static final String T_ERROR = "ERROR_LEXICO";
    public static final String T_EOF = "FIN_DE_ARCHIVO";

    // Diccionario de Palabras Clave para búsqueda rápida
    private static final Map<String, String> PALABRAS_CLAVE = new HashMap<>();
    static {
        // Inicialización estática del mapa de palabras clave
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
    private final Map<String, Simbolo> tablaDeSimbolos;
    private final List<String> errores;

    public AnalizadorLexico(String codigo) {
        this.codigoFuente = codigo;
        this.posicionActual = 0;
        this.lineaActual = 1;
        this.tablaDeSimbolos = new HashMap<>(); // Usa Map<String, Simbolo>
        this.errores = new ArrayList<>();
    }

    // --- Funciones de Utilidad ---

    /**
     * Añade un símbolo a la tabla si no existe ya.
     * @param simbolo El Simbolo a añadir.
     * @return true si se añadió, false si ya existía.
     */
    public boolean agregarSimbolo(Simbolo simbolo) {
        if (!tablaDeSimbolos.containsKey(simbolo.getNombre())) {
            tablaDeSimbolos.put(simbolo.getNombre(), simbolo);
            return true;
        }
        return false;
    }

    /**
     * Avanza una posición y retorna el caracter actual.
     * @return El caracter consumido o null si es EOF.
     */
    private Character avanzar() {
        if (posicionActual >= codigoFuente.length()) {
            return null;
        }
        char caracter = codigoFuente.charAt(posicionActual++);
        if (caracter == '\n') {
            lineaActual++;
        }
        return caracter;
    }

    /**
     * Retorna el siguiente caracter sin avanzar la posición (lookahead).
     * @return El siguiente caracter o null si es EOF.
     */
    private Character verSiguiente() {
        if (posicionActual < codigoFuente.length()) {
            return codigoFuente.charAt(posicionActual);
        }
        return null;
    }

    /**
     * Reporta y almacena un error léxico.
     */
    private Token reportarError(String descripcion, String lexema) {
        String error = String.format("ERROR LÉXICO (Línea %d): '%s' - %s", lineaActual, lexema, descripcion);
        this.errores.add(error);
        System.out.println(error);
        return new Token(T_ERROR, lexema, lineaActual);
    }

    /**
     * Ignora espacios, tabulaciones y saltos de línea.
     */
    private void ignorarEspaciosYComentarios() {
        while (posicionActual < codigoFuente.length()) {
            char c = codigoFuente.charAt(posicionActual);
            if (Character.isWhitespace(c)) {
                if (c == '\n') {
                    lineaActual++;
                }
                posicionActual++;
            } else if (c == '#') {
                // Lógica para ignorar comentario de una sola línea (ej: # ...)
                while (posicionActual < codigoFuente.length() && codigoFuente.charAt(posicionActual) != '\n') {
                    posicionActual++;
                }
                // El salto de línea será manejado por la siguiente iteración o avance normal
            } else {
                break;
            }
        }
    }

    // --- Funciones de Reconocimiento ---

    /**
     * Reconoce un lexema que empieza con letra y lo clasifica como ID o PC.
     */
    private Token reconocerIdentificadorOPalabraClave() {
        int inicio = posicionActual - 1; // Ya avanzamos el primer caracter
        
        while (verSiguiente() != null && 
               (Character.isLetterOrDigit(verSiguiente()) || verSiguiente() == '_')) {
            avanzar(); // Consume el caracter alfanumérico o '_'
        }
        
        String lexema = codigoFuente.substring(inicio, posicionActual);
        String lexemaUpper = lexema.toUpperCase();
        
        // Clasificación: ¿Es una Palabra Clave o un Identificador?
        String tipo;
        if (PALABRAS_CLAVE.containsKey(lexemaUpper)) {
            // Es una palabra clave fija del lenguaje
            tipo = PALABRAS_CLAVE.get(lexemaUpper);
        } else {
            // Es un identificador
            tipo = T_ID;
        }
        
        return new Token(tipo, lexema, lineaActual);
    }

    /**
     * Reconoce un número entero o decimal.
     */
    private Token reconocerLiteralNumerico() {
        int inicio = posicionActual - 1; // Ya avanzamos el primer dígito
        
        while (verSiguiente() != null && Character.isDigit(verSiguiente())) {
            avanzar();
        }
        
        // Opcional: Soporte para números decimales
        if (verSiguiente() != null && verSiguiente() == '.') {
            avanzar(); // Consumir el punto
            while (verSiguiente() != null && Character.isDigit(verSiguiente())) {
                avanzar();
            }
        }
                
        String lexema = codigoFuente.substring(inicio, posicionActual);
        
        return new Token(T_LIT_NUMERO, lexema, lineaActual);
    }

    // --- Función Central ---

    /**
     * Función principal para obtener el siguiente token.
     * @return El Token reconocido.
     */
    public Token obtenerSiguienteToken() {
        ignorarEspaciosYComentarios();

        if (posicionActual >= codigoFuente.length()) {
            return new Token(T_EOF, "", lineaActual);
        }

        Character c = avanzar();
        if (c == null) {
            return new Token(T_EOF, "", lineaActual);
        }
        
        // RECONOCIMIENTO
        if (Character.isLetter(c) || c == '_') {
            return reconocerIdentificadorOPalabraClave();
        } else if (Character.isDigit(c)) {
            return reconocerLiteralNumerico();
        } else if (c == ';') {
            return new Token(T_PUNTO_COMA, ";", lineaActual);
        }

        // GESTIÓN DE ERRORES LÉXICOS
        else {
            // Símbolo no permitido en el alfabeto del DSL
            return reportarError("Símbolo no permitido en el lenguaje.", String.valueOf(c));
        }
    }

    public Map<String, Simbolo> getTablaDeSimbolos() {
        return tablaDeSimbolos;
    }

    public List<String> getErrores() {
        return errores;
    }

    /**
     * Función de prueba que simula el análisis y la interacción con la TS.
     */
    public static void analizarCodigoFuente(String codigo) {
        AnalizadorLexico lexer = new AnalizadorLexico(codigo);
        List<Token> tokens = new ArrayList<>();
        
        System.out.println("--- INICIO DEL ANÁLISIS LÉXICO ---");
        
        while (true) {
            Token token = lexer.obtenerSiguienteToken();
            tokens.add(token);
            System.out.println(token);
            
            if (token.getTipo().equals(T_EOF) || token.getTipo().equals(T_ERROR)) {
                // Solo detenemos el bucle al encontrar EOF,
                // si es un error, el lexer continúa buscando el siguiente token.
                if (token.getTipo().equals(T_EOF)) break;
            }
        }
            
        // SIMULACIÓN de la GESTIÓN DE LA TABLA DE SÍMBOLOS
        System.out.println("\n--- SIMULACIÓN DE LA TABLA DE SÍMBOLOS ---");
        
        // Identificamos el patrón de declaración: CREAR [TIPO] [ID] ;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getTipo().equals(T_CREAR) && i + 2 < tokens.size()) {
                Token tipoStruct = tokens.get(i + 1); // Esperamos el tipo (ej: PILA)
                Token nombreId = tokens.get(i + 2);   // Esperamos el ID (ej: MiPila)
                
                // Una verificación simple de que el tipo sea una estructura y el siguiente un ID.
                if (PALABRAS_CLAVE.containsValue(tipoStruct.getTipo()) && nombreId.getTipo().equals(T_ID)) {
                    // La declaración es válida léxicamente, la añadimos a la TS
                    Simbolo simbolo = new Simbolo(nombreId.getLexema(), tipoStruct.getTipo(), nombreId.getLinea());
                    if (lexer.agregarSimbolo(simbolo)) { // Usamos el método de agregar
                        System.out.printf("✅ TS: '%s' registrado como %s.%n", nombreId.getLexema(), tipoStruct.getTipo());
                    } else {
                        System.out.printf("❌ TS: Error, '%s' ya existe.%n", nombreId.getLexema());
                    }
                }
            }
        }
        
        System.out.println("\n--- CONTENIDO FINAL DE LA TABLA DE SÍMBOLOS ---");
        System.out.println(lexer.getTablaDeSimbolos());
        System.out.println("\n--- ERRORES ENCONTRADOS ---");
        lexer.getErrores().forEach(System.out::println);
    }
    
    // CÓDIGO DE PRUEBA DSL
    public static void main(String[] args) {
        String codigoDsl = 
            "CREAR COLA ColaDePrueba; \n" +
            "INSERTAR 5 EN ColaDePrueba; # Esto es un comentario \n" +
            "CREAR PILA MiPila; \n" +
            "CREAR PILA ColaDePrueba; # Intento de re-declaracion \n" +
            "SimboloNoPermitido $"; // Simula un error léxico
            
        analizarCodigoFuente(codigoDsl);
    }
}