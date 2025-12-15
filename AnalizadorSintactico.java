import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Analizador Sintáctico (Parser) con Reporte Estructurado.
 * Genera un log jerárquico para visualizar el árbol de derivación en la GUI.
 */
public class AnalizadorSintactico {
    private final Token[] tokens;
    private int actual; 
    private final List<String> logDerivacion; 
    private final List<String> errores;
    
    // Variable para controlar la sangría del reporte visual
    private int nivelIndentacion = 0;

    // Excepción personalizada
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

    // --- MÉTODO AUXILIAR PARA EL REPORTE VISUAL ---
    // Agrega espacios o tuberías (|) según la profundidad del análisis
    private void log(String mensaje) {
        StringBuilder sb = new StringBuilder();
        // Por cada nivel de profundidad, agregamos una marca visual
        for (int i = 0; i < nivelIndentacion; i++) {
            sb.append("|  "); 
        }
        sb.append(mensaje);
        logDerivacion.add(sb.toString());
    }

    // Método principal
    public void analizar() {
        logDerivacion.clear(); // Limpiar log anterior si se reusa la instancia
        log("INICIO DEL ANÁLISIS SINTÁCTICO");
        try {
            programa(); 
        } catch (Exception e) {
            errores.add("DSL(999) Error irrecuperable: " + e.getMessage());
        }
        log("FIN DEL ANÁLISIS");
    }

    public List<String> getLogDerivacion() { return logDerivacion; }
    public List<String> getErrores() { return errores; }

    // -------------------------------------------------------------------------
    // --- REGLAS DE PRODUCCIÓN (CON LÓGICA DE INDENTACIÓN) --------------------
    // -------------------------------------------------------------------------

    private void programa() {
        nivelIndentacion++; // Entramos al nivel principal
        while (!esFin()) {
            if (check("}")) {
                avanzar(); 
                continue; 
            }

            try {
                sentencia(); 
            } catch (ParserException e) {
                errores.add(e.getMessage()); 
                sincronizar();
            }
        }
        nivelIndentacion--; // Salimos del nivel principal
    }

    private void sentencia() {
        if (esFin()) return; 

        String lexema = tokenActual().getLexema().toUpperCase();

        // Dispatcher: Decide qué regla llamar y gestiona la indentación del log
        if (lexema.equals("CREAR")) {
            log("<Sentencia> -> Declaración");
            nivelIndentacion++;
            declaracion();
            nivelIndentacion--;
        } 
        else if (esVerboOperacion(lexema)) {
            log("<Sentencia> -> Operación Estructura");
            nivelIndentacion++;
            operacionEstructura();
            nivelIndentacion--;
        }
        else if (lexema.equals("IF")) {
            log("<Sentencia> -> Estructura IF");
            nivelIndentacion++;
            controlFlujoIf();
            nivelIndentacion--;
        }
        else if (lexema.equals("MOSTRAR")) {
            log("<Sentencia> -> Salida");
            nivelIndentacion++;
            salida();
            nivelIndentacion--;
        }
        else if (tokenActual().getTipoToken().equals("IDENTIFICADOR")) {
            log("<Sentencia> -> Asignación");
            nivelIndentacion++;
            asignacion();
            nivelIndentacion--;
        }
        else if (lexema.equals(";")) {
            consumir(";"); 
        }
        else {
            throw error("Se esperaba sentencia válida (CREAR, IF, MOSTRAR...), se encontró: '" + lexema + "'", 203);
        }
    }

    private void declaracion() {
        consumir("CREAR");
        
        // Logueamos detalles específicos para que el reporte sea útil
        String tipo = tokenActual().getLexema().toUpperCase();
        if (!esTipoEstructura(tipo)) {
            throw error("Tipo de estructura desconocido: " + tipo, 204);
        }
        log("Tipo: " + tipo);
        consumir(tokenActual().getLexema()); // Consumir Tipo
        
        log("Nombre ID: " + tokenActual().getLexema());
        consumir("IDENTIFICADOR");
        
        if (tokenActual().getTipoToken().equals("LITERAL_NUMERICA")) {
             log("Tamaño definido: " + tokenActual().getLexema());
             consumir("LITERAL_NUMERICA");
        }
        consumir(";");
        log("✔ Declaración completada.");
    }

