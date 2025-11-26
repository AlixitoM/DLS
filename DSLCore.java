import java.util.*;
import java.util.function.BiConsumer;

/**
 * Clase principal que inicializa el AFD para el DSL de Estructuras de Datos,
 * tokeniza el código y realiza la clasificación léxica.
 */
public class DSLCore {
    
    // --- Definición de Conjuntos para el AFD (Palabras Reservadas del DSL) ---
    
    // Conjunto de TODOS los estados posibles para construir las palabras reservadas del DSL
    private static Set<String> getEstadosDSL() {
        return Set.of(
            "INICIO",
            // P (PILA, PUSH, POP, PREORDEN)
            "P", "PI", "PIL", "PILA", "PILA_C", "PILA_CI", "PILA_CIR", "PILA_CIRC", "PILA_CIRCU", "PILA_CIRCUL", "PILA_CIRCULA", "PILA_CIRCULAR",
            "PO", "POP", "PU", "PUS", "PUSH",
            "PRE", "PREO", "PREOR", "PREORD", "PREORDE", "PREORDEN", "POST", "POSTO", "POSTOR", "POSTORD", "POSTORDE", "POSTORDEN",
            // C (COLA, CON, CAMINOCORTO)
            "C", "CO", "COL", "COLA", "CON", "CA", "CAM", "CAMI", "CAMIN", "CAMINO", "CAMINOC", "CAMINOCO", "CAMINOCOR", "CAMINOCORT", "CAMINOCORTO",
            "CL", "CLA", "CLAV", "CLAVE", // CLAVE estados
            // A (ARBOL, AGREGAR_VERTICE, AGREGARARISTA, APILAR, ACTUALIZAR, ALTURA)
            "A", "AR", "ARB", "ARBO", "ARBOL", "ARBOL_B", "ARBOL_BI", "ARBOL_BIN", "ARBOL_BINA", "ARBOL_BINAR", "ARBOL_BINARI", "ARBOL_BINARIO",
            "AG", "AGR", "AGRE", "AGREG", "AGREGA", "AGREGAR", "AGREGARNODO", "AGREGARARISTA",
            "AP", "API", "APIL", "APILA", "APILAR", "ACT", "ACTU", "ACTUA", "ACTUAL", "ACTUALI", "ACTUALIZ", "ACTUALIZA", "ACTUALIZAR",
            "AL", "ALT", "ALTU", "ALTUR", "ALTURA",
            // B (BICOLAS, BUSCAR, BFS)
            "B", "BI", "BIC", "BICO", "BICOL", "BICOLA", "BICOLAS",
            "BU", "BUS", "BUSC", "BUSCA", "BUSCAR",
            "BF", "BFS",
            // D (DFS, DEQUEUE, DESAPILAR, DESENCOLAR)
            "D", "DF", "DFS", "DE", "DES", "DESA", "DESAP", "DESAPI", "DESAPIL", "DESAPILA", "DESAPILAR",
            "DESE", "DESEN", "DESENC", "DESENCO", "DESENCOL", "DESENCOLA", "DESENCOLAR",
            "DEQ", "DEQU", "DEQUE", "DEQUEU", "DEQUEUE",
            // E (EN, ENCOLAR, ELIMINAR, ELIMINAR_FRENTE/INICIO/FINAL, ELIMINAR_POSICION, ELIMINARARISTA, ELIMINARNODO)
            "E", "EN", "ENC", "ENCO", "ENCOL", "ENCOLA", "ENCOLAR", "ENQ", "ENQU", "ENQUE", "ENQUEU", "ENQUEUE",
            "EL", "ELI", "ELIM", "ELIMI", "ELIMIN", "ELIMINA", "ELIMINAR", "ELIMINAR_",
            "ELIMINAR_F", "ELIMINAR_FR", "ELIMINAR_FRE", "ELIMINAR_FREN", "ELIMINAR_FRENT", "ELIMINAR_FRENTE",
            "ELIMINAR_I", "ELIMINAR_IN", "ELIMINAR_INI", "ELIMINAR_INIC", "ELIMINAR_INICI", "ELIMINAR_INICIO",
            "ELIMINAR_PO", "ELIMINAR_POS", "ELIMINAR_POSI", "ELIMINAR_POSIC", "ELIMINAR_POSICI", "ELIMINAR_POSICIO", "ELIMINAR_POSICION",
            "ELIMINARARISTA", "ELIMINARNODO",
            // F (FRONT, FRENTE)
            "F", "FR", "FRE", "FREN", "FRENT", "FRENTE", "FRO", "FRON", "FRONT",
            // G (GRAFOS)
            "G", "GR", "GRA", "GRAF", "GRAFO", "GRAFOS",
            // H (HOJAS)
            "H", "HO", "HOJ", "HOJA", "HOJAS",
            // I (INSERTAR, INORDEN, INSERTARIZQUIERDA/DERECHA)
            "I", "IN", "INS", "INSE", "INSER", "INSERT", "INSERTA", "INSERTAR", "INSERTAR_",
            "INSERTAR_FRENTE", "INSERTAR_FINAL", "INSERTAR_INICIO", "INSERTAR_EN_POSICION",
            "INOR", "INORD", "INORDE", "INORDEN",
            "INSERTARIZQUIERDA", "INSERTARDERECHA",
            // L (LISTAS, LLENA)
            "L", "LI", "LIS", "LIST", "LISTA", "LISTA_", "LISTA_E", "LISTA_EN", "LISTA_ENL", "LISTA_ENLA", "LISTA_ENLAZ", "LISTA_ENLAZA", "LISTA_ENLAZAD", "LISTA_ENLADA", "LISTA_ENLAZADAS",
            "LISTA_D", "LISTA_DO", "LISTA_DOB", "LISTA_DOBL", "LISTA_DOBLE", "LISTA_DOBLE_E", "LISTA_DOBLE_EN", "LISTA_DOBLE_ENL", "LISTA_DOBLE_ENLA", "LISTA_DOBLE_ENLAZ", "LISTA_DOBLE_ENLAZA", "LISTA_DOBLE_ENLAZAD", "LISTA_DOBLE_ENLAZADA",
            "LISTA_C", "LISTA_CI", "LISTA_CIR", "LISTA_CIRC", "LISTA_CIRCU", "LISTA_CIRCUL", "LISTA_CIRCULA", "LISTA_CIRCULAR",
            "LL", "LLE", "LLEN", "LLENA", "LLENAT",
            // M (MOSTRAR)
            "M", "MO", "MOS", "MOST", "MOSTR", "MOSTRA", "MOSTRAR",
            // N (NODOS)
            "N", "NO", "NOD", "NODO", "NODOS",
            // O (Esta sección se usaba para "INORDEN" antes, ya no es necesaria)
            // R (RECORRER, REHASH)
            "R", "RE", "REC", "RECO", "RECOR", "RECORR", "RECORRE", "RECORRER", "RECORRER_",
            "RECORRERAD", "RECORRERADE", "RECORRERADEL", "RECORRERADELA", "RECORRERADELAN", "RECORRERADELANT", "RECORRERADELANTE",
            "RECORRERAT", "RECORRERATR", "RECORRERATRA", "RECORRERATRAS",
            "RECORRIDOPORNIVELES", "REHASH",
            // T (TOPE, TAMAÑO, TABLASHASH)
            "T", "TO", "TOP", "TOPE", "TAM", "TAMA", "TAMAÑ", "TAMAÑO",
            "TAB", "TABL", "TABLA", "TABLAS", "TABLAS_", "TABLAS_H", "TABLAS_HA", "TABLAS_HAS", "TABLAS_HASH",
            // V (VECINOS, VACIA, VERFILA, VALOR)
            "V", "VE", "VEC", "VECI", "VECIN", "VECINO", "VECINOS",
            "VA", "VAC", "VACI", "VACIA", "VACIAT",
            "VER", "VERF", "VERFI", "VERFIL", "VERFILA", "VAL", "VALO", "VALOR"
        );
    }
    
