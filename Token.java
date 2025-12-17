

/**
 * Clase que representa un token léxico clasificado.*/

public class Token {

    private String lexema;
    private int linea;
    private String tipoToken; 
    private String estadoFinal; 
    private boolean existeSimbolo; 
    private int columna; 

   public Token(String lexema, int linea, int columna) {
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.tipoToken = "Pendiente";
        this.estadoFinal = "N/A";
        this.existeSimbolo = false;
    }
    
    public Token(String lexema, int linea, int columna, String tipoToken, String estadoFinal, boolean existeSimbolo) {
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.tipoToken = tipoToken;
        this.estadoFinal = estadoFinal;
        this.existeSimbolo = existeSimbolo;
    }

    // Getters
    public String getLexema() { return lexema; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    public String getTipoToken() { return tipoToken; }
    public String getEstadoFinal() { return estadoFinal; }
    public boolean existeSimbolo() { return existeSimbolo; }
}
 