import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalizadorSintactico {
    private Token[] tokens;
    private int actual;
    private List<String> logDerivacion; 
    private List<String> errores;
    
    // VARIABLE NUEVA PARA EL ÁRBOL
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
        this.nivel = 0; // Reiniciar nivel
    }

    public List<String> getLogDerivacion() { return logDerivacion; }
    public List<String> getErrores() { return errores; }

    // --- MÉTODO PARA DAR FORMATO DE ÁRBOL ---
    private void log(String mensaje) {
        StringBuilder sb = new StringBuilder();
        // Agrega 2 espacios por cada nivel de profundidad
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

    private boolean match(String lexemaOTipo) {
        Token t = tokenActual();
        if (t.getLexema().equals(lexemaOTipo) || t.getTipoToken().equals(lexemaOTipo)) {
            log("-> Match: " + t.getLexema()); // Usa el log con indentación
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

    // --- REGLAS GRAMATICALES (Con control de nivel) ---

    // <PROGRAMA>
    private void programa() {
        log("<PROGRAMA>"); // Imprime el nombre del nodo
        nivel++;           // Baja un nivel en el árbol
        
        while (!tokenActual().getTipoToken().equals("EOF") && 
               !tokenActual().getTipoToken().equals("LLAVE_DER")) {
            try {
                sentencia();
            } catch (Exception e) {
                registrarError("Error recuperable: " + e.getMessage());
                panicMode(); 
            }
        }
        
        nivel--; // Sube un nivel al terminar la regla
    }

    // <SENTENCIA>
    private void sentencia() {
        Token t = tokenActual();
        if (t.getLexema().equals(";")) {
            actual++;
            return;
        }

        log("<SENTENCIA>"); // Nodo
        nivel++;            // Entra

        if (t.getLexema().equals("CREAR")) {
            declaracion();
        } else if (t.getTipoToken().equals("PC_IF")) {
            bloqueIf();
        } else if (t.getLexema().equals("MOSTRAR")) {
            sentenciaMostrar();
        } else if (VERBOS_DOS_VALORES.contains(t.getLexema())) {
            operacionDosValores(); 
        } else if (VERBOS_CON_VALOR.contains(t.getLexema())) {
            operacionUnValor();    
        } else if (VERBOS_SIN_VALOR.contains(t.getLexema()) || 
                   t.getTipoToken().equals("PALABRA_RESERVADA")) {
            operacionSimple();     
        } else if (t.getTipoToken().equals("LLAVE_DER") || t.getTipoToken().equals("PC_ELSE")) {
            nivel--; // Importante bajar nivel si hacemos return temprano
            return; 
        } else {
            registrarError("Sentencia no reconocida: " + t.getLexema());
            actual++; 
        }
        
        nivel--; // Sale
    }

    // <DECLARACION>
    private void declaracion() {
        try {
            log("<DECLARACION>");
            nivel++;

            match("CREAR"); 
            Token tokenTipo = tokenActual();
            if (tokenTipo.getTipoToken().equals("PALABRA_RESERVADA")) {
                match("PALABRA_RESERVADA");
            } else {
                throw new Exception("Tipo desconocido: " + tokenTipo.getLexema());
            }

            if (!match("IDENTIFICADOR")) throw new Exception("Se esperaba ID");

            if (tokenActual().getTipoToken().equals("LITERAL_NUMERICA")) {
                match("LITERAL_NUMERICA");
            } 

            if (!match("DELIMITADOR")) throw new Exception("Falta ';'");

        } catch (Exception e) {
            registrarError(e.getMessage());
            sincronizar();
        } finally {
            nivel--; // Aseguramos que siempre regrese el nivel
        }
    }

    // <BLOQUE_IF>
    private void bloqueIf() {
        log("<IF>");
        nivel++;
        
        match("PC_IF");
        if (!match("PARENTESIS_IZQ")) registrarError("Falta '('");
        condicion();
        if (!match("PARENTESIS_DER")) registrarError("Falta ')'");
        
        if (!match("LLAVE_IZQ")) registrarError("Falta '{'");
        programa(); // El programa maneja sus propios niveles
        if (!match("LLAVE_DER")) registrarError("Falta '}'");

        if (tokenActual().getTipoToken().equals("PC_ELSE")) {
            log("<ELSE>");
            nivel++;
            match("PC_ELSE");
            if (!match("LLAVE_IZQ")) registrarError("Falta '{'");
            programa();
            if (!match("LLAVE_DER")) registrarError("Falta '}'");
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
        match(tokenActual().getTipoToken()); 
        valor();
        if (!match("EN")) registrarError("Se esperaba 'EN'");
        destino();
        if (!match("DELIMITADOR")) registrarError("Falta ';'");
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
        if (!match("DELIMITADOR")) registrarError("Falta ';'");
        nivel--;
    }

    // <OP_SIMPLE>
    private void operacionSimple() {
        log("<OP_SIMPLE>");
        nivel++;
        match(tokenActual().getTipoToken()); 
        if (!match("EN")) registrarError("Se esperaba 'EN'");
        destino();
        if (!match("DELIMITADOR")) registrarError("Falta ';'");
        nivel--;
    }

    // <MOSTRAR>
    private void sentenciaMostrar() {
        log("<MOSTRAR>");
        nivel++;
        match("MOSTRAR");
        valor();
        if (!match("DELIMITADOR")) registrarError("Falta ';'");
        nivel--;
    }

    // <DESTINO>
    private void destino() {
        Token t = tokenActual();
        if (t.getTipoToken().equals("IDENTIFICADOR") || t.getTipoToken().equals("PALABRA_RESERVADA")) {
            match(t.getTipoToken()); 
        } else {
            registrarError("Destino inválido");
        }
    }

    // <VALOR>
    private void valor() {
        log("<VALOR>");
        nivel++;
        Token t = tokenActual();
        if (t.getTipoToken().equals("LITERAL_NUMERICA") || t.getTipoToken().equals("NUMERO")) {
            match(t.getTipoToken());
        } else if (t.getTipoToken().equals("IDENTIFICADOR")) {
            match("IDENTIFICADOR");
        } else if (t.getTipoToken().equals("LITERAL_CADENA") || t.getTipoToken().equals("CADENA")) {
            match(t.getTipoToken()); 
        } else if (PROPIEDADES.contains(t.getLexema())) {
            log("<PROPIEDAD>");
            nivel++;
            match(t.getTipoToken()); 
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