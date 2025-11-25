/**
 * Clase para representar un Token léxico.
 */
public class Token {
    private final String tipo;
    private final String lexema;
    private final int linea;

    public Token(String tipo, String lexema, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }

    public String getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    @Override
    public String toString() {
        return "Token(" + tipo + ", '" + lexema + "', L:" + linea + ")";
    }
}