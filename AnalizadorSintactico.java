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
            errores.add("DSL(999) Error irrecuperable: " + e.getMessage());
        }
        logDerivacion.add("Fin del Análisis.");
    }

    public List<String> getLogDerivacion() { return logDerivacion; }
    public List<String> getErrores() { return errores; }

    // <Programa>
    private void programa() {
        while (!esFin()) {
            if (check("}")) {
                avanzar(); 
                continue; 
            }
            try {
                sentencia();
            } catch (ParserException e) {
                errores.add(e.getMessage()); // El mensaje ya trae el código DSL(id)
                sincronizar();
            }
        }
    }

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
            consumir(";"); 
        }
        else {
            throw error("Se esperaba sentencia (CREAR, IF, INSERTAR...), se encontró: '" + lexema + "'", 203);
        }
    }

    // --- MÉTODOS DE REGLAS ---

    private void declaracion() {
        logDerivacion.add("<Sentencia> -> Declaración");
        consumir("CREAR");
        
        if (!esTipoEstructura(tokenActual().getLexema().toUpperCase())) {
            throw error("Tipo de estructura desconocido: " + tokenActual().getLexema(), 204);
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

        if (esVerboSinParametros(verbo)) {
            if (!check("EN")) throw error("Falta la palabra reservada 'EN' después de " + verbo, 205);
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
            // Caso estándar
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

    private void controlFlujoIf() {
        logDerivacion.add("<Sentencia> -> Control IF");
        consumir("IF");
        consumir("(");
        condicion();
        consumir(")");
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
    // ======================== MÉTODOS AUXILIARES ACTUALIZADOS ================
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
            // AUTODETECCIÓN DE CÓDIGO DE ERROR SEGÚN LO QUE FALTÓ
            int codigoError = 203; // Genérico
            
            if (expected.equals(";")) codigoError = 201;      // Falta punto y coma
            else if (expected.equals("}")) codigoError = 202; // Falta cierre de bloque
            else if (expected.equals(")")) codigoError = 202; // Falta paréntesis
            else if (expected.equals("EN")) codigoError = 205;// Falta palabra reservada EN
            else if (expected.equals("IDENTIFICADOR")) codigoError = 208; // Falta ID

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

    // MÉTODO ERROR SOBRECARGADO PARA INCLUIR CÓDIGO DSL(id)
    private ParserException error(String mensaje, int codigo) {
        int linea = esFin() ? tokens[tokens.length-1].getLinea() : tokenActual().getLinea();
        // Formato: "DSL(201) [Línea 5]: Mensaje..."
        return new ParserException("DSL(" + codigo + ") [Línea " + linea + "]: " + mensaje);
    }

    private void sincronizar() {
        logDerivacion.add("   >> RECUPERANDO... (Buscando ';' o cierre de bloque)");
        if (check(";") || check("}")) avanzar();
        while (!esFin()) {
            String lexema = tokenActual().getLexema();
            if (lexema.equals(";")) { avanzar(); return; }
            if (lexema.equals("}")) { return; }
            
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