    private void operacionEstructura() {
        String verbo = tokenActual().getLexema().toUpperCase();
        log("Acción: " + verbo);
        consumir(verbo); 

        // Lógica de parámetros según el verbo
        if (esVerboSinParametros(verbo)) {
            if (!check("EN")) throw error("Falta 'EN' después de " + verbo, 205);
            consumir("EN");
            log("Sobre estructura: " + tokenActual().getLexema());
            consumir("IDENTIFICADOR");
        }
        else if (verbo.equals("INSERTAR_EN_POSICION")) {
            log("Param: Posición");
            expresion();
            consumir("EN");
            consumir("IDENTIFICADOR");
            consumir("CON");
            consumir("VALOR");
            log("Param: Valor");
            expresion();
        }
        else if (verbo.equals("AGREGARARISTA") || verbo.equals("CAMINOCORTO")) {
            log("Nodo Origen:");
            expresion();
            log("Nodo Destino:");
            expresion();
            consumir("EN");
            consumir("IDENTIFICADOR");
            if (check("CON") || check("PESO")) {
                 avanzar();
                 log("Con Peso:");
                 expresion();
            }
        }
        else if (verbo.equals("ELIMINAR_POSICION")) {
            log("Índice a eliminar:");
            expresion();
            consumir("EN");
            consumir("IDENTIFICADOR");
        }
        else {
            // Caso estándar (INSERTAR x EN y)
            if (!verbo.startsWith("ELIMINAR") && !verbo.startsWith("DES") && !verbo.startsWith("POP")) {
                log("Valor/Dato:");
                expresion(); 
            }
            if (!check("EN")) throw error("Falta 'EN'", 205);
            consumir("EN");
            log("En estructura: " + tokenActual().getLexema());
            consumir("IDENTIFICADOR");
        }

        consumir(";");
    }

    private void controlFlujoIf() {
        consumir("IF");
        consumir("(");
        log("Evaluando Condición...");
        condicion();
        consumir(")");
        consumir("{");
        
        log("--- Bloque VERDADERO ---");
        // Procesar sentencias dentro del bloque IF
        while (!check("}") && !esFin()) {
            try {
                sentencia();
            } catch (ParserException e) {
                errores.add(e.getMessage());
                sincronizar();
            }
        }
        consumir("}");
        log("--- Fin Bloque VERDADERO ---");

        if (match("ELSE")) {
            consumir("{");
            log("--- Bloque FALSO (Else) ---");
            while (!check("}") && !esFin()) {
                try {
                    sentencia();
                } catch (ParserException e) {
                    errores.add(e.getMessage());
                    sincronizar();
                }
            }
            consumir("}");
            log("--- Fin Bloque FALSO ---");
        }
    }

    private void salida() {
        consumir("MOSTRAR");
        log("Expresión a imprimir:");
        expresion();
        consumir(";");
    }

    private void asignacion() {
        String id = tokenActual().getLexema();
        consumir("IDENTIFICADOR");
        consumir("="); 
        log("Asignando valor a: " + id);
        expresion();
        consumir(";");
    }

    private void condicion() {
        expresion();
        String op = tokenActual().getLexema();
        if (esOperadorRelacional(op)) {
            log("Operador: " + op);
            consumir(op);
            expresion();
        } else {
            throw error("Se esperaba operador relacional, hallado: " + op, 206);
        }
    }

    // --- EXPRESIONES ARITMÉTICAS ---
    // Mantenemos esto simple en el log para no saturar con demasiados detalles matemáticos
    
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
            log("Propiedad: " + lexema);
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
        else if (tipo.equals("LITERAL_NUMERICA") || tipo.equals("LITERAL_CADENA")) {
            consumir(tipo);
        }
        else {
            throw error("Factor inválido en expresión: " + lexema, 207);
        }
    }

    // =========================================================================
    // ======================== MÉTODOS AUXILIARES CORE ========================
    // =========================================================================

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
            int codigoError = 203; 
            if (expected.equals(";")) codigoError = 201;
            else if (expected.equals("}")) codigoError = 202; 
            else if (expected.equals(")")) codigoError = 202; 
            else if (expected.equals("EN")) codigoError = 205;
            else if (expected.equals("IDENTIFICADOR")) codigoError = 208;

            throw error("Se esperaba '" + expected + "', se encontró '" + t.getLexema() + "'", codigoError);
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

    private ParserException error(String mensaje, int codigo) {
        int linea = esFin() ? tokens[tokens.length-1].getLinea() : tokenActual().getLinea();
        return new ParserException("DSL(" + codigo + ") [Línea " + linea + "]: " + mensaje);
    }

    private void sincronizar() {
        log(">> ERROR DETECTADO - RECUPERANDO MODO PÁNICO... <<");
        
        if (check(";") || check("}")) avanzar();
        
        while (!esFin()) {
            String lexema = tokenActual().getLexema();
            if (lexema.equals(";")) { avanzar(); return; }
            if (lexema.equals("}")) { return; } 
            
            String lexUpper = lexema.toUpperCase();
            if (Set.of("CREAR", "IF", "MOSTRAR", "INSERTAR", "APILAR", "ELIMINAR").contains(lexUpper)) {
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