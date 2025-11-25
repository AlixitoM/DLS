# dsl_lexer.py

### CLASES DE ESTRUCTURA DE DATOS ###

class Token:
    def __init__(self, tipo, lexema, linea):
        self.tipo = tipo
        self.lexema = lexema
        self.linea = linea

    def __repr__(self):
        return f"Token({self.tipo}, '{self.lexema}', L:{self.linea})"

class Simbolo:
    def __init__(self, nombre, tipo_estructura, linea):
        self.nombre = nombre
        self.tipo_estructura = tipo_estructura
        self.linea_declaracion = linea

    def __repr__(self):
        return f"Simbolo(Nombre: {self.nombre}, Tipo: {self.tipo_estructura}, Linea: {self.linea_declaracion})"

### CONSTANTES DE TIPOS DE TOKENS ###

# Palabras Clave de Control
T_CREAR = 'PC_CREAR'
T_EN = 'PREP_EN'
T_PUNTO_COMA = 'DELIMITADOR'

# Palabras Clave de Estructuras (PC_ESTRUCTURA) - Basado en tu imagen
T_PILA = 'PILA'
T_COLA = 'COLA'
T_LISTA_SIMPLE = 'LISTA_SIMPLE'
T_ARBOL = 'ARBOL'
T_GRAFO = 'GRAFO'
# ... (otras estructuras)

# Palabras Clave de Acciones (PC_ACCION) - Basado en tu imagen
T_INSERTAR = 'ACCION_INSERTAR'
T_POP = 'ACCION_POP'
T_ENQUEUE = 'ACCION_ENQUEUE'
T_BUSCAR = 'ACCION_BUSCAR'
T_RECORRER = 'ACCION_RECORRER'
T_AGREGAR = 'ACCION_AGREGAR'
# ... (otras acciones)

# Tokens Genéricos
T_ID = 'IDENTIFICADOR'
T_LIT_NUMERO = 'LITERAL_NUMERICO'
T_ERROR = 'ERROR_LEXICO'
T_EOF = 'FIN_DE_ARCHIVO'

# Diccionario de Palabras Clave para búsqueda rápida
PALABRAS_CLAVE = {
    'CREAR': T_CREAR, 'EN': T_EN,
    'PILA': T_PILA, 'COLA': T_COLA, 'ARBOL': T_ARBOL, 'LISTA_SIMPLE': T_LISTA_SIMPLE, 'GRAFO': T_GRAFO,
    'INSERTAR': T_INSERTAR, 'POP': T_POP, 'ENQUEUE': T_ENQUEUE, 'BUSCAR': T_BUSCAR, 'RECORRER': T_RECORRER,
    'AGREGAR': T_AGREGAR
}

