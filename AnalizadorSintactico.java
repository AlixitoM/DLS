import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class AnalizadorSintactico {
    private final Token[] tokens;
    private int actual;
    private final List<String> logDerivacion;
    private final List<String> errores;
    
    private int nivelIndentacion = 0;

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

   
    private void arbol(String mensaje) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nivelIndentacion; i++) {
            sb.append("|   ");
        }
        sb.append(mensaje);
        logDerivacion.add(sb.toString());
    }

    /*
      Punto de entrada principal del análisis.
      Inicia la derivación desde el símbolo inicial <Programa>.
     */
    public void analizar() {
        logDerivacion.clear();
        arbol("INICIO DEL ANÁLISIS SINTÁCTICO");
        try {
            programa();
        } catch (Exception e) {
            errores.add("DSL(999) Error irrecuperable: " + e.getMessage());
        }
        arbol("FIN DEL ANÁLISIS");
    }

    public List<String> getArbolDerivacion() { return logDerivacion; }
    public List<String> getErrores() { return errores; }

    // -------------------------------------------------------------------------
    // --- REGLAS DE PRODUCCIÓN (GRAMÁTICA) ------------------------------------
    // -------------------------------------------------------------------------

    // esta es la secuencia principal del ocmpilador a partir de estas salen todas las demas 
    private void programa() {
        nivelIndentacion++;
        while (!esFin()) {
          
            if (checar("}")) {
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
        nivelIndentacion--;
    }

    //  una vez se inicia el programa se inicia con la sentencia el cual seria nuestro primer 'no terminal'
    private void sentencia() {
        if (esFin()) return;

        String lexema = tokenActual().getLexema().toUpperCase();

        // se valida la clasificacion de los posibles iniciadores de sentencia 
        
        if (lexema.equals("CREAR")) {
            arbol("<Sentencia> -> Declaración");
            nivelIndentacion++;
            declaracion();
            nivelIndentacion--;
        } 
        else if (esVerboOperacion(lexema)) {
            arbol("<Sentencia> -> Operación Estructura");
            nivelIndentacion++;
            operacionEstructura();
            nivelIndentacion--;
        }
        else if (lexema.equals("IF")) {
            arbol("<Sentencia> -> Estructura IF");
            nivelIndentacion++;
            flujoIf();
            nivelIndentacion--;
        }
        else if (lexema.equals("MOSTRAR")) {
            arbol("<Sentencia> -> Salida");
            nivelIndentacion++;
            salida();
            nivelIndentacion--;
        }
        else if (tokenActual().getTipoToken().equals("IDENTIFICADOR")) {
            arbol("<Sentencia> -> Asignación");
            nivelIndentacion++;
            asignacion();
            nivelIndentacion--;
        }
        else if (lexema.equals(";")) {
            consumir(";");
        }
        // si no hay ninguno se manda mensaje de error 
        else {
            throw error("Se esperaba sentencia válida (CREAR, IF, MOSTRAR...), se encontró: '" + lexema + "'", 203);
        }
    }

    /*
      <Declaracion> ::= CREAR <Tipo> IDENTIFICADOR [LITERAL_NUMERICA] ;
     */
    private void declaracion() {
        consumir("CREAR");
        
        String tipo = tokenActual().getLexema().toUpperCase();
        if (!esTipoEstructura(tipo)) {
            throw error("Tipo de estructura desconocido: " + tipo, 204);
        }
        
        arbol("Tipo: " + tipo);
        consumir(tokenActual().getLexema()); 
        
        arbol("Nombre ID: " + tokenActual().getLexema());
        consumir("IDENTIFICADOR");
        
        // Tamaño opcional para estructuras estáticas
        if (tokenActual().getTipoToken().equals("LITERAL_NUMERICA")) {
             arbol("Tamaño definido: " + tokenActual().getLexema());
             consumir("LITERAL_NUMERICA");
        }
        consumir(";");
        arbol(" Declaración completada.");
    }

    /*
      <Operacion> ::= VERBO [Parametros] EN IDENTIFICADOR ;
      Maneja la sintaxis variable de los verbos (con 0, 1 o más parámetros).
     */
    private void operacionEstructura() {
        String verbo = tokenActual().getLexema().toUpperCase();
        arbol("Acción: " + verbo);
        consumir(verbo);

        // 1. Verbos sin parámetros explícitos (ej. ELIMINAR, RECORRER)
        if (esVerboSinParametros(verbo)) {
            if (!checar("EN")) throw error("Falta 'EN' después de " + verbo, 205);
            consumir("EN");
            arbol("Sobre estructura: " + tokenActual().getLexema());
            consumir("IDENTIFICADOR");
        }
        // 2. Operaciones complejas específicas
        else if (verbo.equals("INSERTAR_EN_POSICION")) {
            arbol("Param: Posición");
            expresion();
            consumir("EN");
            consumir("IDENTIFICADOR");
            consumir("CON");
            consumir("VALOR");
            arbol("Param: Valor");
            expresion();
        }
        else if (verbo.equals("AGREGARARISTA") || verbo.equals("CAMINOCORTO")) {
            arbol("Nodo Origen:");
            expresion();
            arbol("Nodo Destino:");
            expresion();
            consumir("EN");
            consumir("IDENTIFICADOR");
            if (checar("CON") || checar("PESO")) {
                 avanzar();
                 arbol("Con Peso:");
                 expresion();
            }
        }
        else if (verbo.equals("ELIMINAR_POSICION")) {
            arbol("Índice a eliminar:");
            expresion();
            consumir("EN");
            consumir("IDENTIFICADOR");
        }
        // 3. Caso estándar (Un parámetro + EN + ID)
        else {
            if (!verbo.startsWith("ELIMINAR") && !verbo.startsWith("DES") && !verbo.startsWith("POP")) {
                arbol("Valor/Dato:");
                expresion();
            }
            if (!checar("EN")) throw error("Falta 'EN'", 205);
            consumir("EN");
            arbol("En estructura: " + tokenActual().getLexema());
            consumir("IDENTIFICADOR");
        }

        consumir(";");
    }

    
    private void flujoIf() {
        consumir("IF");
        consumir("(");
        arbol("Evaluando Condición...");
        condicion();
        consumir(")");
        consumir("{");
        
        arbol("--- Bloque VERDADERO ---");
        while (!checar("}") && !esFin()) {
            try {
                sentencia();
            } catch (ParserException e) {
                errores.add(e.getMessage());
                sincronizar();
            }
        }
        consumir("}");
        arbol("--- Fin Bloque VERDADERO ---");

        if (coincide("ELSE")) {
            consumir("{");
            arbol("--- Bloque FALSO (Else) ---");
            while (!checar("}") && !esFin()) {
                try {
                    sentencia();
                } catch (ParserException e) {
                    errores.add(e.getMessage());
                    sincronizar();
                }
            }
            consumir("}");
            arbol("--- Fin Bloque FALSO ---");
        }
    }

    private void salida() {
        consumir("MOSTRAR");
        arbol("Expresión a imprimir:");
        expresion();
        consumir(";");
    }

    private void asignacion() {
        String id = tokenActual().getLexema();
        consumir("IDENTIFICADOR");
        consumir("=");
        arbol("Asignando valor a: " + id);
        expresion();
        consumir(";");
    }

    private void condicion() {
        expresion();
        String op = tokenActual().getLexema();
        if (esOperadorRelacional(op)) {
            arbol("Operador: " + op);
            consumir(op);
            expresion();
        } else {
            throw error("Se esperaba operador relacional, hallado: " + op, 206);
        }
    }

    private void expresion() {
        termino();
        while (checar("+") || checar("-")) {
            consumir(tokenActual().getLexema());
            termino();
        }
    }

    private void termino() {
        factor();
        while (checar("*") || checar("/")) {
            consumir(tokenActual().getLexema());
            factor();
        }
    }

    private void factor() {
        String lexema = tokenActual().getLexema().toUpperCase();
        String tipo = tokenActual().getTipoToken();

        if (esPropiedad(lexema)) {
            arbol("Propiedad: " + lexema);
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

    private void consumir(String expected) {
        if (esFin()) {
            throw error("Final inesperado. Se esperaba: " + expected, 299);
        }

        Token t = tokenActual();
        String lexema = t.getLexema().toUpperCase();
        String tipo = t.getTipoToken();

        boolean match = lexema.equals(expected.toUpperCase()) || tipo.equals(expected);

        if (match) {
            avanzar();
        } else {
            // Mapeo de códigos de error según el tipo esperado
            int codigoError = 203; 
            if (expected.equals(";")) codigoError = 201;
            else if (expected.equals("}") || expected.equals(")")) codigoError = 202; 
            else if (expected.equals("EN")) codigoError = 205;
            else if (expected.equals("IDENTIFICADOR")) codigoError = 208;

            throw error("Se esperaba '" + expected + "', se encontró '" + t.getLexema() + "'", codigoError);
        }
    }

    
    // valida que el token recibido este clasificado y sea lo que nescesitamos 
    private boolean checar(String token) {
        if (esFin()) return false;
        Token t = tokenActual();
        return t.getLexema().equalsIgnoreCase(token) || t.getTipoToken().equals(token);
    }
    
    
    private boolean coincide(String expected) {
        if (checar(expected)) {
            avanzar();
            return true;
        }
        return false;
    }

    private void sincronizar() {
        arbol(">> ERROR DETECTADO - RECUPERANDO MODO PÁNICO... <<");
        
        if (checar(";") || checar("}")) avanzar();
        
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

    // -------------------------------------------------------------------------
    // --- CONJUNTOS DE VALIDACIÓN RÁPIDA (LOOKUPS) ----------------------------
    // -------------------------------------------------------------------------

    private boolean esTipoEstructura(String s) {
        return Set.of("PILA", "COLA", "BICOLA", "LISTA_ENLAZADA", "LISTA_CIRCULAR","LISTA_DOBLE_ENLAZADA",
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
            "PREORDEN", "INORDEN", "POSTORDEN", "RECORRIDOPORNIVELES", "VACIA"
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