    // Conjunto de caracteres en mayúsculas, dígitos y '_' (necesario para el AFD)
    private static Set<Character> getAlfabetoDSL() {
        Set<Character> alfabeto = new HashSet<>();
        for (char c = 'A'; c <= 'Z'; c++) alfabeto.add(c);
        for (char c = '0'; c <= '9'; c++) alfabeto.add(c);
        alfabeto.add('_');
        return alfabeto;
    }

    // Mapeo de transiciones (Estado Origen -> {Símbolo -> Estado Destino})
    private static Map<String, Map<Character, String>> getTransicionesDSL() {
        Map<String, Map<Character, String>> transiciones = new HashMap<>();

        BiConsumer<String, Map<Character, String>> add = (s, map) -> transiciones.put(s, map);
        
        // Transiciones iniciales (A, B, C, D, E, F, G, H, I, L, M, N, P, R, T, V)
        add.accept("INICIO", Map.ofEntries(
            Map.entry('A', "A"), Map.entry('B', "B"), Map.entry('C', "C"), Map.entry('D', "D"), Map.entry('E', "E"),
            Map.entry('F', "F"), Map.entry('G', "G"), Map.entry('H', "H"), Map.entry('I', "I"), Map.entry('L', "L"),
            Map.entry('M', "M"), Map.entry('N', "N"), Map.entry('P', "P"), Map.entry('R', "R"), Map.entry('T', "T"), Map.entry('V', "V")
        ));
        
        // --- Transiciones para A ---
        add.accept("A", Map.of('R', "AR", 'G', "AG", 'P', "AP", 'C', "ACT", 'L', "AL"));
        // ARBOL_BINARIO
        add.accept("AR", Map.of('B', "ARB"));
        add.accept("ARB", Map.of('O', "ARBO"));
        add.accept("ARBO", Map.of('L', "ARBOL"));
        add.accept("ARBOL", Map.of('_', "ARBOL_B"));
        add.accept("ARBOL_B", Map.of('I', "ARBOL_BI"));
        add.accept("ARBOL_BI", Map.of('N', "ARBOL_BIN"));
        add.accept("ARBOL_BIN", Map.of('A', "ARBOL_BINA"));
        add.accept("ARBOL_BINA", Map.of('R', "ARBOL_BINAR"));
        add.accept("ARBOL_BINAR", Map.of('I', "ARBOL_BINARI"));
        add.accept("ARBOL_BINARI", Map.of('O', "ARBOL_BINARIO"));
        // AGREGAR(NODO/ARISTA)
        add.accept("AG", Map.of('R', "AGR"));
        add.accept("AGR", Map.of('E', "AGRE"));
        add.accept("AGRE", Map.of('G', "AGREG"));
        add.accept("AGREG", Map.of('A', "AGREGA"));
        add.accept("AGREGA", Map.of('R', "AGREGAR"));
        add.accept("AGREGAR", Map.of('N', "AGREGARNODO", 'A', "AGREGARARISTA"));
        add.accept("AGREGARNODO", Map.of('O', "AGREGARNODO")); // Estado de aceptación
        add.accept("AGREGARARISTA", Map.of('A', "AGREGARARISTA")); // Estado de aceptación
        // APILAR
        add.accept("AP", Map.of('I', "API"));
        add.accept("API", Map.of('L', "APIL"));
        add.accept("APIL", Map.of('A', "APILA"));
        add.accept("APILA", Map.of('R', "APILAR"));
        // ACTUALIZAR
        add.accept("ACT", Map.of('U', "ACTU"));
        add.accept("ACTU", Map.of('A', "ACTUA"));
        add.accept("ACTUA", Map.of('L', "ACTUAL"));
        add.accept("ACTUAL", Map.of('I', "ACTUALI"));
        add.accept("ACTUALI", Map.of('Z', "ACTUALIZ"));
        add.accept("ACTUALIZ", Map.of('A', "ACTUALIZA"));
        add.accept("ACTUALIZA", Map.of('R', "ACTUALIZAR"));
        // ALTURA
        add.accept("AL", Map.of('T', "ALT"));
        add.accept("ALT", Map.of('U', "ALTU"));
        add.accept("ALTU", Map.of('R', "ALTUR"));
        add.accept("ALTUR", Map.of('A', "ALTURA"));


        // --- Transiciones para B ---
        add.accept("B", Map.of('I', "BI", 'U', "BU", 'F', "BF"));
        // BICOLAS
        add.accept("BI", Map.of('C', "BIC"));
        add.accept("BIC", Map.of('O', "BICO"));
        add.accept("BICO", Map.of('L', "BICOL"));
        add.accept("BICOL", Map.of('A', "BICOLA"));
        add.accept("BICOLA", Map.of('S', "BICOLAS"));
        // BUSCAR
        add.accept("BU", Map.of('S', "BUS"));
        add.accept("BUS", Map.of('C', "BUSC"));
        add.accept("BUSC", Map.of('A', "BUSCA"));
        add.accept("BUSCA", Map.of('R', "BUSCAR"));
        // BFS
        add.accept("BF", Map.of('S', "BFS"));


        // --- Transiciones para C ---
        add.accept("C", Map.of('O', "CO", 'A', "CA", 'L', "CL")); // Eliminada 'K' que no tiene uso
        // COLA/CON
        add.accept("CO", Map.of('L', "COL", 'N', "CON")); // CORRECCIÓN: Eliminada la clave 'L' duplicada
        add.accept("COL", Map.of('A', "COLA"));
        // CAMINOCORTO
        add.accept("CA", Map.of('M', "CAM"));
        add.accept("CAM", Map.of('I', "CAMI"));
        add.accept("CAMI", Map.of('N', "CAMIN"));
        add.accept("CAMIN", Map.of('O', "CAMINO"));
        add.accept("CAMINO", Map.of('C', "CAMINOC"));
        add.accept("CAMINOC", Map.of('O', "CAMINOCO"));
        add.accept("CAMINOCO", Map.of('R', "CAMINOCOR"));
        add.accept("CAMINOCOR", Map.of('T', "CAMINOCORT"));
        add.accept("CAMINOCORT", Map.of('O', "CAMINOCORTO"));
        // CLAVE
        add.accept("CL", Map.of('A', "CLA"));
        add.accept("CLA", Map.of('V', "CLAV"));
        add.accept("CLAV", Map.of('E', "CLAVE"));


        // --- Transiciones para D ---
        add.accept("D", Map.of('F', "DF", 'E', "DE", 'S', "DES"));
        // DFS
        add.accept("DF", Map.of('S', "DFS"));
        // DEQUEUE
        add.accept("DE", Map.of('Q', "DEQ"));
        add.accept("DEQ", Map.of('U', "DEQU"));
        add.accept("DEQU", Map.of('E', "DEQUE"));
        add.accept("DEQUE", Map.of('U', "DEQUEU"));
        add.accept("DEQUEU", Map.of('E', "DEQUEUE"));
        // DESAPILAR/DESENCOLAR
        add.accept("DES", Map.of('A', "DESA", 'E', "DESE"));
        add.accept("DESA", Map.of('P', "DESAP"));
        add.accept("DESAP", Map.of('I', "DESAPI"));
        add.accept("DESAPI", Map.of('L', "DESAPIL"));
        add.accept("DESAPIL", Map.of('A', "DESAPILA"));
        add.accept("DESAPILA", Map.of('R', "DESAPILAR"));
        add.accept("DESE", Map.of('N', "DESEN"));
        add.accept("DESEN", Map.of('C', "DESENC"));
        add.accept("DESENC", Map.of('O', "DESENCO"));
        add.accept("DESENCO", Map.of('L', "DESENCOL"));
        add.accept("DESENCOL", Map.of('A', "DESENCOLA"));
        add.accept("DESENCOLA", Map.of('R', "DESENCOLAR"));


        // --- Transiciones para E ---
        add.accept("E", Map.of('N', "EN", 'L', "EL"));
        // EN/ENQUEUE/ENCOLAR
        add.accept("EN", Map.of('Q', "ENQ", 'C', "ENC"));
        add.accept("ENQ", Map.of('U', "ENQU"));
        add.accept("ENQU", Map.of('E', "ENQUE"));
        add.accept("ENQUE", Map.of('U', "ENQUEU"));
        add.accept("ENQUEU", Map.of('E', "ENQUEUE"));
        add.accept("ENC", Map.of('O', "ENCO"));
        add.accept("ENCO", Map.of('L', "ENCOL"));
        add.accept("ENCOL", Map.of('A', "ENCOLA"));
        add.accept("ENCOLA", Map.of('R', "ENCOLAR"));
        // ELIMINAR(SINTAXIS)
        add.accept("EL", Map.of('I', "ELI"));
        add.accept("ELI", Map.of('M', "ELIM"));
        add.accept("ELIM", Map.of('I', "ELIMI"));
        add.accept("ELIMI", Map.of('N', "ELIMIN"));
        add.accept("ELIMIN", Map.of('A', "ELIMINA"));
        add.accept("ELIMINA", Map.of('R', "ELIMINAR"));
        add.accept("ELIMINAR", Map.of('_', "ELIMINAR_"));
        add.accept("ELIMINAR_", Map.of('F', "ELIMINAR_F", 'I', "ELIMINAR_I", 'P', "ELIMINAR_PO")); // FRENTE/FINAL, INICIO, POSICION
        // ELIMINAR_FRENTE
        add.accept("ELIMINAR_F", Map.of('R', "ELIMINAR_FR", 'I', "ELIMINAR_FINAL"));
        add.accept("ELIMINAR_FR", Map.of('E', "ELIMINAR_FRE"));
        add.accept("ELIMINAR_FRE", Map.of('N', "ELIMINAR_FREN"));
        add.accept("ELIMINAR_FREN", Map.of('T', "ELIMINAR_FRENT"));
        add.accept("ELIMINAR_FRENT", Map.of('E', "ELIMINAR_FRENTE"));
        // ELIMINAR_FINAL (ya está arriba en F)
        // ELIMINAR_INICIO
        add.accept("ELIMINAR_I", Map.of('N', "ELIMINAR_IN"));
        add.accept("ELIMINAR_IN", Map.of('I', "ELIMINAR_INI"));
        add.accept("ELIMINAR_INI", Map.of('C', "ELIMINAR_INIC"));
        add.accept("ELIMINAR_INIC", Map.of('I', "ELIMINAR_INICI"));
        add.accept("ELIMINAR_INIC", Map.of('O', "ELIMINAR_INICIO"));
        // ELIMINAR_POSICION
        add.accept("ELIMINAR_PO", Map.of('S', "ELIMINAR_POS"));
        add.accept("ELIMINAR_POS", Map.of('I', "ELIMINAR_POSI"));
        add.accept("ELIMINAR_POSI", Map.of('C', "ELIMINAR_POSIC"));
        add.accept("ELIMINAR_POSIC", Map.of('I', "ELIMINAR_POSICI"));
        add.accept("ELIMINAR_POSICI", Map.of('O', "ELIMINAR_POSICIO"));
        add.accept("ELIMINAR_POSICIO", Map.of('N', "ELIMINAR_POSICION"));


        // --- Transiciones para F ---
        add.accept("F", Map.of('R', "FR"));
        add.accept("FR", Map.of('E', "FRE", 'O', "FRO"));
        // FRENTE
        add.accept("FRE", Map.of('N', "FREN"));
        add.accept("FREN", Map.of('T', "FRENT"));
        add.accept("FRENT", Map.of('E', "FRENTE"));
        // FRONT
        add.accept("FRO", Map.of('N', "FRON"));
        add.accept("FRON", Map.of('T', "FRONT"));


        // --- Transiciones para G ---
        add.accept("G", Map.of('R', "GR"));
        add.accept("GR", Map.of('A', "GRA"));
        add.accept("GRA", Map.of('F', "GRAF"));
        add.accept("GRAF", Map.of('O', "GRAFO"));
        add.accept("GRAFO", Map.of('S', "GRAFOS"));


        // --- Transiciones para H ---
        add.accept("H", Map.of('O', "HO"));
        add.accept("HO", Map.of('J', "HOJ"));
        add.accept("HOJ", Map.of('A', "HOJA"));
        add.accept("HOJA", Map.of('S', "HOJAS"));


        // --- Transiciones para I ---
        add.accept("I", Map.of('N', "IN"));
        add.accept("IN", Map.of('S', "INS", 'O', "INOR")); // INSERTAR, INORDEN
        // INSERTAR(SINTAXIS)
        add.accept("INS", Map.of('E', "INSE"));
        add.accept("INSE", Map.of('R', "INSER"));
        add.accept("INSER", Map.of('T', "INSERT"));
        add.accept("INSERT", Map.of('A', "INSERTA"));
        add.accept("INSERTA", Map.of('R', "INSERTAR"));
        add.accept("INSERTAR", Map.of('_', "INSERTAR_", 'I', "INSERTARIZQUIERDA", 'D', "INSERTARDERECHA"));
        add.accept("INSERTAR_", Map.of('F', "INSERTAR_F", 'I', "INSERTAR_I", 'E', "INSERTAR_EN_POSICION"));
        // INSERTAR_FRENTE/FINAL
        add.accept("INSERTAR_F", Map.of('R', "INSERTAR_FRENTE", 'I', "INSERTAR_FINAL"));
        // INSERTAR_INICIO
        add.accept("INSERTAR_I", Map.of('N', "INSERTAR_IN"));
        add.accept("INSERTAR_IN", Map.of('I', "INSERTAR_INI"));
        add.accept("INSERTAR_INI", Map.of('C', "INSERTAR_INIC"));
        add.accept("INSERTAR_INIC", Map.of('I', "INSERTAR_INICI"));
        add.accept("INSERTAR_INIC", Map.of('O', "INSERTAR_INICIO"));
        // INSERTAR_EN_POSICION (simplificado, ya que es largo)
        add.accept("INSERTAR_EN_POSICION", Map.of()); // Estado de aceptación
        // INORDEN
        add.accept("INOR", Map.of('D', "INORD"));
        add.accept("INORD", Map.of('E', "INORDE"));
        add.accept("INORDE", Map.of('N', "INORDEN"));
        // INSERTARIZQUIERDA / INSERTARDERECHA
        add.accept("INSERTARIZQUIERDA", Map.of()); // Estado de aceptación
        add.accept("INSERTARDERECHA", Map.of()); // Estado de aceptación


        // --- Transiciones para L ---
        add.accept("L", Map.of('I', "LI", 'L', "LL"));
        // LISTA_...
        add.accept("LI", Map.of('S', "LIS"));
        add.accept("LIS", Map.of('T', "LIST"));
        add.accept("LIST", Map.of('A', "LISTA"));
        add.accept("LISTA", Map.of('_', "LISTA_"));
        add.accept("LISTA_", Map.of('E', "LISTA_E", 'D', "LISTA_D", 'C', "LISTA_C")); // ENLAZADAS, DOBLE, CIRCULAR
        // LISTA_ENLAZADAS
        add.accept("LISTA_E", Map.of('N', "LISTA_EN"));
        add.accept("LISTA_EN", Map.of('L', "LISTA_ENL"));
        add.accept("LISTA_ENL", Map.of('A', "LISTA_ENLA"));
        add.accept("LISTA_ENLA", Map.of('Z', "LISTA_ENLAZ"));
        add.accept("LISTA_ENLAZ", Map.of('A', "LISTA_ENLAZA"));
        add.accept("LISTA_ENLAZA", Map.of('D', "LISTA_ENLAZAD"));
        add.accept("LISTA_ENLAZAD", Map.of('A', "LISTA_ENLADA"));
        add.accept("LISTA_ENLADA", Map.of('S', "LISTA_ENLAZADAS"));
        // LISTA_DOBLE_ENLAZADA
        add.accept("LISTA_D", Map.of('O', "LISTA_DO"));
        add.accept("LISTA_DO", Map.of('B', "LISTA_DOB"));
        add.accept("LISTA_DOB", Map.of('L', "LISTA_DOBL"));
        add.accept("LISTA_DOBL", Map.of('E', "LISTA_DOBLE"));
        add.accept("LISTA_DOBLE", Map.of('_', "LISTA_DOBLE_E"));
        add.accept("LISTA_DOBLE_E", Map.of('N', "LISTA_DOBLE_EN"));
        add.accept("LISTA_DOBLE_EN", Map.of('L', "LISTA_DOBLE_ENL"));
        add.accept("LISTA_DOBLE_ENL", Map.of('A', "LISTA_DOBLE_ENLA"));
        add.accept("LISTA_DOBLE_ENLA", Map.of('Z', "LISTA_DOBLE_ENLAZ"));
        add.accept("LISTA_DOBLE_ENLAZ", Map.of('A', "LISTA_DOBLE_ENLAZA"));
        add.accept("LISTA_DOBLE_ENLAZA", Map.of('D', "LISTA_DOBLE_ENLAZAD"));
        add.accept("LISTA_DOBLE_ENLAZAD", Map.of('A', "LISTA_DOBLE_ENLAZADA"));
        // LISTA_CIRCULAR
        add.accept("LISTA_C", Map.of('I', "LISTA_CI"));
        add.accept("LISTA_CI", Map.of('R', "LISTA_CIR"));
        add.accept("LISTA_CIR", Map.of('C', "LISTA_CIRC"));
        add.accept("LISTA_CIRC", Map.of('U', "LISTA_CIRCU"));
        add.accept("LISTA_CIRC", Map.of('L', "LISTA_CIRCUL"));
        add.accept("LISTA_CIRCUL", Map.of('A', "LISTA_CIRCULA"));
        add.accept("LISTA_CIRCULA", Map.of('R', "LISTA_CIRCULAR"));
        // LLENA
        add.accept("LL", Map.of('E', "LLE"));
        add.accept("LLE", Map.of('N', "LLEN"));
        add.accept("LLEN", Map.of('A', "LLENA"));
        add.accept("LLENA", Map.of('T', "LLENAT"));


        // --- Transiciones para M --- (MOSTRAR)
        add.accept("M", Map.of('O', "MO"));
        add.accept("MO", Map.of('S', "MOS"));
        add.accept("MOS", Map.of('T', "MOST"));
        add.accept("MOST", Map.of('R', "MOSTR"));
        add.accept("MOSTR", Map.of('A', "MOSTRA"));
        add.accept("MOSTRA", Map.of('R', "MOSTRAR"));


        // --- Transiciones para N ---
        add.accept("N", Map.of('O', "NO"));
        add.accept("NO", Map.of('D', "NOD"));
        add.accept("NOD", Map.of('O', "NODO"));
        add.accept("NODO", Map.of('S', "NODOS"));


        // --- Transiciones para P ---
        add.accept("P", Map.of('I', "PI", 'O', "PO", 'U', "PU", 'R', "PRE"));
        // PILA/PILA_CIRCULAR (continúa de arriba)
        add.accept("PI", Map.of('L', "PIL"));
        add.accept("PIL", Map.of('A', "PILA"));
        add.accept("PILA", Map.of('_', "PILA_C"));
        add.accept("PILA_C", Map.of('I', "PILA_CI"));
        add.accept("PILA_CI", Map.of('R', "PILA_CIR"));
        add.accept("PILA_CIR", Map.of('C', "PILA_CIRC"));
        add.accept("PILA_CIRC", Map.of('U', "PILA_CIRCU"));
        add.accept("PILA_CIRC", Map.of('L', "PILA_CIRCUL"));
        add.accept("PILA_CIRCUL", Map.of('A', "PILA_CIRCULA"));
        add.accept("PILA_CIRCULA", Map.of('R', "PILA_CIRCULAR"));
        // POP
        add.accept("PO", Map.of('P', "POP", 'S', "POST"));
        // PUSH
        add.accept("PU", Map.of('S', "PUS"));
        add.accept("PUS", Map.of('H', "PUSH"));
        // PREORDEN
        add.accept("PRE", Map.of('O', "PREO"));
        add.accept("PREO", Map.of('R', "PREOR"));
        add.accept("PREOR", Map.of('D', "PREORD"));
        add.accept("PREORD", Map.of('E', "PREORDE"));
        add.accept("PREORDE", Map.of('N', "PREORDEN"));
        // POSTORDEN
        add.accept("POST", Map.of('O', "POSTO"));
        add.accept("POSTO", Map.of('R', "POSTOR"));
        add.accept("POSTOR", Map.of('D', "POSTORD"));
        add.accept("POSTORD", Map.of('E', "POSTORDE"));
        add.accept("POSTORDE", Map.of('N', "POSTORDEN"));


        // --- Transiciones para R ---
        add.accept("R", Map.of('E', "RE"));
        add.accept("RE", Map.of('C', "REC", 'H', "REH"));
        // RECORRER(SINTAXIS)
        add.accept("REC", Map.of('O', "RECO"));
        add.accept("RECO", Map.of('R', "RECOR"));
        add.accept("RECOR", Map.of('R', "RECORR"));
        add.accept("RECORR", Map.of('E', "RECORRE"));
        add.accept("RECORRE", Map.of('R', "RECORRER"));
        add.accept("RECORRER", Map.of('A', "RECORRERAD", 'I', "RECORRER_I", 'P', "RECORRIDOPORNIVELES"));
        // RECORRERAD(ELANTE)/RECORRERAT(RAS)
        add.accept("RECORRERAD", Map.of('E', "RECORRERADE"));
        add.accept("RECORRERADE", Map.of('L', "RECORRERADEL"));
        add.accept("RECORRERADEL", Map.of('A', "RECORRERADELA"));
        add.accept("RECORRERADELA", Map.of('N', "RECORRERADELAN"));
        add.accept("RECORRERADELAN", Map.of('T', "RECORRERADELANT"));
        add.accept("RECORRERADELANT", Map.of('E', "RECORRERADELANTE"));
        add.accept("RECORRERAT", Map.of('R', "RECORRERATR"));
        add.accept("RECORRERATR", Map.of('A', "RECORRERATRA"));
        add.accept("RECORRERATR", Map.of('S', "RECORRERATRAS"));
        // RECORRIDOPORNIVELES
        add.accept("RECORRIDOPORNIVELES", Map.of()); // Estado de aceptación
        // REHASH
        add.accept("REH", Map.of('A', "REHA"));
        add.accept("REHA", Map.of('S', "REHAS"));
        add.accept("REHAS", Map.of('H', "REHASH"));


        // --- Transiciones para T ---
        add.accept("T", Map.of('O', "TO", 'A', "TA"));
        // TOPE
        add.accept("TO", Map.of('P', "TOP"));
        add.accept("TOP", Map.of('E', "TOPE"));
        // TAMAÑO
        add.accept("TA", Map.of('M', "TAM", 'B', "TAB"));
        add.accept("TAM", Map.of('A', "TAMA"));
        add.accept("TAMA", Map.of('Ñ', "TAMAÑ"));
        add.accept("TAMAÑ", Map.of('O', "TAMAÑO"));
        // TABLASHASH
        add.accept("TAB", Map.of('L', "TABL"));
        add.accept("TABL", Map.of('A', "TABLA"));
        add.accept("TABLA", Map.of('S', "TABLAS"));
        add.accept("TABLAS", Map.of('_', "TABLAS_H"));
        add.accept("TABLAS_H", Map.of('A', "TABLAS_HA"));
        add.accept("TABLAS_HA", Map.of('S', "TABLAS_HAS"));
        add.accept("TABLAS_HAS", Map.of('H', "TABLAS_HASH"));


        // --- Transiciones para V ---
        add.accept("V", Map.of('E', "VE", 'A', "VA"));
        // VECINOS
        add.accept("VE", Map.of('C', "VEC"));
        add.accept("VEC", Map.of('I', "VECI"));
        add.accept("VECI", Map.of('N', "VECIN"));
        add.accept("VECIN", Map.of('O', "VECINO"));
        add.accept("VECINO", Map.of('S', "VECINOS"));
        // VACIA / VACIAT
        add.accept("VA", Map.of('C', "VAC"));
        add.accept("VAC", Map.of('I', "VACI"));
        add.accept("VACI", Map.of('A', "VACIA"));
        add.accept("VACIA", Map.of('T', "VACIAT"));
        // VERFILA
        add.accept("VER", Map.of('F', "VERF"));
        add.accept("VERF", Map.of('I', "VERFI"));
        add.accept("VERFI", Map.of('L', "VERFIL"));
        add.accept("VERFIL", Map.of('A', "VERFILA"));
        // VALOR (ya estaba en el archivo anterior)
        add.accept("VAL", Map.of('O', "VALO"));
        add.accept("VALO", Map.of('R', "VALOR"));


        return transiciones;
    }

