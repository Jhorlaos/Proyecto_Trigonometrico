

# Flujograma 2: Generación de puntos y gráfica

Este flujograma describe la validación de rangos, generación de puntos mediante Python, almacenamiento en SQLite y creación de gráficas en formato PNG.

```mermaid
flowchart TD
    A([Inicio]) --> B[/Solicitar funcion, xmin, xmax, paso y modo de angulo/]
    B --> C{¿La funcion existe y es trigonometrica?}

    C -- No --> D[Generar mensaje de funcion no valida]
    D --> E[/Mostrar mensaje de error al usuario/]
    E --> Z([Fin])

    C -- Sí --> F[Validar numeros, rango, paso y modo de angulo]
    F --> G{¿Los parametros son validos?}
    G -- No --> H[Generar mensaje de parametros invalidos]
    H --> E

    %% Python genera todos los puntos antes de almacenarlos
    G -- Sí --> I[Invocar trig.py serie mediante ProcessBuilder]
    I --> J{¿Python devolvio la serie sin error?}
    J -- No --> K[Capturar mensaje de error de Python]
    K --> E
    J -- Sí --> L[Recibir lista de pares x;y]
    L --> M{¿La lista contiene puntos?}
    M -- No --> N[Generar mensaje de serie vacia]
    N --> E

    M -- Sí --> O[Validar formato y valores de todos los pares x;y]
    O --> P{¿Todos los puntos tienen formato correcto?}
    P -- No --> Q[Generar mensaje de puntos malformados]
    Q --> E
    P -- Sí --> R[Convertir y vacia en NULL y detectar puntos indefinidos]

    %% La serie y sus puntos se guardan como una sola unidad
    R --> S[[Guardar encabezado de serie y puntos en una transaccion]]
    S --> T{¿Se confirmo el guardado completo con COMMIT?}
    T -- No --> U[Revertir cambios pendientes con ROLLBACK]
    U --> V[/Mostrar error de almacenamiento al usuario/]
    V --> Z

    %% graficar.py obtiene los puntos ya almacenados en SQLite
    T -- Sí --> W[Invocar graficar.py con ruta BD, serie_id y archivo PNG]
    W --> X{¿Se genero el PNG correctamente?}
    X -- No --> Y[/Mostrar error de grafica e indicar que la serie esta guardada/]
    Y --> Z
    X -- Sí --> AA{¿Hay puntos con y igual a NULL?}
    AA -- Sí --> AB[/Mostrar PNG y advertencia de puntos indefinidos/]
    AB --> Z
    AA -- No --> AC[/Mostrar PNG generado al usuario/]
    AC --> Z
```
