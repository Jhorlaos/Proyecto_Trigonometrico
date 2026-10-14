
# Diagrama Entidad-Relación - Base de Datos

Proyecto integrado: Calculadora Java, Python y SQLite.

```mermaid
erDiagram
    OPERACION ||--o{ CALCULO : "se ejecuta en (1:N)"
    OPERACION ||--o{ SERIE : "genera (1:N)"
    SERIE ||--|{ PUNTO : "contiene (1:N)"

    OPERACION {
        int id PK
        string simbolo UK "Not Null"
        string nombre "Not Null"
        string tipo "CHECK: BASICA, TRIGONOMETRICA"
        string lenguaje "CHECK: JAVA, PYTHON"
        int aridad "CHECK: 1, 2"
    }

    CALCULO {
        int id PK
        int operacion_id FK "Not Null"
        double operando_a "Not Null"
        double operando_b "Null para aridad 1"
        string modo_angulo "CHECK: grados, radianes, Null"
        double resultado "Null si falla"
        int exito "CHECK: 0, 1"
        string mensaje_error "Null si exito=1"
        datetime creado_en "Default CURRENT_TIMESTAMP"
    }

    SERIE {
        int id PK
        int operacion_id FK "Not Null"
        string nombre "Not Null"
        double x_min "Not Null"
        double x_max "Not Null"
        double paso "CHECK: paso > 0"
        string modo_angulo "CHECK: grados, radianes"
        datetime creado_en "Default CURRENT_TIMESTAMP"
    }

    PUNTO {
        int id PK
        int serie_id FK "Not Null, On Delete Cascade"
        double x "Not Null"
        double y "Null si es indefinido (ej. tan 90°)"
    }
```