    // Conjunto de estados de aceptación (Palabras Reservadas completas)
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
            "IF", "ELSE", "MOSTRAR" // Añadidas para completar la lógica auxiliar
        );
    }

    // --- Funciones de Tokenización (se mantienen) ---
    
    /**
     * Pre-tokeniza una línea separando lexemas por espacios y delimitadores.
     * Esto actúa como el pre-procesador para el escaneo granular del AFD.
     */
    public static String[] tokenizarLinea(String entrada) {
        // 1. Manejo de comentarios (se eliminan hasta el final de la línea)
        int indiceComentario = entrada.indexOf('#');
        if (indiceComentario != -1) {
            entrada = entrada.substring(0, indiceComentario);
        }
        
        // 2. Normalizar espacios
        String tokenizada = entrada.trim().replaceAll("\\s+", " ");

        // 3. Separar operadores y delimitadores con espacios para aislarlos como tokens individuales.
        // Caracteres separados: ( ) [ ] { } , ; = + - * / < > ! & | .
        // El punto se añade para manejar sintaxis tipo objeto.
        tokenizada = tokenizada.replaceAll("([\\Q(){}[]|,;=+-*/<>\u0021&|.\\E])", " $1 ");

        // 4. Normalizar espacios de nuevo (limpiar espacios dobles generados)
        tokenizada = tokenizada.trim().replaceAll("\\s+", " ");

        // 5. Separar tokens finales y unirlos si son operadores compuestos.
        if (tokenizada.isEmpty()) return new String[0];
        String[] tokens = tokenizada.split(" ");
        
        List<String> listaTokens = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            String t = tokens[i];
            
            if (i + 1 < tokens.length) {
                String nextT = tokens[i + 1];
                String combined = t + nextT;
                // Reconstruir operadores de dos caracteres
                if (combined.equals("==") || combined.equals("!=") || combined.equals("<=") || combined.equals(">=") || combined.equals("&&") || combined.equals("||")) {
                    listaTokens.add(combined);
                    i++; // Saltar el siguiente token
                    continue;
                }
            }
            // Agregar token simple o el inicio de un operador compuesto fallido
            listaTokens.add(t);
        }

        return listaTokens.toArray(new String[0]);
    }

    /**
     * Itera sobre todas las líneas del código fuente para generar tokens iniciales.
     */
    public static Token[] tokenizador(String entrada) {
        String[] lineas = entrada.split("\n");
        List<Token> listaTokens = new ArrayList<>();
        
        int numLinea = 1;

        for (String linea : lineas) {
            String[] toks = tokenizarLinea(linea);

            for (String t : toks) {
                // Se agrega solo si no está vacío
                if (!t.trim().isEmpty()) {
                    listaTokens.add(new Token(t, numLinea));
                }
            }
            numLinea++;
        }

        return listaTokens.toArray(new Token[0]);
    }
    
    
    // --- FUNCIÓN MAIN DE PRUEBA ---
    
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
            234Inválido # Número y letra pegados -> Debería ser clasificado como ERROR
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
            if (tk.getTipoToken().equals("ERROR_LEXICO")) {
                erroresEncontrados++;
            }
        }
        
        System.out.println("\nResumen: " + tablaSimbolos.length + " tokens procesados. " +
                           erroresEncontrados + " errores léxicos.");
    }
}