/**
 * Clase que representa un token léxico clasificado.
 */
public class Token {
    private String lexema;
    private int linea;
    private String tipoToken; // Tipo léxico (e.g., PALABRA_RESERVADA, LITERAL_NUMERICA)
    private String estadoFinal; // Estado final del AFD (solo relevante para Palabras Reservadas)
    private boolean existeSimbolo; // Indica si el lexema fue reconocido (true) o si es un error (false)

    // Constructor usado por el tokenizador (solo obtiene lexema y línea)
    public Token(String lexema, int linea) {
        this.lexema = lexema;
        this.linea = linea;
        this.tipoToken = "Pendiente"; // Valor inicial
        this.estadoFinal = "N/A";
        this.existeSimbolo = false;
    }
    
    // Constructor usado por el AFD para clasificar el token
    public Token(String lexema, int linea, String tipoToken, String estadoFinal, boolean existeSimbolo) {
        this.lexema = lexema;
        this.linea = linea;
        this.tipoToken = tipoToken;
        this.estadoFinal = estadoFinal;
        this.existeSimbolo = existeSimbolo;
    }

    // Getters
    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public String getEstadoFinal() {
        return estadoFinal;
    }

    public boolean existeSimbolo() {
        return existeSimbolo;
    }

    @Override
    public String toString() {
        return String.format("Token(%-18s, '%-20s', L:%d, E:%-10s, Reconocido: %s)",
            tipoToken, lexema, linea, estadoFinal, existeSimbolo ? "Sí" : "No");
    }
}