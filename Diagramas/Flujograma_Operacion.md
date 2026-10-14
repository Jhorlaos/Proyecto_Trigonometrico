# Flujograma 1 - Operación matemática

flowchart TD
    A([Inicio]) --> B[/Solicitar al usuario operacion y operandos/]
    B --> C{¿La operacion existe en el catalogo?}
    
    C -- No --> D[Generar mensaje 'Operacion inexistente']
    D --> E[[Guarda en BD calculo con exito=0 y mensaje de error]]
    E --> F[/Mostrar mensaje de error al usuario/]
    F --> Z([Fin])
    
    C -- Sí --> G{¿Los operandos son numeros validos?}
    G -- No --> H[Generar mensaje 'Entradas invalidas']
    H --> E
    
    G -- Sí --> I{¿Es operacion BASICA o TRIGONOMETRICA?}
    
    I -- BASICA --> J{¿Es una division con divisor igual a cero?}
    J -- Sí --> K[Lanzar DivisionPorCeroException]
    K --> L[Capturar error 'Division por cero']
    L --> E
    
    J -- No --> M[Calcular resultado en Java]
    M --> N[[Guarda en BD calculo con exito=1 y resultado]]
    N --> O[/Mostrar resultado al usuario/]
    O --> Z
    
    I -- TRIGONOMETRICA --> P[/Solicitar modo de angulo: grados o radianes/]
    P --> Q[Construir y ejecutar comando ProcessBuilder para trig.py]
    Q --> R{¿La respuesta de Python inicia con OK?}
    
    R -- No --> S[Capturar mensaje de error devuelto por Python]
    S --> E
    
    R -- Sí --> T[Extraer valor numerico de la respuesta de Python]
    T --> N
