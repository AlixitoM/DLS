import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
  Analizador Sintáctico (Parser) implementado como un Descendente Recursivo.
  Esta clase se encarga de validar que la secuencia de tokens generada por el 
  Analizador Léxico (AFD) cumpla con las reglas gramaticales del lenguaje (Gramática Libre de Contexto).
  Funciona solicitando tokens uno a uno y verificando si coinciden con la estructura esperada
  (Reglas de Producción). Si encuentra un error, utiliza una estrategia de recuperación 
  "Modo Pánico" para intentar seguir analizando el resto del código.
 */
public class AnalizadorSintactico {
    private final Token[] tokens;
    private int actual; // Puntero al token actual en el flujo
    private final List<String> logDerivacion; // Historial de reglas aplicadas (para mostrar el árbol)
    private final List<String> errores;       // Lista de errores encontrados

    // Excepción personalizada para el control de flujo de errores sintácticos
    private static class ParserException extends RuntimeException {
        public ParserException(String message) {
            super(message);
        }
    }

    /**
      Constructor del Analizador Sintáctico.
      @param tokens Arreglo de tokens provenientes del análisis léxico.
     */
    public AnalizadorSintactico(Token[] tokens) {
        this.tokens = tokens;
        this.actual = 0;
        this.logDerivacion = new ArrayList<>();
        this.errores = new ArrayList<>();
    }

    /*
      Método principal que inicia el proceso de compilación sintáctica.
      Envuelve la llamada al símbolo inicial <Programa> en un bloque try-catch general.
     */
    public void analizar() {
        logDerivacion.add("Inicio del Análisis Sintáctico...");
        try {
            programa(); // Llamada al símbolo inicial de la gramática
        } catch (Exception e) {
            errores.add("DSL(999) Error irrecuperable: " + e.getMessage());
        }
        logDerivacion.add("Fin del Análisis.");
    }

    public List<String> getLogDerivacion() { return logDerivacion; }
    public List<String> getErrores() { return errores; }

    // -------------------------------------------------------------------------
    // --- REGLAS DE PRODUCCIÓN (MÉTODOS RECURSIVOS) ---------------------------
    // -------------------------------------------------------------------------

    /*
      Regla: <Programa>
      Representa el bucle principal que procesa sentencias hasta llegar al final del archivo (EOF).
      Incluye lógica de recuperación de errores para evitar que el compilador se detenga 
      en el primer fallo.
     */
    private void programa() {
        while (!esFin()) {
            // Protección contra ciclos infinitos en recuperación:
            // Si encontramos una llave de cierre suelta en el nivel principal, la ignoramos.
            if (check("}")) {
                avanzar(); 
                continue; 
            }

            try {
                sentencia(); // Intenta procesar una sentencia completa
            } catch (ParserException e) {
                // Estrategia de recuperación: Registrar error y Sincronizar (Panic Mode)
                errores.add(e.getMessage()); 
                sincronizar();
            }
        }
    }

    /*
      Regla: <Sentencia>
      Actúa como un "Dispatcher". Mira el token actual (Lookahead) para decidir 
      a qué regla específica derivar (Declaración, Operación, If, etc.).
     */
    private void sentencia() {
        if (esFin()) return; 

        String lexema = tokenActual().getLexema().toUpperCase();

        if (lexema.equals("CREAR")) {
            declaracion();
        } 
        else if (esVerboOperacion(lexema)) {
            operacionEstructura();
        }
        else if (lexema.equals("IF")) {
            controlFlujoIf();
        }
        else if (lexema.equals("MOSTRAR")) {
            salida();
        }
        else if (tokenActual().getTipoToken().equals("IDENTIFICADOR")) {
            asignacion();
        }
        else if (lexema.equals(";")) {
            consumir(";"); // Sentencia vacía válida
        }
        else {
            // Si el token no coincide con ningún inicio de sentencia válido, es un error.
            throw error("Se esperaba sentencia (CREAR, IF, INSERTAR...), se encontró: '" + lexema + "'", 203);
        }
    }

