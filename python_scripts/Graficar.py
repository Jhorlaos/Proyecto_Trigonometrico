import sys
import math
import sqlite3
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

def generar_grafica(db_path, serie_id, ruta_salida_png):
    conn = None
    try:
        # 1. Conexión a la base de datos SQLite
        from pathlib import Path
        conn = sqlite3.connect(Path(db_path).resolve().as_uri() + "?mode=ro", uri=True)
        cursor = conn.cursor()

        # 2. Obtener el nombre de la serie para el título
        cursor.execute("SELECT nombre, modo_angulo FROM serie WHERE id = ?", (serie_id,))
        serie_info = cursor.fetchone()
        
        titulo_grafica = f"Serie #{serie_id}"
        if serie_info:
            titulo_grafica = f"{serie_info[0]} ({serie_info[1]})"

        # 3. Consultar los puntos de la serie ordenados por x
        cursor.execute("SELECT x, y FROM punto WHERE serie_id = ? ORDER BY x ASC", (serie_id,))
        filas = cursor.fetchall()
        conn.close()

        if not filas:
            print(f"ERROR;No se encontraron puntos para la serie ID {serie_id}")
            return

        x_vals = []
        y_vals = []

        anterior_rad = None
        for x, y in filas:
            if serie_info and serie_info[0] == "tan":
                rad = math.radians(x) if serie_info[1] == "grados" else x
                if anterior_rad is not None and math.floor((anterior_rad + math.pi / 2) / math.pi) != math.floor((rad + math.pi / 2) / math.pi):
                    # El corte es visual: no se altera el valor matemático guardado.
                    x_vals.append(x)
                    y_vals.append(float("nan"))
                anterior_rad = rad
            x_vals.append(x)
            # Si y es None (NULL en SQLite), matplotlib no dibuja la línea en ese punto
            y_vals.append(y)

        # 4. Configurar el gráfico con matplotlib
        plt.figure(figsize=(8, 5))
        plt.plot(x_vals, y_vals, label=titulo_grafica, color="#2b5c8f", linewidth=2)
        plt.axhline(0, color="black", linewidth=0.8, linestyle="--")
        plt.axvline(0, color="black", linewidth=0.8, linestyle="--")
        plt.grid(True, linestyle=":", alpha=0.6)
        
        plt.title(f"Gráfica: {titulo_grafica}", fontsize=12, fontweight="bold")
        nombre, modo = serie_info if serie_info else ("", "")
        inversa = nombre in ("asin", "acos", "atan")
        plt.xlabel("x (valor)" if inversa else f"x (ángulo en {modo})", fontsize=10)
        plt.ylabel(f"y (ángulo en {modo})" if inversa else "y (resultado)", fontsize=10)
        plt.legend(loc="upper right")

        # 5. Guardar la imagen en formato PNG
        plt.savefig(ruta_salida_png, dpi=120, bbox_inches="tight")
        plt.close()
        
        print("OK;Grafica generada exitosamente")

    except Exception as e:
        print(f"ERROR;Fallo al generar grafica: {str(e)}")
        sys.exit(1)
    finally:
        if conn is not None:
            conn.close()
        plt.close("all")

def main():
    args = sys.argv[1:]
    
    # Contrato técnico: python graficar.py <ruta_db> <serie_id> <salida.png>
    if len(args) < 3:
        print("ERROR;Argumentos insuficientes. Uso: python Graficar.py <ruta_db> <serie_id> <salida.png>")
        return

    db_path = args[0]
    try:
        serie_id = int(args[1])
    except ValueError:
        print("ERROR;El serie_id debe ser un número entero")
        return

    salida_png = args[2]
    
    generar_grafica(db_path, serie_id, salida_png)

if __name__ == "__main__":
    main()