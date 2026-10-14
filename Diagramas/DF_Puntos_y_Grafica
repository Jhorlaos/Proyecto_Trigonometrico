flowchart TD
    

    A([Inicio]) --> B[/Solicitar funcion, xmin, xmax, paso y modo de angulo/]
    B --> C{¿xmin < xmax y paso > 0?}
    
    C -- No --> D[Generar mensaje 'Rango o paso invalido']
    D --> E[/Mostrar mensaje de error al usuario/]
    E --> Z([Fin])
    
    C -- Sí --> F[[Guarda en BD encabezado de la serie]]
    F --> G[Invocar Python trig.py serie con parametros]
    G --> H{¿Python ejecuto la serie correctamente?}
    
    H -- No --> I[Capturar mensaje de error de Python]
    I --> E
    
    H -- Sí --> J[Recibir lista de pares x y devuelta por Python]
    J --> K[Iterar sobre cada punto de la lista]
    
    K --> L{¿El punto contiene un valor y valido?}
    L -- Sí --> M[[Guarda en BD punto con x e y]]
    L -- No --> N[[Guarda en BD punto con x e y=NULL]]
    
    M --> O{¿Quedan mas puntos por procesar?}
    N --> O
    
    O -- Sí --> K
    O -- No --> P[Invocar Python graficar.py con la ruta BD e ID de serie]
    
    P --> Q{¿graficar.py genero el archivo PNG correctamente?}
    Q -- No --> R[Capturar error de generacion de grafica]
    R --> E
    
    Q -- Sí --> S[/Mostrar imagen PNG generada al usuario/]
    S --> Z
