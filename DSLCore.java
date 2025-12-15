// importamos la libreira java.util ya que nescesitaremos hash maps array list y sets 

import java.util.*;

/*ESTA CLASE IMPLEMENTA LOS SIGUIENTES METODOS         
 getEstadosAceptacionDSL retorna el conjunto estados de aceptación del automata




 */
public class DSLCore {

    /*
    // En este metodo creamos un set, el cual es un hash map que no puede tener elementos repetidos y a la vez 
    //tiene una gran utilidad  para las busquedas ya que tiene tiempos de busquedas rapidos.
    A este set  usamos el metodo Set.of el cual nos dará un valor estatico, estos valores serán nuestras palabrs reservadas 
    las cuales  serán nuestros estados de aceptacion en el automta 
    
    
     */
    public static Set<String> getEstadosAceptacionDSL() {
        return Set.of(
                "PILA", "PILA_CIRCULAR", "COLA", "BICOLA", "LISTA_ENLAZADA", "LISTA_DOBLE_ENLAZADA", "LISTA_CIRCULAR", "ARBOL_BINARIO", "TABLA_HASH", "GRAFO",
                "INSERTAR", "INSERTAR_FINAL", "INSERTAR_INICIO", "INSERTAR_EN_POSICION", "INSERTARIZQUIERDA", "INSERTARDERECHA", "AGREGARNODO", "APILAR", "ENCOLAR", "PUSH", "ENQUEUE",
                "ELIMINAR", "ELIMINAR_INICIO", "ELIMINAR_FINAL", "ELIMINAR_FRENTE", "ELIMINAR_POSICION", "ELIMINARNODO", "DESAPILAR", "POP", "DESENCOLAR", "DEQUEUE",
                "BUSCAR", "TOPE", "FRENTE", "PEEK", "VERFILA", "FRONT", "CLAVE",
                "RECORRER", "RECORRERADELANTE", "RECORRERATRAS", "PREORDEN", "INORDEN", "POSTORDEN", "RECORRIDOPORNIVELES",
                "ACTUALIZAR", "REHASH", "AGREGARARISTA", "ELIMINARARISTA", "VECINOS", "BFS", "DFS", "CAMINOCORTO",
                "VACIAT", "LLENAT", "TAMANO", "ALTURA", "HOJAS", "NODOS",
                "EN", "CON", "VALOR", "CREAR",
                "MOSTRAR", "IF", "ELSE"
        );
    }


    /*

    
    Este metodo  es el encargado de definir todos los estados , al igual lo hace con un set<>,
    El cual contendra los nombres de todos los estados que llevan a una palabra reservada
    
    El metodo se divide en 4 partes se declara un estado set <> el cual contendra todos los estados 
    a este metodo se le agrega el estado de inicio 
    se hace un for each que recorre todos los estados disponibles en los estados de aceptacion
    dentro de este for each hay un for anidado el cual agrega al set llamado todosLosEstados
    un nuevo estado que sera llamado igual que el estado que esta iterando -1 posicion 
    por ejemplo: 
    SE empieza con el estado de aceptacion PILA 
    Se agrega el estado PIL al set 
    luego el estado PI y asi sucesivamente hasta que se llega a una cadena de longitud 1 en este caso P
    en caso de que multiples estados empiecen en P el set los ignorará y solo agregará uno haciendo que tenga los menos estados posibles
    se repetirá hasta que no haya mas estados en el set de estados finales 
    
    
     */
    private static Set<String> getEstadosDSL() {
        Set<String> todosLosEstados = new HashSet<>();
        todosLosEstados.add("INICIO");

        for (String pr : getEstadosAceptacionDSL()) {
            for (int i = 1; i <= pr.length(); i++) {
                todosLosEstados.add(pr.substring(0, i));
            }
        }
        return todosLosEstados;
    }

