import java.util.*;


/**
 * Clase principal que inicializa el AFD para el DSL de Estructuras de Datos,
 * tokeniza el código y realiza la clasificación léxica.
 * Los estados y transiciones del AFD ahora se generan dinámicamente.
 */
public class DSLCore {

    // --- 1. Definición de Palabras Reservadas Finales ---

    // Conjunto de estados de aceptación (Palabras Reservadas completas). 
    private static Set<String> getEstadosAceptacionDSL() {
        return Set.of(
            // Estructuras
            "PILA", "PILA_CIRCULAR", "COLA", "BICOLAS", "LISTA_ENLAZADAS", "LISTA_DOBLE_ENLAZADA", "LISTA_CIRCULAR", "ARBOL_BINARIO", "TABLAS_HASH", "GRAFOS",
            // Acciones/Operaciones
            "INSERTAR", "INSERTAR_FINAL", "INSERTAR_INICIO", "INSERTAR_EN_POSICION", "INSERTARIZQUIERDA", "INSERTARDERECHA", "AGREGARNODO", "APILAR", "ENCOLAR", "PUSH", "ENQUEUE",
            "ELIMINAR", "ELIMINAR_INICIO", "ELIMINAR_FINAL", "ELIMINAR_FRENTE", "ELIMINAR_POSICION", "ELIMINARNODO", "DESAPILAR", "POP", "DESENCOLAR", "DEQUEUE",
            "BUSCAR", "TOPE", "FRENTE", "PEEK", "VERFILA", "FRONT", "CLAVE",
            "RECORRER", "RECORRERADELANTE", "RECORRERATRAS", "PREORDEN", "INORDEN", "POSTORDEN", "RECORRIDOPORNIVELES",
            "ACTUALIZAR", "REHASH", "AGREGARARISTA", "ELIMINARARISTA", "VECINOS", "BFS", "DFS", "CAMINOCORTO",
            // Propiedades/Verificación
            "VACIAT", "LLENAT", "TAMAÑO", "ALTURA", "HOJAS", "NODOS",
            // Auxiliares
            "EN", "CON", "VALOR",
            // Palabras Clave de Control
             "MOSTRAR", "IF", "ELSE"
        );
    }

    // --- 2. Generación Dinámica del AFD ---

    // Genera todos los estados intermedios a partir de las palabras reservadas finales.
    private static Set<String> getEstadosDSL() {
        Set<String> todosLosEstados = new HashSet<>();
        todosLosEstados.add("INICIO");
        
        for (String pr : getEstadosAceptacionDSL()) {
            for (int i = 1; i <= pr.length(); i++) {
                todosLosEstados.add(pr.substring(0, i));
            }
        }
        return todosLosEstados;
    }

    // Genera todas las transiciones (Estado Origen)
    private static Map<String, Map<Character, String>> getTransicionesDSL() {
        Map<String, Map<Character, String>> transiciones = new HashMap<>();
        
        for (String estado : getEstadosDSL()) {
            transiciones.put(estado, new HashMap<>());
        }
        
        for (String pr : getEstadosAceptacionDSL()) {
            for (int i = 0; i < pr.length(); i++) {
                char simbolo = pr.charAt(i);
                String estadoDestino = pr.substring(0, i + 1);
                String origen = (i == 0) ? "INICIO" : pr.substring(0, i);

                transiciones.get(origen).put(simbolo, estadoDestino); 
            }
        }
        return transiciones;
    }

    // Conjunto de caracteres en mayúsculas, dígitos y '_' (necesario para el AFD)
    private static Set<Character> getAlfabetoDSL() {
        Set<Character> alfabeto = new HashSet<>();
        for (char c = 'A'; c <= 'Z'; c++) alfabeto.add(c);
        for (char c = '0'; c <= '9'; c++) alfabeto.add(c);
        alfabeto.add('_');
        return alfabeto;
    }
    
    // --- 3. Funciones de Tokenización (Pre-procesamiento) ---
    
    /**
     * Pre-tokeniza una línea separando lexemas por espacios y delimitadores.
     * CORREGIDO: Maneja correctamente operadores compuestos (==, !=, etc.)
     */
    public static String[] tokenizarLinea(String entrada) {
        // Manejo de comentarios
        int indiceComentario = entrada.indexOf("//");
        if (indiceComentario != -1) {
            entrada = entrada.substring(0, indiceComentario);
        }
        
        // Normalizar espacios iniciales
        String tokenizada = entrada.trim().replaceAll("\\s+", " ");

        // Separar operadores. 
        // IMPORTANTE: Ponemos primero los operadores compuestos (ej. ==) para que tengan prioridad
        // y no se dividan en dos caracteres separados.
        // El regex dice: "Busca uno de estos grupos O busca uno de estos caracteres individuales"
        tokenizada = tokenizada.replaceAll("(==|!=|<=|>=|&&|\\|\\||[\\Q(){}[]|,;=+-*/<>\u0021&|.\\E])", " $1 ");

        // Limpiar espacios dobles generados por el reemplazo anterior
        tokenizada = tokenizada.trim().replaceAll("\\s+", " ");

        if (tokenizada.isEmpty()) return new String[0];
        
        // Retornar arreglo limpio
        return tokenizada.split(" ");
    }

