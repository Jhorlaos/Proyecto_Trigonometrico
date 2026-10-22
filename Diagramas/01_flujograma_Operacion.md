
# Flujograma 1: Operación completa

**Proyecto:** Calculadora Java, Python y SQLite

Este flujograma representa el proceso de ejecución de operaciones matemáticas, validación de datos, manejo de errores y almacenamiento de los resultados en la base de datos.

```mermaid
flowchart TD
    A([Inicio]) --> B[/Solicitar operacion y operandos al usuario/]
    B --> C{¿La operacion existe en el catalogo?}

    C -- No --> D[Generar mensaje de operacion inexistente]
    D --> F[/Mostrar mensaje de validacion/]
    F --> Z([Fin])

    C -- Sí --> CAT[Consultar tipo y aridad en el catalogo]
    CAT --> I{¿La operacion es basica?}

    %% Camino de operaciones basicas en Java
    I -- Sí --> G[Validar operandos segun la aridad]
    G --> H{¿Los operandos son numericos y la cantidad es correcta?}
    H -- No --> U[Generar mensaje de entradas invalidas]
    U --> F

    H -- Sí --> J{¿Es division con divisor igual a cero?}
    J -- Sí --> K[Lanzar DivisionPorCeroException]
    K --> L[Capturar mensaje de division por cero]
    L --> E[[Guardar calculo fallido con resultado NULL y mensaje, con espera limitada ante bloqueos temporales de SQLite mediante busy_timeout en la conexion JDBC]]

    J -- No --> M[Calcular resultado en Java]
    M --> M2{¿El calculo produjo un resultado valido?}
    M2 -- No --> L2[Capturar mensaje de error del calculo]
    L2 --> E
    M2 -- Sí --> N[[Guardar calculo exitoso con resultado, con espera limitada ante bloqueos temporales de SQLite mediante busy_timeout en la conexion JDBC]]

    %% Camino de operaciones trigonometricas en Python
    I -- No --> P[/Solicitar modo de angulo al usuario/]
    P --> V{¿El modo es grados o radianes?}
    V -- No --> W[Generar mensaje de modo invalido]
    W --> F

    V -- Sí --> GT[Validar operandos segun la aridad]
    GT --> HT{¿Los operandos son numericos y la cantidad es correcta?}
    HT -- No --> U
    HT -- Sí --> Q[Ejecutar trig.py mediante ProcessBuilder]
    Q --> R{¿Python devolvio OK con un numero valido?}
    R -- No --> S[Capturar ERROR de Python o fallo de ejecucion]
    S --> E
    R -- Sí --> T[Extraer resultado numerico de Python]
    T --> N

    %% Comprobar persistencia y mostrar la salida correcta
    N --> N2{¿Se guardo el calculo exitoso?}
    N2 -- Sí --> O[/Mostrar resultado al usuario/]
    O --> Z
    N2 -- No --> X[Generar mensaje de error al guardar]
    X --> Y[/Mostrar error de almacenamiento al usuario/]
    Y --> Z

    E --> E2{¿Se guardo el calculo fallido?}
    E2 -- Sí --> F2[/Mostrar mensaje del error matematico o de Python/]
    F2 --> Z
    E2 -- No --> X
```

