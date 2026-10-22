
# Diagrama Entidad-Relación - Base de Datos

Proyecto integrado: Calculadora Java, Python y SQLite.

Este diagrama representa las entidades, atributos, claves primarias (PK), claves foráneas (FK), restricciones y cardinalidades del sistema.

```mermaid
erDiagram
    %% Relaciones uno a muchos. Cada entidad hija tiene su propio id como PK.
    %% Por eso las relaciones son no identificadoras (linea discontinua).
    OPERACION ||..o{ CALCULO : registra
    OPERACION ||..o{ SERIE : genera
    SERIE ||..|{ PUNTO : contiene

    OPERACION {
        INTEGER id PK "PRIMARY KEY"
        TEXT simbolo UK "NOT NULL UNIQUE"
        TEXT nombre "NOT NULL"
        TEXT tipo "NOT NULL CHECK BASICA o TRIGONOMETRICA"
        TEXT lenguaje "NOT NULL CHECK JAVA o PYTHON"
        INTEGER aridad "NOT NULL CHECK 1 o 2"
    }

    CALCULO {
        INTEGER id PK "PRIMARY KEY"
        INTEGER operacion_id FK "NOT NULL"
        REAL operando_a "NOT NULL"
        REAL operando_b "NULL si la aridad es 1"
        TEXT modo_angulo "NULL basica; grados o radianes trig"
        REAL resultado "NULL si exito es 0"
        INTEGER exito "NOT NULL CHECK 0 o 1"
        TEXT mensaje_error "NULL si exito es 1"
        TEXT creado_en "NOT NULL DEFAULT CURRENT_TIMESTAMP"
    }

    SERIE {
        INTEGER id PK "PRIMARY KEY"
        INTEGER operacion_id FK "NOT NULL"
        TEXT nombre "NOT NULL"
        REAL x_min "NOT NULL"
        REAL x_max "NOT NULL CHECK x_min menor que x_max"
        REAL paso "NOT NULL CHECK mayor que 0"
        TEXT modo_angulo "NOT NULL grados o radianes"
        TEXT creado_en "NOT NULL DEFAULT CURRENT_TIMESTAMP"
    }

    PUNTO {
        INTEGER id PK "PRIMARY KEY"
        INTEGER serie_id FK "NOT NULL ON DELETE CASCADE"
        REAL x "NOT NULL"
        REAL y "NULL si la funcion no esta definida"
    }

    %% Reglas adicionales a implementar en create_schema.sql:
    %% 1) CHECK combinado: tipo BASICA usa JAVA; TRIGONOMETRICA usa PYTHON.
    %% 2) CHECK combinado: exito=1 exige resultado NOT NULL y mensaje_error NULL.
    %%    exito=0 exige resultado NULL y mensaje_error NOT NULL.
    %% 3) CHECK: x_min < x_max, paso > 0 y modo_angulo valido.
    %% 4) FOREIGN KEY: operacion_id y serie_id deben existir.
    %% 5) Aridad y modo de CALCULO dependen de OPERACION:
    %%    se validaran en la aplicacion o con triggers de SQLite.
    %% 6) Al menos un PUNTO por SERIE se asegura al generar
    %%    y guardar la serie completa dentro de una transaccion.
```