    /*
    Este metodo hace un poco de lo mismo solo que un poco más complejo
    Este estado retorna un Mapa que tiene como identificador un caracter
    El mapa contiene otro mapa lo cual nos hace tener tres campos los cuales simularän nuestras transiciones 
    siendo el primer string el nombre del estado el character el caracter con el que va y el segundo string al estado al que va 
    
     */
    private static Map<String, Map<Character, String>> getTransicionesDSL() {
        Map<String, Map<Character, String>> transiciones = new HashMap<>();

        // fpr para rellenar el hash transiciones el campo estados obteniendo la informacion del hash pasado  
        for (String estado : getEstadosDSL()) {
            transiciones.put(estado, new HashMap<>());
        }

        /* para rellenar el los campos de caracter y estado destino se hace un for anidado se rellenará las tarnsiciones
        1.- se obtiene el primer caracter del estado de aceptacion 
        2.- se define su estadodestino como pr.substring (i+1)  
        3.- se define su estado de origen como pr.substring (0+i)
         
        para  que se aclare más usaremos como ejemplo la palabra pila
        obtenemos pila 
        obtenemos el character en la posicion i en este caso 0 que seria p 
        el estado de destino seria pi
        y se valida que el estado de inicio sea 0 , para que no sea el estado de incio y en caso de que no lo sea se obtiene 
        el substring de 0 hasta i en este caso p
         */
        for (String pr : getEstadosAceptacionDSL()) {
            for (int i = 0; i < pr.length(); i++) {
                char simbolo = pr.charAt(i);
                String estadoDestino = pr.substring(0, i + 1);
                String origen = (i == 0) ? "INICIO" : pr.substring(0, i);

                transiciones.get(origen).put(simbolo, estadoDestino);
            }
        }
        //Retorna el estado de transiciones 
        return transiciones;
    }

    
    // ahora hacemos el set con los alfabetos 
    private static Set<Character> getAlfabetoDSL() {
        Set<Character> alfabeto = new HashSet<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            alfabeto.add(c);
        }
        for (char c = '0'; c <= '9'; c++) {
            alfabeto.add(c);
        }
        alfabeto.add('_');
        return alfabeto;
    }
/*
 Este metodo retorna un array de cadenas los cuales seran las palabras en cada celda sin espacios    
    
*/
    public static String[] tokenizarLinea(String entrada) {
        /*
        
        Primero obtiene 
        */
        int indiceComentario = entrada.indexOf("//");
        if (indiceComentario != -1) {
            entrada = entrada.substring(0, indiceComentario);
        }

        String tokenizada = entrada.trim().replaceAll("\\s+", " ");

        tokenizada = tokenizada.replaceAll("(==|!=|<=|>=|&&|\\|\\||[\\Q(){}[]|,;=+-*/<>\u0021&|.\\E])", " $1 ");

        tokenizada = tokenizada.trim().replaceAll("\\s+", " ");

        if (tokenizada.isEmpty()) {
            return new String[0];
        }

        return tokenizada.split(" ");
    }

    public static Token[] tokenizador(String entrada) {
        List<Token> listaTokens = new ArrayList<>();

        String regex = "(//.*)|"
                + "(\"[^\"]*\")|"
                + "(==|!=|<=|>=|&&|\\|\\|)|"
                + "([\\Q(){}[]|,;=+-*/<>\u0021&|.\\E])|" 
                + "([^\\s\\Q(){}[]|,;=+-*/<>\u0021&|.\\E\"]+)";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);

        String[] lineas = entrada.split("\n");
        int numLinea = 1;

        for (String lineaOriginal : lineas) {
            java.util.regex.Matcher matcher = pattern.matcher(lineaOriginal);

            while (matcher.find()) {
                String token = matcher.group();

           
                if (matcher.group(1) != null) {
                    continue;
                }

                if (token.trim().isEmpty()) {
                    continue;
                }

                int columna = matcher.start() + 1;

                listaTokens.add(new Token(token, numLinea, columna));
            }
            numLinea++;
        }
        return listaTokens.toArray(new Token[0]);
    }

    public static AFD obtenerInstanciaAFD() {
        return new AFD(
                getEstadosDSL(),
                getAlfabetoDSL(),
                getTransicionesDSL(),
                "INICIO",
                getEstadosAceptacionDSL()
        );
    }

}
