import java.util.*;
import java.util.function.BiConsumer;

/**
 * Clase principal que inicializa el AFD para el DSL de Estructuras de Datos,
 * tokeniza el código y realiza la clasificación léxica.
 * Los estados y transiciones del AFD ahora se generan dinámicamente.
 */
public class DSLCore {

    // --- 1. Definición de Palabras Reservadas Finales ---

    // Conjunto de estados de aceptación (Palabras Reservadas completas). 
    // Esta es la ÚNICA lista manual necesaria.
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
             "MOSTRAR"
        );
    }

    // --- 2. Generación Dinámica del AFD ---

    // Genera todos los estados intermedios a partir de las palabras reservadas finales.
    private static Set<String> getEstadosDSL() {
        //Creamos una lista vacia en la cual se almacenarn los posibles estados
        Set<String> todosLosEstados = new HashSet<>();
        // se agrega el estado inicio
        todosLosEstados.add("INICIO");
        
        // Se usan los estados de aceptacion en orden inversa para obtener 
        //sus palabras reservadas
        for (String pr : getEstadosAceptacionDSL()) { //por cada elemento del arreglo que retorna estados de aceptacion
          // ciclamos la lista
            for (int i = 1; i <= pr.length(); i++) {
            // y  creamos un estado con la cadena menos la ultima letra y el destino es el estado actual
                todosLosEstados.add(pr.substring(0, i));
            }
        }
       // ahora ya tenemos todos los estados 
        return todosLosEstados;
    }

    // Genera todas las transiciones (Estado Origen)
    private static Map<String, Map<Character, String>> getTransicionesDSL() {
        Map<String, Map<Character, String>> transiciones = new HashMap<>();
        
        // 1. a los estados les agrega un hash map que sera el encargado de definir las transiciones
        for (String estado : getEstadosDSL()) {
            transiciones.put(estado, new HashMap<>());
        }
        
        // 2. Generar transiciones a partir de las palabras reservadas finales
       // por cada estado de aceptacion
        for (String pr : getEstadosAceptacionDSL()) {
            //String estadoOrigen = "INICIO";
            // recorre desde 0 hasta su tamaño
            for (int i = 0; i < pr.length(); i++) {
                // variable simbolo
                char simbolo = pr.charAt(i);
               // esta letra ira a una posicion mas de su resultado
                String estadoDestino = pr.substring(0, i + 1);
                
               // se valida que no sea el primer estado
                String origen = (i == 0) ? "INICIO" : pr.substring(0, i);

                transiciones.get(origen)
                            .put(simbolo, estadoDestino); // define la transicion al estado de destino 
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
     */
    public static String[] tokenizarLinea(String entrada) {
        // Manejo de comentarios
        int indiceComentario = entrada.indexOf("//");
        if (indiceComentario != -1) {
            entrada = entrada.substring(0, indiceComentario);
        }
        
        // Normalizar espacios
        String tokenizada = entrada.trim().replaceAll("\\s+", " ");

        // Separar operadores y delimitadores
        tokenizada = tokenizada.replaceAll("([\\Q(){}[]|,;=+-*/<>\u0021&|.\\E])", " $1 ");

        // Normalizar espacios y limpiar
        tokenizada = tokenizada.trim().replaceAll("\\s+", " ");

        if (tokenizada.isEmpty()) return new String[0];
        String[] tokens = tokenizada.split(" ");
        
        List<String> listaTokens = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            String t = tokens[i];
            
         
            listaTokens.add(t);
        }
        return listaTokens.toArray(new String[0]);
    }

    /**
     * Itera sobre todas las líneas del código fuente para generar tokens iniciales.
     */
    
   
    public static Token[] tokenizador(String entrada) {
        // Hace un arreglo de tipo string con las lineas, separandolas por cada salto de linea
        String[] lineas = entrada.split("\n");
   
        // Crea un listatokens
        List<Token> listaTokens = new ArrayList<>();
        int numLinea = 1;
        //Recorre todas las linea 
        for (String linea : lineas) {
            String[] toks = tokenizarLinea(linea);
            for (String t : toks) {
                if (!t.trim().isEmpty()) {
                    listaTokens.add(new Token(t, numLinea));
                }
            }
            numLinea++;
        }
        return listaTokens.toArray(new Token[0]);
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

        // 2. Código de prueba con la sintaxis del DSL EXPANDIDA
        String codigo = """
            # Operaciones de Colas y Bicolas
            ENCOLA.R 5 EN COLA;
            INSERTAR_FRENTE 10 EN BICOLAS;
            VERFILA EN COLA;

            # Operaciones de Pilas
            APILAR 20 EN PILA_CIRCULAR;
            TAMAÑO EN PILA;
            if (PILA.VACIAT) { MOSTRAR "Pila vacía"; }
            
            # Operaciones de Listas
            INSERTAR_EN_POSICION 3 1 EN LISTA_ENLAZADAS;
            ELIMINAR_POSICION 5 EN LISTA_DOBLE_ENLAZADA;
            
            # Operaciones de Arboles
            INSERTARIZQUIERDA 7 EN ARBOL_BINARIO;
            
            # Ejemplo de error léxico
            INSERTAZ 7 EN ARBOL_BINARIO; # INSERTA es válido, Z es error
            234Inválido # Mezcla inválida
            SimboloInválido $ & | @
            """;

        // 3. Fase 1: Tokenización (Separación de lexemas y asignación de línea)
        Token[] tokens = tokenizador(codigo);

        System.out.println("=== Fase 1: Lexemas separados ===");
        for (Token tk : tokens) {
            System.out.println(tk.getLexema() + " (Línea: " + tk.getLinea() + ")");
        }

        // 4. Fase 2: Análisis y Clasificación Léxica (Uso del AFD)
        Token[] tablaSimbolos = afd.aceptar(tokens);
        
        System.out.println("\n=== Fase 2: Tabla de Símbolos (Clasificación Léxica) ===");
        System.out.println("Lexema\t\t\tLínea\tTipoToken\t\tEstado Final\tReconocido");
        System.out.println("--------------------\t-----\t------------------\t------------\t----------");
        
        int erroresEncontrados = 0;
        for (Token tk : tablaSimbolos) {
            System.out.printf("%-20s\t%-5d\t%-18s\t%-12s\t%-10s\n",
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