    /*
      Regla: <Declaración>
      Valida la creación de estructuras de datos. 
      Ej: CREAR PILA miPila;
     */
    private void declaracion() {
        logDerivacion.add("<Sentencia> -> Declaración");
        consumir("CREAR");
        
        if (!esTipoEstructura(tokenActual().getLexema().toUpperCase())) {
            throw error("Tipo de estructura desconocido: " + tokenActual().getLexema(), 204);
        }
        consumir(tokenActual().getLexema()); // Consumir Tipo (PILA, COLA, etc.)
        consumir("IDENTIFICADOR");
        
        // Soporte opcional para tamaño en la declaración (ej. arreglos o estructuras acotadas)
        if (tokenActual().getTipoToken().equals("LITERAL_NUMERICA")) {
             consumir("LITERAL_NUMERICA");
        }
        consumir(";");
        logDerivacion.add("   Declaración correcta.");
    }

    /*
      Regla: <Operación>
      Maneja verbos de acción sobre las estructuras (INSERTAR, ELIMINAR, etc.).
      Valida la sintaxis específica de cada verbo (parametros necesarios).
     */
    private void operacionEstructura() {
        logDerivacion.add("<Sentencia> -> Operación de Estructura");
        String verbo = tokenActual().getLexema().toUpperCase();
        consumir(verbo); 

        // Caso 1: Verbos sin valores extra (ej. ELIMINAR EN pila)
        if (esVerboSinParametros(verbo)) {
            if (!check("EN")) throw error("Falta la palabra reservada 'EN' después de " + verbo, 205);
            consumir("EN");
            consumir("IDENTIFICADOR");
        }
        // Caso 2: Inserción compleja con posición y valor
        else if (verbo.equals("INSERTAR_EN_POSICION")) {
            expresion(); // Posición
            consumir("EN");
            consumir("IDENTIFICADOR");
            consumir("CON");
            consumir("VALOR");
            expresion(); // Valor a insertar
        }
        // Caso 3: Operaciones de Grafos
        else if (verbo.equals("AGREGARARISTA") || verbo.equals("CAMINOCORTO")) {
            expresion(); // Origen
            expresion(); // Destino
            consumir("EN");
            consumir("IDENTIFICADOR");
             if (check("CON") || check("PESO")) {
                 avanzar();
                 expresion(); // Peso
            }
        }
        // Caso 4: Eliminación por posición
        else if (verbo.equals("ELIMINAR_POSICION")) {
            expresion();
            consumir("EN");
            consumir("IDENTIFICADOR");
        }
        // Caso 5: Estándar (INSERTAR valor EN estructura)
        else {
            if (!verbo.startsWith("ELIMINAR") && !verbo.startsWith("DES") && !verbo.startsWith("POP")) {
                expresion(); 
            }
            
            if (!check("EN")) {
                throw error("Falta la palabra reservada 'EN' después del valor/operación.", 205);
            }
            consumir("EN");
            consumir("IDENTIFICADOR");
        }

        consumir(";");
        logDerivacion.add("   Operación '" + verbo + "' válida.");
    }

    /*
      Regla: <IF>
      Valida la estructura de control IF-ELSE, incluyendo el bloque de código entre llaves.
     */
    private void controlFlujoIf() {
        logDerivacion.add("<Sentencia> -> Control IF");
        consumir("IF");
        consumir("(");
        condicion();
        consumir(")");
        consumir("{");
        
        // Procesar sentencias dentro del bloque IF
        while (!check("}") && !esFin()) {
            try {
                sentencia();
            } catch (ParserException e) {
                // Recuperación local dentro del bloque para no perder todo el IF
                errores.add(e.getMessage());
                sincronizar();
            }
        }
        consumir("}");

        // Parte opcional ELSE
        if (match("ELSE")) {
            consumir("{");
            while (!check("}") && !esFin()) {
                try {
                    sentencia();
                } catch (ParserException e) {
                    errores.add(e.getMessage());
                    sincronizar();
                }
            }
            consumir("}");
        }
    }

