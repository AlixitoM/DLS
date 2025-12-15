

/**
 * Clase que representa un token léxico clasificado.*/

public class Token {
   // Atributos que tendra el token
    private String lexema;// codigo 'crudo'
    private int linea;// linea del codigo que se encuentra el 
    private String tipoToken; // Tipo  del token palabra reservada, operador etc etc 
    private String estadoFinal; // estado final del afd en el que se encuentra
    private boolean existeSimbolo; // Indica si el lexema fue reconocido (true) o si es un error (false)
    private int columna; // columna en la que se acaba el token

    // Constructor usado por el tokenizador (solo obtiene lexema y línea)
   public Token(String lexema, int linea, int columna) {
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.tipoToken = "Pendiente";
        this.estadoFinal = "N/A";
        this.existeSimbolo = false;
    }
    
    // Constructor usado por el AFD para clasificar el token
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
 
