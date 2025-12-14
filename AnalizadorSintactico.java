import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalizadorSintactico {
    private final Token[] tokens;
    private int actual;
    private final List<String> logDerivacion; 
    private final List<String> errores;       

    private static class ParserException extends RuntimeException {
        public ParserException(String message) {
            super(message);
        }
    }

    public AnalizadorSintactico(Token[] tokens) {
        this.tokens = tokens;
        this.actual = 0;
        this.logDerivacion = new ArrayList<>();
        this.errores = new ArrayList<>();
    }

    public void analizar() {
        logDerivacion.add("Inicio del Análisis Sintáctico...");
        try {
            programa();
        } catch (Exception e) {
            errores.add("Error irrecuperable: " + e.getMessage());
        }
        logDerivacion.add("Fin del Análisis.");
    }

    public List<String> getLogDerivacion() { return logDerivacion; }
    public List<String> getErrores() { return errores; }

    // =========================================================================
    // ======================== CORRECCIÓN CLAVE AQUÍ ==========================
    // =========================================================================

    // <Programa>
    private void programa() {
        while (!esFin()) {
            // FIX DEL CONGELAMIENTO:
            // Si encontramos una llave de cierre '}' en el nivel principal (programa),
            // significa que hubo un error dentro de un bloque y la recuperación nos dejó aquí.
            // Debemos consumirla para evitar el bucle infinito.
            if (check("}")) {
                // Opcional: Reportar que sobró una llave si quieres ser estricto, 
                // pero para recuperación suave, solo la saltamos.
                // errores.add("Se encontró una llave '}' inesperada (posible error de cierre previo).");
                avanzar(); 
                continue; 
            }

            try {
                sentencia();
            } catch (ParserException e) {
                // MODO PÁNICO
                errores.add(e.getMessage());
                sincronizar();
            }
        }
    }

    private void sentencia() {
        if (esFin()) return; // Protección extra

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
            consumir(";"); 
        }
        else {
            throw error("Se esperaba sentencia (CREAR, IF, INSERTAR...), se encontró: '" + lexema + "'");
        }
    }

    // --- MÉTODOS DE REGLAS (Igual que antes, con pequeñas protecciones) ---

    private void declaracion() {
        logDerivacion.add("<Sentencia> -> Declaración");
        consumir("CREAR");
        
        if (!esTipoEstructura(tokenActual().getLexema().toUpperCase())) {
            throw error("Tipo de estructura desconocido: " + tokenActual().getLexema());
        }
        consumir(tokenActual().getLexema()); 
        consumir("IDENTIFICADOR");
        
        if (tokenActual().getTipoToken().equals("LITERAL_NUMERICA")) {
             consumir("LITERAL_NUMERICA");
        }
        consumir(";");
        logDerivacion.add("   Declaración correcta.");
    }

    private void operacionEstructura() {
        logDerivacion.add("<Sentencia> -> Operación de Estructura");
        String verbo = tokenActual().getLexema().toUpperCase();
        consumir(verbo); 

        // Lógica para detectar errores como "ELIMINAR miPila" (Falta EN)
        // Verificamos si el siguiente token es 'EN' antes de intentar consumirlo ciegamente
        // para dar un mensaje de error más específico si falta.

        if (esVerboSinParametros(verbo)) {
            consumir("EN");
            consumir("IDENTIFICADOR");
        }
        else if (verbo.equals("INSERTAR_EN_POSICION")) {
            expresion(); 
            consumir("EN");
            consumir("IDENTIFICADOR");
            consumir("CON");
            consumir("VALOR");
            expresion(); 
        }
        else if (verbo.equals("AGREGARARISTA") || verbo.equals("CAMINOCORTO")) {
            expresion();
            expresion();
            consumir("EN");
            consumir("IDENTIFICADOR");
             if (check("CON") || check("PESO")) {
                 avanzar();
                 expresion();
            }
        }
        else if (verbo.equals("ELIMINAR_POSICION")) {
            expresion();
            consumir("EN");
            consumir("IDENTIFICADOR");
        }
        else {
            // Caso estándar: INSERTAR 5 EN pila
            if (!verbo.startsWith("ELIMINAR") && !verbo.startsWith("DES") && !verbo.startsWith("POP")) {
                expresion(); 
            }
            
            // Aquí es donde ocurría tu error. Si falta EN, lanzará error.
            if (!check("EN")) {
                throw error("Falta la palabra reservada 'EN' después del valor/operación.");
            }
            consumir("EN");
            consumir("IDENTIFICADOR");
        }

        consumir(";");
        logDerivacion.add("   Operación '" + verbo + "' válida.");
    }

    private void controlFlujoIf() {
        logDerivacion.add("<Sentencia> -> Control IF");
        consumir("IF");
        consumir("(");
        condicion();
        consumir(")");
        consumir("{");
        
        // Bloque del IF protegido
        while (!check("}") && !esFin()) {
            try {
                sentencia();
            } catch (ParserException e) {
                // Si falla una sentencia DENTRO del IF, sincronizamos localmente
                // para intentar seguir leyendo el resto del bloque
                errores.add(e.getMessage());
                sincronizar();
            }
        }
        consumir("}");

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
            throw error("Se esperaba operador relacional (==, >, <), hallado: " + tokenActual().getLexema());
        }
    }

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
            throw error("Expresión inválida. Se esperaba valor, ID o propiedad. Encontrado: " + lexema);
        }
    }

    // =========================================================================
    // ======================== MÉTODOS AUXILIARES =============================
    // =========================================================================

    private void consumir(String expected) {
        if (esFin()) {
            throw error("Final inesperado. Se esperaba: " + expected);
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
            throw error("Se esperaba '" + expected + "', se encontró '" + t.getLexema() + "'");
        }
    }

    private boolean check(String expected) {
        if (esFin()) return false;
        Token t = tokenActual();
        return t.getLexema().equalsIgnoreCase(expected) || t.getTipoToken().equals(expected);
    }
    
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

    private ParserException error(String mensaje) {
        int linea = esFin() ? tokens[tokens.length-1].getLinea() : tokenActual().getLinea();
        return new ParserException("Error Sintáctico [Línea " + linea + "]: " + mensaje);
    }

    // --- ALGORITMO DE RECUPERACIÓN MEJORADO ---
    private void sincronizar() {
        logDerivacion.add("   >> RECUPERANDO... (Buscando ';' o cierre de bloque)");
        
        // Proteccion contra loops: Si entramos a sincronizar y YA estamos en un punto de sincronización,
        // forzamos avanzar uno para no quedarnos atascados infinitamente en el mismo token.
        if (check(";") || check("}")) {
            avanzar();
        }

        while (!esFin()) {
            String lexema = tokenActual().getLexema();
            
            // Puntos de sincronización seguros
            if (lexema.equals(";")) {
                avanzar(); 
                return;
            }
            if (lexema.equals("}")) {
                // No consumimos la llave aquí, dejamos que la maneje el bloque superior (if o programa)
                return; 
            }
            
            // Heurística: Si vemos el inicio de otra sentencia clara, paramos
            String lexUpper = lexema.toUpperCase();
            if (Set.of("CREAR", "IF", "MOSTRAR", "INSERTAR", "APILAR", "ELIMINAR", "WHILE").contains(lexUpper)) {
                return;
            }

            avanzar();
        }
    }

    // --- CONJUNTOS DE VALIDACIÓN ---
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