
# Diagrama de Clases UML

Este diagrama representa las clases, interfaces, atributos, métodos, excepciones y relaciones del sistema integrado de la calculadora.

```mermaid
classDiagram
    direction TB

    %% INTERFAZ DE OPERACIONES

    class Operacion {
        <<interface>>
        +getSimbolo() String
        +getNombre() String
        +getAridad() int
        +calcular(operandos: double[]) double
    }

    %% CLASE ABSTRACTA Y OPERACIONES BASICAS

    class OperacionBasica {
        <<abstract>>
        #simbolo: String
        #nombre: String
        #aridad: int
        +OperacionBasica(simbolo: String, nombre: String, aridad: int)
        +getSimbolo() String
        +getNombre() String
        +getAridad() int
        +calcular(operandos: double[]) double*
    }

    class Suma {
        +Suma()
        +calcular(operandos: double[]) double
    }

    class Resta {
        +Resta()
        +calcular(operandos: double[]) double
    }

    class Multiplicacion {
        +Multiplicacion()
        +calcular(operandos: double[]) double
    }

    class Division {
        +Division()
        +calcular(operandos: double[]) double
    }

    %% TRIGONOMETRIA Y COMUNICACION CON PYTHON

    class OperacionTrigonometrica {
        -simbolo: String
        -nombre: String
        -aridad: int
        -modoAngulo: String
        -pythonBridge: PythonBridge
        +OperacionTrigonometrica(simbolo: String, nombre: String, pythonBridge: PythonBridge)
        +getSimbolo() String
        +getNombre() String
        +getAridad() int
        +getModoAngulo() String
        +setModoAngulo(modo: String) void
        +calcular(operandos: double[]) double
    }

    class PythonBridge {
        -pythonExecutable: String
        -scriptTrigPath: String
        -scriptGraficarPath: String
        +PythonBridge(pythonExecutable: String, scriptTrigPath: String, scriptGraficarPath: String)
        +ejecutarTrig(funcion: String, valor: double, modo: String) String
        +ejecutarSerie(funcion: String, xmin: double, xmax: double, paso: double, modo: String) List~String~
        +ejecutarGrafica(dbPath: String, serieId: int, salidaPNG: String) boolean
    }

    %% ENTIDADES DE LA BASE DE DATOS

    class OperacionDTO {
        -id: Integer
        -simbolo: String
        -nombre: String
        -tipo: String
        -lenguaje: String
        -aridad: int
        +OperacionDTO(id: Integer, simbolo: String, nombre: String, tipo: String, lenguaje: String, aridad: int)
        +getId() Integer
        +getSimbolo() String
        +getNombre() String
        +getTipo() String
        +getLenguaje() String
        +getAridad() int
    }

    class CalculoDTO {
        -id: Integer
        -operacionId: int
        -operandoA: double
        -operandoB: Double
        -modoAngulo: String
        -resultado: Double
        -exito: boolean
        -mensajeError: String
        -creadoEn: LocalDateTime
        +CalculoDTO(operacionId: int, operandoA: double, operandoB: Double, modoAngulo: String, resultado: Double, exito: boolean, mensajeError: String)
        +getId() Integer
        +setId(id: int) void
        +getOperacionId() int
        +getOperandoA() double
        +getOperandoB() Double
        +getModoAngulo() String
        +getResultado() Double
        +isExito() boolean
        +getMensajeError() String
        +getCreadoEn() LocalDateTime
    }

    class SerieDTO {
        -id: Integer
        -operacionId: int
        -nombre: String
        -xMin: double
        -xMax: double
        -paso: double
        -modoAngulo: String
        -creadoEn: LocalDateTime
        +SerieDTO(operacionId: int, nombre: String, xMin: double, xMax: double, paso: double, modoAngulo: String)
        +getId() Integer
        +setId(id: int) void
        +getOperacionId() int
        +getNombre() String
        +getXMin() double
        +getXMax() double
        +getPaso() double
        +getModoAngulo() String
        +getCreadoEn() LocalDateTime
    }

    class PuntoDTO {
        -id: Integer
        -serieId: int
        -x: double
        -y: Double
        +PuntoDTO(serieId: int, x: double, y: Double)
        +getId() Integer
        +setId(id: int) void
        +getSerieId() int
        +getX() double
        +getY() Double
    }

    %% ACCESO A BASE DE DATOS

    class OperacionDAO {
        -dbUrl: String
        +OperacionDAO(dbUrl: String)
        +buscarPorSimbolo(simbolo: String) OperacionDTO
        +listarOperaciones() List~OperacionDTO~
    }

    class CalculoDAO {
        -dbUrl: String
        +CalculoDAO(dbUrl: String)
        +guardarCalculo(calculo: CalculoDTO) int
        +obtenerHistorial() List~CalculoDTO~
    }

    class SerieDAO {
        -dbUrl: String
        +SerieDAO(dbUrl: String)
        +guardarSerie(serie: SerieDTO) int
        +guardarPunto(punto: PuntoDTO) boolean
        +obtenerPuntos(serieId: int) List~PuntoDTO~
    }

    %% SERVICIO PRINCIPAL

    class CalculadoraService {
        -operaciones: Map~String, Operacion~
        -operacionDAO: OperacionDAO
        -calculoDAO: CalculoDAO
        +CalculadoraService(operacionDAO: OperacionDAO, calculoDAO: CalculoDAO)
        +registrarOperacion(operacion: Operacion) void
        +validarOperandos(entradas: String[], aridad: int) double[]
        +ejecutarOperacion(simbolo: String, entradas: String[], modoAngulo: String) double
        +consultarHistorial() List~CalculoDTO~
    }

    %% SERVICIO DE SERIES Y GRAFICAS

    class SerieService {
        -operacionDAO: OperacionDAO
        -serieDAO: SerieDAO
        -pythonBridge: PythonBridge
        -dbPath: String
        +SerieService(operacionDAO: OperacionDAO, serieDAO: SerieDAO, pythonBridge: PythonBridge, dbPath: String)
        +validarRango(xmin: double, xmax: double, paso: double) boolean
        +generarSerie(funcion: String, nombre: String, xmin: double, xmax: double, paso: double, modo: String) int
        +convertirPuntos(lineas: List~String~, serieId: int) List~PuntoDTO~
        +generarGrafica(serieId: int, salidaPNG: String) boolean
    }

    %% INTERFAZ DEL USUARIO

    class CalculadoraUI {
        -calculadoraService: CalculadoraService
        -serieService: SerieService
        +CalculadoraUI(calculadoraService: CalculadoraService, serieService: SerieService)
        +solicitarOperacion() String
        +solicitarOperandos() String[]
        +solicitarModoAngulo() String
        +solicitarFuncion() String
        +solicitarXMin() double
        +solicitarXMax() double
        +solicitarPaso() double
        +mostrarResultado(resultado: double) void
        +mostrarError(mensaje: String) void
        +mostrarGrafica(rutaPNG: String) void
    }

    %% EXCEPCIONES

    class CalculadoraException {
        +CalculadoraException(mensaje: String)
    }

    class DivisionPorCeroException {
        +DivisionPorCeroException(mensaje: String)
    }

    class OperacionTrigonometricaException {
        +OperacionTrigonometricaException(mensaje: String)
    }

    class ValidacionException {
        +ValidacionException(mensaje: String)
    }

    class PythonEjecucionException {
        +PythonEjecucionException(mensaje: String)
    }

    %% HERENCIA E IMPLEMENTACION

    Operacion <|.. OperacionBasica : implementa
    Operacion <|.. OperacionTrigonometrica : implementa

    OperacionBasica <|-- Suma : hereda
    OperacionBasica <|-- Resta : hereda
    OperacionBasica <|-- Multiplicacion : hereda
    OperacionBasica <|-- Division : hereda

    CalculadoraException <|-- DivisionPorCeroException : hereda
    CalculadoraException <|-- OperacionTrigonometricaException : hereda
    CalculadoraException <|-- ValidacionException : hereda
    CalculadoraException <|-- PythonEjecucionException : hereda

    %% DEPENDENCIAS CON EXCEPCIONES

    Division ..> DivisionPorCeroException : lanza
    OperacionTrigonometrica ..> OperacionTrigonometricaException : lanza
    CalculadoraService ..> ValidacionException : lanza
    SerieService ..> ValidacionException : lanza
    PythonBridge ..> PythonEjecucionException : lanza

    %% ASOCIACIONES DE LOS SERVICIOS

    CalculadoraUI --> CalculadoraService : utiliza (1 a 1)
    CalculadoraUI --> SerieService : utiliza (1 a 1)

    CalculadoraService --> Operacion : gestiona (1 a 1..*)
    CalculadoraService --> OperacionDAO : consulta (1 a 1)
    CalculadoraService --> CalculoDAO : guarda (1 a 1)

    OperacionTrigonometrica --> PythonBridge : invoca (1 a 1)

    SerieService --> OperacionDAO : consulta (1 a 1)
    SerieService --> SerieDAO : guarda (1 a 1)
    SerieService --> PythonBridge : invoca (1 a 1)

    %% DEPENDENCIAS DTO

    CalculadoraService ..> CalculoDTO : crea
    SerieService ..> SerieDTO : crea
    SerieService ..> PuntoDTO : crea

    OperacionDAO ..> OperacionDTO : consulta
    CalculoDAO ..> CalculoDTO : persiste
    SerieDAO ..> SerieDTO : persiste
    SerieDAO ..> PuntoDTO : persiste

    %% RELACIONES DE BASE DE DATOS

    OperacionDTO --> CalculoDTO : operacionId (1 a 0..*)
    OperacionDTO --> SerieDTO : operacionId (1 a 0..*)
    SerieDTO --> PuntoDTO : serieId (1 a 1..*)
```