    /**
     * Itera sobre todas las líneas del código fuente para generar tokens iniciales.
     */
    // Sustituye en DSLCore.java

public static Token[] tokenizador(String entrada) {
    List<Token> listaTokens = new ArrayList<>();
    
    String regex = 
        "(//.*)|" +                       
        "(\"[^\"]*\")|" +                 
        "(==|!=|<=|>=|&&|\\|\\|)|" +      
        "([a-zA-Z_][a-zA-Z0-9_]*)|" +     
        "(\\d+)|" +                       
        "([\\Q(){}[]|,;=+-*/<>\u0021&|.\\E])|" + 
        "(\\S+)";                         

    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
    
    String[] lineas = entrada.split("\n");
    int numLinea = 1;

    for (String lineaOriginal : lineas) {
        java.util.regex.Matcher matcher = pattern.matcher(lineaOriginal);
        
        while (matcher.find()) {
            String token = matcher.group();
            
            // Si es comentario (Grupo 1), ignoramos el resto de la línea o el match
            if (matcher.group(1) != null) continue;
            
            // Si es espacio vacío (a veces matcher captura vacíos si el regex no es perfecto), ignorar
            if (token.trim().isEmpty()) continue;

            // Calcular columna real usando start() del matcher
            // Sumamos 1 porque las columnas suelen ser base-1
            int columna = matcher.start() + 1; 

            listaTokens.add(new Token(token, numLinea, columna));
        }
        numLinea++;
    }
    return listaTokens.toArray(new Token[0]);
}
    public static AFD obtenerInstanciaAFD() {
    return new AFD(
        getEstadosDSL(),
        getAlfabetoDSL(),
        getTransicionesDSL(),
        "INICIO",
        getEstadosAceptacionDSL()
    );
}
    
    
    // --- 4. FUNCIÓN MAIN DE PRUEBA ---
    
    public static void main(String[] args) {
        
        // 1. Inicialización del AFD con las reglas del DSL
        AFD afd = new AFD(
            getEstadosDSL(),
            getAlfabetoDSL(),
            getTransicionesDSL(),
            "INICIO",
            getEstadosAceptacionDSL()
        );

        // 2. Código de prueba con casos normales y casos de borde (Identificadores largos, errores, etc.)
        String codigo = """
            // Operaciones de Colas y Bicolas
            ENCOLAR 5 EN COLA;
            INSERTAR_FRENTE 10 EN BICOLAS;
            VERFILA EN COLA;

            // Identificadores que parecen Palabras Reservadas (Prueba de robustez)
            INSERTARDATOS = 50;  
            ENCOLARDATOS;

            // Operaciones de Pilas y Control de flujo con operadores compuestos
            APILAR 20 EN PILA_CIRCULAR;
            if (PILA.TAMAÑO == 0) { MOSTRAR "Pila vacía"; }
            if (A != B) { MOSTRAR "Diferente"; }
            
            // Ejemplo de error léxico
            INSERTAZ 7 EN ARBOL_BINARIO; # INSERTA es válido, Z es error
            234Inválido 
            $ SimboloInválido
            """;

        // 3. Fase 1: Tokenización
        Token[] tokens = tokenizador(codigo);

        System.out.println("=== Fase 1: Lexemas separados ===");
        for (Token tk : tokens) {
            System.out.println("Lexema: [" + tk.getLexema() + "] Línea: " + tk.getLinea());
        }

        // 4. Fase 2: Análisis y Clasificación Léxica
        Token[] tablaSimbolos = afd.aceptar(tokens);
        
        System.out.println("\n=== Fase 2: Tabla de Símbolos (Clasificación Léxica) ===");
        System.out.printf("%-20s %-5s %-25s %-15s %-10s\n", "Lexema", "Lin", "TipoToken", "EstadoFin", "Reconocido");
        System.out.println("--------------------------------------------------------------------------------------");
        
        int erroresEncontrados = 0;
        for (Token tk : tablaSimbolos) {
            System.out.printf("%-20s %-5d %-25s %-15s %-10s\n",
                tk.getLexema(),
                tk.getLinea(),
                tk.getTipoToken(),
                tk.getEstadoFinal(),
                tk.existeSimbolo() ? "Sí" : "No"
            );
            if (tk.getTipoToken().startsWith("ERROR")) {
                erroresEncontrados++;
            }
        }
        
        System.out.println("\nResumen: " + tablaSimbolos.length + " tokens procesados. " +
                           erroresEncontrados + " errores léxicos.");
    }
}