    private void salida() {
        logDerivacion.add("<Sentencia> -> Salida");
        consumir("MOSTRAR");
        expresion();
        consumir(";");
    }

    private void asignacion() {
        logDerivacion.add("<Sentencia> -> Asignación");
        consumir("IDENTIFICADOR");
        consumir("="); 
        expresion();
        consumir(";");
    }

    private void condicion() {
        expresion();
        if (esOperadorRelacional(tokenActual().getLexema())) {
            consumir(tokenActual().getLexema());
            expresion();
        } else {
            throw error("Se esperaba operador relacional (==, >, <), hallado: " + tokenActual().getLexema(), 206);
        }
    }

    // --- REGLAS PARA EXPRESIONES ARITMÉTICAS ---
    
    private void expresion() {
        termino();
        while (check("+") || check("-")) {
            consumir(tokenActual().getLexema());
            termino();
        }
    }

    private void termino() {
        factor();
        while (check("*") || check("/")) {
            consumir(tokenActual().getLexema());
            factor();
        }
    }

    private void factor() {
        String lexema = tokenActual().getLexema().toUpperCase();
        String tipo = tokenActual().getTipoToken();

        if (esPropiedad(lexema)) {
            consumir(lexema);
            consumir("EN");
            consumir("IDENTIFICADOR");
        } 
        else if (lexema.equals("(")) {
            consumir("(");
            expresion();
            consumir(")");
        }
        else if (tipo.equals("IDENTIFICADOR")) {
            consumir("IDENTIFICADOR");
        }
        else if (tipo.equals("LITERAL_NUMERICA")) {
            consumir("LITERAL_NUMERICA");
        }
        else if (tipo.equals("LITERAL_CADENA")) {
            consumir("LITERAL_CADENA");
        }
        else {
            throw error("Expresión inválida. Se esperaba valor, ID o propiedad. Encontrado: " + lexema, 207);
        }
    }

    // =========================================================================
    // ======================== MÉTODOS AUXILIARES (CORE DEL PARSER) ===========
    // =========================================================================

    /**
      Verifica que el token actual coincida con el esperado y avanza el puntero.
      Si no coincide, lanza una excepción (Error Sintáctico).
      @param expected Lexema o Tipo de token esperado.
     */
    private void consumir(String expected) {
        if (esFin()) {
            throw error("Final inesperado. Se esperaba: " + expected, 299);
        }

        Token t = tokenActual();
        String lexema = t.getLexema().toUpperCase();
        String tipo = t.getTipoToken();

        boolean match = false;
        if (lexema.equals(expected.toUpperCase())) match = true;
        else if (tipo.equals(expected)) match = true;

        if (match) {
            avanzar();
        } else {
            // Lógica de autodetección de código de error para mensajes más precisos
            int codigoError = 203; // Código genérico
            
            if (expected.equals(";")) codigoError = 201;      // Falta punto y coma
            else if (expected.equals("}")) codigoError = 202; // Falta cierre de bloque
            else if (expected.equals(")")) codigoError = 202; // Falta paréntesis
            else if (expected.equals("EN")) codigoError = 205;// Falta palabra reservada EN
            else if (expected.equals("IDENTIFICADOR")) codigoError = 208; // Falta ID

            throw error("Se esperaba '" + expected + "', se encontró '" + t.getLexema() + "'", codigoError);
        }
    }

    /*
      Verifica el token actual sin consumirlo (Lookahead).
     */
    private boolean check(String expected) {
        if (esFin()) return false;
        Token t = tokenActual();
        return t.getLexema().equalsIgnoreCase(expected) || t.getTipoToken().equals(expected);
    }
    
    /*
      Si el token actual coincide, lo consume y retorna true. Si no, retorna false.
     */
    private boolean match(String expected) {
        if (check(expected)) {
            avanzar();
            return true;
        }
        return false;
    }

    private Token tokenActual() {
        if (actual >= tokens.length) return tokens[tokens.length - 1];
        return tokens[actual];
    }

    private void avanzar() {
        if (actual < tokens.length) actual++;
    }

