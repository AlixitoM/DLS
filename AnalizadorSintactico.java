import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalizadorSintactico {
    private Token[] tokens;
    private int actual;
    private List<String> logDerivacion; 
    private List<String> errores;
    
    private int nivel = 0; 

    // --- CONJUNTOS DE VALIDACIÓN ---
    private static final Set<String> VERBOS_CON_VALOR = Set.of(
        "INSERTAR", "APILAR", "ENCOLAR", "PUSH", "ENQUEUE", "AGREGARNODO", 
        "INSERTAR_INICIO", "INSERTAR_FINAL", "BUSCAR", "CLAVE", 
        "INSERTARIZQUIERDA", "INSERTARDERECHA", "BFS", "DFS"
    );
    
    private static final Set<String> VERBOS_DOS_VALORES = Set.of(
        "AGREGARARISTA", "CAMINOCORTO", "ACTUALIZAR", "INSERTAR_EN_POSICION"
    );

    private static final Set<String> VERBOS_SIN_VALOR = Set.of(
        "ELIMINAR", "ELIMINAR_INICIO", "ELIMINAR_FINAL", "ELIMINAR_FRENTE", 
        "ELIMINARNODO", "DESAPILAR", "POP", "DESENCOLAR", "DEQUEUE", 
        "RECORRER", "RECORRERADELANTE", "RECORRERATRAS", 
        "PREORDEN", "INORDEN", "POSTORDEN", "RECORRIDOPORNIVELES",
        "REHASH", "ELIMINARARISTA"
    );

    private static final Set<String> PROPIEDADES = Set.of(
         "TAMANO", "VACIAT", "LLENAT", "TOPE", "FRENTE", 
        "ALTURA", "NODOS", "HOJAS", "PEEK", "FRONT", "VERFILA", "VECINOS"
    );

    public AnalizadorSintactico(Token[] tokens) {
        this.tokens = tokens;
        this.actual = 0;
        this.logDerivacion = new ArrayList<>();
        this.errores = new ArrayList<>();
        this.nivel = 0; 
    }

    public List<String> getLogDerivacion() { return logDerivacion; }
    public List<String> getErrores() { return errores; }

    private void log(String mensaje) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nivel; i++) {
            sb.append("|  "); 
        }
        sb.append(mensaje);
        logDerivacion.add(sb.toString());
    }

    public void analizar() {
        log("INICIO DEL ANÁLISIS (Raíz)");
        try {
            programa();
        } catch (Exception e) {
            registrarError("Error fatal: " + e.getMessage());
        }
        
        if (errores.isEmpty()) {
            log("FIN: Análisis exitoso.");
        } else {
            log("FIN: Análisis con " + errores.size() + " errores.");
        }
    }

    // --- FUNCIONES DE APOYO ---
    private Token tokenActual() {
        if (actual >= tokens.length) {
            return new Token("EOF", -1, -1, "EOF", "N/A", false); 
        }
        return tokens[actual];
    }

    // --- CAMBIO PRINCIPAL 1: Match insensible a mayúsculas ---
    private boolean match(String lexemaOTipo) {
        Token t = tokenActual();
        // Usamos equalsIgnoreCase para el lexema (ej: "en" == "EN")
        // Pero mantenemos equals para el TipoToken (ej: "IDENTIFICADOR")
        if (t.getLexema().equalsIgnoreCase(lexemaOTipo) || t.getTipoToken().equals(lexemaOTipo)) {
            log("-> Match: " + t.getLexema()); 
            actual++;
            return true;
        }
        return false;
    }

    private void sincronizar() {
        log("!!! RECUPERANDO ERROR (Panic Mode) !!!");
        Token t = tokenActual();
        while (!t.getTipoToken().equals("EOF") && !t.getLexema().equals(";")) {
            actual++;
            t = tokenActual();
        }
        if (t.getLexema().equals(";")) {
            actual++;
        }
    }

    // --- REGLAS GRAMATICALES ---

    // <PROGRAMA>
    private void programa() {
        log("<PROGRAMA>"); 
        nivel++;          
        
        while (!tokenActual().getTipoToken().equals("EOF") && 
               !tokenActual().getTipoToken().equals("LLAVE_DER")) {
            try {
                sentencia();
            } catch (Exception e) {
                registrarError("Error recuperable: " + e.getMessage());
                panicMode(); 
            }
        }
        
        nivel--; 
    }

    // <SENTENCIA>
    private void sentencia() {
        Token t = tokenActual();
        if (t.getLexema().equals(";")) {
            actual++;
            return;
        }

        log("<SENTENCIA>"); 
        nivel++;            

        // --- CAMBIO PRINCIPAL 2: .toUpperCase() antes de buscar en los Sets ---
        // Esto asegura que si el token es "apilar", al convertirlo a "APILAR" lo encuentre en el Set.
        String lexemaUpper = t.getLexema().toUpperCase();

        if (lexemaUpper.equals("CREAR")) {
            declaracion();
        } else if (t.getTipoToken().equals("PC_IF") || lexemaUpper.equals("IF")) {
            bloqueIf();
        } else if (lexemaUpper.equals("MOSTRAR")) {
            sentenciaMostrar();
        } else if (VERBOS_DOS_VALORES.contains(lexemaUpper)) {
            operacionDosValores(); 
        } else if (VERBOS_CON_VALOR.contains(lexemaUpper)) {
            operacionUnValor();    
        } else if (VERBOS_SIN_VALOR.contains(lexemaUpper) || 
                   t.getTipoToken().equals("PALABRA_RESERVADA")) {
            operacionSimple();     
        } else if (t.getTipoToken().equals("LLAVE_DER") || t.getTipoToken().equals("PC_ELSE")) {
            nivel--; 
            return; 
        } else {
            registrarError("Sentencia no reconocida: " + t.getLexema());
            actual++; 
        }
        
        nivel--; 
    }

    // <DECLARACION>
    private void declaracion() {
        try {
            log("<DECLARACION>");
            nivel++;

            match("CREAR"); 
            Token tokenTipo = tokenActual();
            // Validamos contra el Set de Estructuras si fuera necesario, o confiamos en el tipo
            if (tokenTipo.getTipoToken().equals("PALABRA_RESERVADA") || 
                tokenTipo.getTipoToken().equals("ESTRUCTURA")) { // Ajuste por si usas otro tipo
                // Avanzamos, no importa si match usa equalsIgnoreCase, funcionará
                actual++; 
                log("-> Tipo estructura: " + tokenTipo.getLexema());
            } else {
                throw new Exception("Tipo desconocido: " + tokenTipo.getLexema());
            }

            if (!match("IDENTIFICADOR")) throw new Exception("Se esperaba ID");

            if (tokenActual().getTipoToken().equals("LITERAL_NUMERICA")) {
                match("LITERAL_NUMERICA");
            } 

            if (!match("DELIMITADOR") && !match(";")) throw new Exception("Falta ';'");

        } catch (Exception e) {
            registrarError(e.getMessage());
            sincronizar();
        } finally {
            nivel--; 
        }
    }

    // <BLOQUE_IF>
    private void bloqueIf() {
        log("<IF>");
        nivel++;
        
        // Match acepta "if", "IF", "If" gracias al cambio en match()
        if(!match("PC_IF")) match("IF"); 
        
        if (!match("PARENTESIS_IZQ") && !match("(")) registrarError("Falta '('");
        condicion();
        if (!match("PARENTESIS_DER") && !match(")")) registrarError("Falta ')'");
        
        if (!match("LLAVE_IZQ") && !match("{")) registrarError("Falta '{'");
        programa(); 
        if (!match("LLAVE_DER") && !match("}")) registrarError("Falta '}'");

        Token t = tokenActual();
        if (t.getTipoToken().equals("PC_ELSE") || t.getLexema().equalsIgnoreCase("ELSE")) {
            log("<ELSE>");
            nivel++;
            match("ELSE"); // Funcionará con "else"
            if (!match("LLAVE_IZQ") && !match("{")) registrarError("Falta '{'");
            programa();
            if (!match("LLAVE_DER") && !match("}")) registrarError("Falta '}'");
            nivel--;
        }
        
        nivel--;
    }

    // <CONDICION>
    private void condicion() {
        log("<CONDICION>");
        nivel++;
        valor(); 
        Token t = tokenActual();
        if (t.getLexema().matches("==|!=|<|>|<=|>=")) {
            match(t.getLexema()); 
            valor();            
        } 
        nivel--;
    }

    // <OP_UN_VALOR>
    private void operacionUnValor() {
        log("<OP_UN_VALOR>");
        nivel++;
        // match avanzará aunque sea "apilar" porque lo encuentra por equalsIgnoreCase o por TipoToken
        match(tokenActual().getTipoToken()); 
        valor();
        if (!match("EN")) registrarError("Se esperaba 'EN'");
        destino();
        if (!match("DELIMITADOR") && !match(";")) registrarError("Falta ';'");
        nivel--;
    }

    // <OP_DOS_VALORES>
    private void operacionDosValores() {
        log("<OP_DOS_VALORES>");
        nivel++;
        match(tokenActual().getTipoToken()); 
        valor(); 
        if (!match("CON")) registrarError("Se esperaba 'CON'");
        valor(); 
        if (!match("EN")) registrarError("Se esperaba 'EN'");
        destino();
        if (!match("DELIMITADOR") && !match(";")) registrarError("Falta ';'");
        nivel--;
    }

    // <OP_SIMPLE>
    private void operacionSimple() {
        log("<OP_SIMPLE>");
        nivel++;
        match(tokenActual().getTipoToken()); 
        if (!match("EN")) registrarError("Se esperaba 'EN'");
        destino();
        if (!match("DELIMITADOR") && !match(";")) registrarError("Falta ';'");
        nivel--;
    }

    // <MOSTRAR>
    private void sentenciaMostrar() {
        log("<MOSTRAR>");
        nivel++;
        match("MOSTRAR");
        valor();
        if (!match("DELIMITADOR") && !match(";")) registrarError("Falta ';'");
        nivel--;
    }

    // <DESTINO>
    private void destino() {
        Token t = tokenActual();
        if (t.getTipoToken().equals("IDENTIFICADOR") || t.getTipoToken().equals("PALABRA_RESERVADA")) {
            // Avanzamos manualmente para evitar conflictos de match
            log("-> Destino: " + t.getLexema());
            actual++;
        } else {
            registrarError("Destino inválido: " + t.getLexema());
            actual++; // Forzar avance para no ciclar
        }
    }

    // <VALOR>
    private void valor() {
        log("<VALOR>");
        nivel++;
        Token t = tokenActual();
        
        // --- CAMBIO: toUpperCase para buscar propiedades ---
        String lexemaUpper = t.getLexema().toUpperCase();

        if (t.getTipoToken().equals("LITERAL_NUMERICA") || t.getTipoToken().equals("NUMERO")) {
            match(t.getTipoToken());
        } else if (t.getTipoToken().equals("IDENTIFICADOR")) {
            match("IDENTIFICADOR");
        } else if (t.getTipoToken().equals("LITERAL_CADENA") || t.getTipoToken().equals("CADENA")) {
            match(t.getTipoToken()); 
        } else if (PROPIEDADES.contains(lexemaUpper)) {
            log("<PROPIEDAD>");
            nivel++;
            // Como ya sabemos que está en el Set, avanzamos
            actual++;
            log("-> Propiedad: " + t.getLexema());

            if (!match("EN")) registrarError("Falta 'EN'");
            destino();
            nivel--;
        } else {
            registrarError("Valor inválido: " + t.getLexema());
            actual++; 
        }
        nivel--;
    }

    private void registrarError(String msg) {
        Token t = tokenActual();
        errores.add("Error Sintáctico [Línea " + t.getLinea() + "]: " + msg);
    }

    private void panicMode() {
        log("!!! PANIC MODE !!!");
        while (actual < tokens.length) {
            String lex = tokenActual().getLexema();
            if (lex.equals(";") || lex.equals("}")) {
                if(lex.equals(";")) actual++; 
                return;
            }
            actual++;
        }
    }
}