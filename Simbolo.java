/**
 * Clase para representar un Símbolo en la Tabla de Símbolos.
 */
public class Simbolo {
    private final String nombre;
    private final String tipoEstructura;
    private final int lineaDeclaracion;

    public Simbolo(String nombre, String tipoEstructura, int linea) {
        this.nombre = nombre;
        this.tipoEstructura = tipoEstructura;
        this.lineaDeclaracion = linea;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoEstructura() {
        return tipoEstructura;
    }

    @Override
    public String toString() {
        return "Simbolo(Nombre: " + nombre + ", Tipo: " + tipoEstructura + ", Linea: " + lineaDeclaracion + ")";
    }
}