    private boolean esFin() {
        return actual >= tokens.length;
    }

    /*
      Genera la excepción de error sintáctico con formato estándar DSL(id).
     */
    private ParserException error(String mensaje, int codigo) {
        int linea = esFin() ? tokens[tokens.length-1].getLinea() : tokenActual().getLinea();
        // Formato estandarizado: "DSL(201) [Línea 5]: Mensaje..."
        return new ParserException("DSL(" + codigo + ") [Línea " + linea + "]: " + mensaje);
    }

    /*
      Algoritmo de recuperación "Modo Pánico".
      Avanza tokens ciegamente hasta encontrar un punto de sincronización seguro
      (un punto y coma o una llave de cierre) para intentar retomar el análisis.
     */
    private void sincronizar() {
        logDerivacion.add("   >> RECUPERANDO... (Buscando ';' o cierre de bloque)");
        
        // Evitar bucle si ya estamos sobre un sincronizador
        if (check(";") || check("}")) avanzar();
        
        while (!esFin()) {
            String lexema = tokenActual().getLexema();
            
            // Puntos de sincronización fuertes
            if (lexema.equals(";")) { avanzar(); return; }
            if (lexema.equals("}")) { return; } // No consumir }, dejar que el bloque superior lo maneje
            
            // Heurística: Si encontramos el inicio de una nueva sentencia, asumimos que el error terminó
            String lexUpper = lexema.toUpperCase();
            if (Set.of("CREAR", "IF", "MOSTRAR", "INSERTAR", "APILAR", "ELIMINAR", "WHILE").contains(lexUpper)) {
                return;
            }
            avanzar();
        }
    }

    // --- CONJUNTOS DE VALIDACIÓN (SETS AUXILIARES) ---
    // Usamos Sets para búsquedas O(1) rápidas al validar tipos o verbos
    
    private boolean esTipoEstructura(String s) {
        return Set.of("PILA", "COLA", "BICOLA", "LISTA_ENLAZADA", "LISTA_CIRCULAR", 
                      "ARBOL_BINARIO", "TABLA_HASH", "GRAFO", "PILA_CIRCULAR").contains(s);
    }

    private boolean esVerboOperacion(String s) {
        return Set.of(
            "INSERTAR", "INSERTAR_FINAL", "INSERTAR_INICIO", "INSERTAR_EN_POSICION", 
            "INSERTARIZQUIERDA", "INSERTARDERECHA", "AGREGARNODO", "APILAR", "ENCOLAR", 
            "PUSH", "ENQUEUE", "ELIMINAR", "ELIMINAR_INICIO", "ELIMINAR_FINAL", 
            "ELIMINAR_FRENTE", "ELIMINAR_POSICION", "ELIMINARNODO", "DESAPILAR", "POP", 
            "DESENCOLAR", "DEQUEUE", "BUSCAR", "RECORRER", "BFS", "DFS", "AGREGARARISTA", 
            "ELIMINARARISTA", "ACTUALIZAR", "REHASH", "CAMINOCORTO"
        ).contains(s);
    }
    
    private boolean esVerboSinParametros(String s) {
        return Set.of(
            "ELIMINAR", "DESAPILAR", "POP", "DESENCOLAR", "DEQUEUE", 
            "ELIMINAR_INICIO", "ELIMINAR_FINAL", "ELIMINAR_FRENTE",
            "RECORRER", "RECORRERADELANTE", "RECORRERATRAS", "BFS", "DFS",
            "PREORDEN", "INORDEN", "POSTORDEN", "RECORRIDOPORNIVELES", "VACIAT"
        ).contains(s);
    }

    private boolean esPropiedad(String s) {
        return Set.of("TOPE", "FRENTE", "FRONT", "PEEK", "VERFILA", "CLAVE", 
                      "TAMANO", "ALTURA", "HOJAS", "NODOS", "VECINOS", "VACIAT", "LLENAT").contains(s);
    }
    
    private boolean esOperadorRelacional(String s) {
        return Set.of("==", "!=", "<", ">", "<=", ">=").contains(s);
    }
}