class AnalizadorLexico:
    def __init__(self, codigo):
        self.codigo_fuente = codigo
        self.posicion_actual = 0
        self.linea_actual = 1
        self.tabla_de_simbolos = {} # Diccionario para almacenar Simbolos
        self.errores = []           # Lista para almacenar errores léxicos

    # --- Funciones de Utilidad ---




    def agregar_simbolo(self, simbolo):
        """Añade un símbolo a la tabla si no existe ya."""
        if simbolo.nombre not in self.tabla_de_simbolos:
            self.tabla_de_simbolos[simbolo.nombre] = simbolo
            return True
        return False # Ya existe

    def _avanzar(self):
        """Avanza una posición y retorna el caracter actual."""
        if self.posicion_actual < len(self.codigo_fuente):
            caracter = self.codigo_fuente[self.posicion_actual]
            self.posicion_actual += 1
            if caracter == '\n':
                self.linea_actual += 1
            return caracter
        return None

    def _ver_siguiente(self):
        """Retorna el siguiente caracter sin avanzar la posición (lookahead)."""
        if self.posicion_actual < len(self.codigo_fuente):
            return self.codigo_fuente[self.posicion_actual]
        return None

    def _reportar_error(self, descripcion, lexema=''):
        """Reporta y almacena un error léxico."""
        error = f"ERROR LÉXICO (Línea {self.linea_actual}): '{lexema}' - {descripcion}"
        self.errores.append(error)
        print(error)
        return Token(T_ERROR, lexema, self.linea_actual)

    def _ignorar_espacios_y_comentarios(self):
        """Ignora espacios, tabulaciones y saltos de línea."""
        while self.posicion_actual < len(self.codigo_fuente):
            c = self.codigo_fuente[self.posicion_actual]
            if c.isspace():
                if c == '\n':
                    self.linea_actual += 1
                self.posicion_actual += 1
            # Se podría añadir aquí la lógica para ignorar comentarios (ej: // o #)
            else:
                break

    # --- Funciones de Reconocimiento ---
    
    def _reconocer_identificador_o_palabra_clave(self):
        """Reconoce un lexema que empieza con letra y lo clasifica como ID o PC."""
        inicio = self.posicion_actual - 1
        
        while self._ver_siguiente() and (self._ver_siguiente().isalnum() or self._ver_siguiente() == '_'):
            self._avanzar()
            
        lexema = self.codigo_fuente[inicio:self.posicion_actual]
        
        # Clasificación: ¿Es una Palabra Clave o un Identificador?
        if lexema.upper() in PALABRAS_CLAVE:
            # Es una palabra clave fija del lenguaje
            tipo = PALABRAS_CLAVE[lexema.upper()]
        else:
            # Es un identificador (nombre de instancia de estructura)
            tipo = T_ID
            
        return Token(tipo, lexema, self.linea_actual)

    def _reconocer_literal_numerico(self):
        """Reconoce un número entero o decimal (con lookahead)."""
        inicio = self.posicion_actual - 1
        
        while self._ver_siguiente() and self._ver_siguiente().isdigit():
            self._avanzar()
            
        # Opcional: Soporte para números decimales
        if self._ver_siguiente() == '.':
            self._avanzar() # Consumir el punto
            while self._ver_siguiente() and self._ver_siguiente().isdigit():
                self._avanzar()
                
        lexema = self.codigo_fuente[inicio:self.posicion_actual]
        
        return Token(T_LIT_NUMERO, lexema, self.linea_actual)


    # --- Función Central ---

    def obtener_siguiente_token(self):
        """Función principal para obtener el siguiente token."""
        self._ignorar_espacios_y_comentarios()

        if self.posicion_actual >= len(self.codigo_fuente):
            return Token(T_EOF, "", self.linea_actual)

        c = self._avanzar()
        
        # RECONOCIMIENTO
        if c.isalpha() or c == '_':
            # Paso 4: Reconocimiento de ID y Palabras Clave
            return self._reconocer_identificador_o_palabra_clave()
        elif c.isdigit():
            # Paso 4: Reconocimiento de Literales Numéricos
            return self._reconocer_literal_numerico()
        elif c == ';':
            return Token(T_PUNTO_COMA, ';', self.linea_actual)

        # GESTIÓN DE ERRORES LÉXICOS (Paso 5)
        else:
            # Símbolo no permitido en el alfabeto del DSL
            return self._reportar_error("Símbolo no permitido en el lenguaje.", c)
        

def analizar_codigo_fuente(codigo):
    """
    Función de prueba que simula el análisis y la interacción con la TS.
    """
    lexer = AnalizadorLexico(codigo)
    tokens = []
    
    print("--- INICIO DEL ANÁLISIS LÉXICO ---")
    
    while True:
        token = lexer.obtener_siguiente_token()
        tokens.append(token)
        print(token)
        
        if token.tipo == T_EOF or token.tipo == T_ERROR:
            break
            
    # SIMULACIÓN de la GESTIÓN DE LA TABLA DE SÍMBOLOS
    # Esto lo haría la fase Sintáctica/Semántica:
    print("\n--- SIMULACIÓN DE LA TABLA DE SÍMBOLOS ---")
    
    # Identificamos el patrón de declaración: CREAR [TIPO] [ID] ;
    for i, token in enumerate(tokens):
        if token.tipo == T_CREAR and i + 2 < len(tokens):
            tipo_struct = tokens[i+1] # Esperamos el tipo (ej: PILA)
            nombre_id = tokens[i+2]   # Esperamos el ID (ej: MiPila)
            
            if tipo_struct.tipo in [T_PILA, T_COLA, T_ARBOL] and nombre_id.tipo == T_ID:
                # La declaración es válida léxicamente, la añadimos a la TS
                simbolo = Simbolo(nombre_id.lexema, tipo_struct.tipo, nombre_id.linea)
                if lexer.agregar_simbolo(simbolo): # Usamos el método de agregar
                    print(f"✅ TS: '{nombre_id.lexema}' registrado como {tipo_struct.tipo}.")
                else:
                    print(f"❌ TS: Error, '{nombre_id.lexema}' ya existe.")

    print("\n--- CONTENIDO FINAL DE LA TABLA DE SÍMBOLOS ---")
    print(lexer.tabla_de_simbolos)
    
    return tokens, lexer.errores

# CÓDIGO DE PRUEBA DSL
codigo_dsl = """
CREAR COLA ColaDePrueba;
INSERTAR 5 EN ColaDePrueba; # Símbolo no permitido $
CREAR PILA MiPila;
"""

analizar_codigo_fuente(codigo_dsl)