import sys
import math
import sqlite3
from pathlib import Path
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

def generar_grafica(db_path, serie_id, ruta_salida_png):
    conn = None
    try:
        # 1. Conexión segura a la base de datos SQLite en modo lectura
        db_uri = Path(db_path).resolve().as_uri() + "?mode=ro"
        conn = sqlite3.connect(db_uri, uri=True)
        cursor = conn.cursor()

        # 2. Obtener metadatos de la serie para títulos y etiquetas
        cursor.execute("SELECT nombre, modo_angulo, x_min, x_max, paso FROM serie WHERE id = ?", (serie_id,))
        serie_info = cursor.fetchone()

        if not serie_info:
            print(f"ERROR;No se encontró la serie ID {serie_id}")
            return

        nombre, modo, xmin, xmax, paso = serie_info
        titulo_grafica = f"f(x) = {nombre}(x)  [{modo}]"

        # 3. Consultar los puntos ordenados cronológicamente por x
        cursor.execute("SELECT x, y FROM punto WHERE serie_id = ? ORDER BY x ASC", (serie_id,))
        filas = cursor.fetchall()
        conn.close()
        conn = None

        if not filas:
            print(f"ERROR;No se encontraron puntos para la serie ID {serie_id}")
            return

        x_vals = []
        y_vals = []

        # Interrupción de líneas en las asíntotas de la tangente
        anterior_rad = None
        for x, y in filas:
            if nombre == "tan":
                rad = math.radians(x) if modo == "grados" else x
                if anterior_rad is not None and math.floor((anterior_rad + math.pi / 2) / math.pi) != math.floor((rad + math.pi / 2) / math.pi):
                    x_vals.append(x)
                    y_vals.append(float("nan"))
                anterior_rad = rad
            
            x_vals.append(x)
            y_vals.append(y if y is not None else float("nan"))

        # 4. Configurar el tema visual oscuro alineado con la UI de Java Swing
        plt.style.use('dark_background')
        fig, ax = plt.subplots(figsize=(9, 5.2), dpi=160)
        fig.patch.set_facecolor('#25292D')  # Color OSCURO de Java Swing
        ax.set_facecolor('#1D2124')         # Fondo interno del panel

        # Color verde característico de la UI de la calculadora (#B5E43B)
        line_color = '#B5E43B'
        
        # Si la serie tiene pocos puntos muestreados (<= 35), dibuja puntos circulares sobre la línea
        puntos_validos = sum(1 for y in y_vals if not math.isnan(y))
        mostrar_puntos = puntos_validos <= 35
        
        marker_style = 'o' if mostrar_puntos else None
        marker_size = 5 if mostrar_puntos else None

        # Trazar la curva principal
        ax.plot(x_vals, y_vals, color=line_color, linewidth=2.2, 
                marker=marker_style, markersize=marker_size, 
                markerfacecolor='#FFFFFF', markeredgecolor=line_color,
                label=titulo_grafica, zorder=3)

        # Relleno sutil con transparencia para funciones continuas
        if nombre in ("sin", "cos", "atan"):
            y_filled = [0 if math.isnan(v) else v for v in y_vals]
            ax.fill_between(x_vals, y_filled, color=line_color, alpha=0.12, zorder=2)

        # 5. Acotar eje Y para evitar desbordamiento en asíntotas de 'tan'
        if nombre == "tan":
            ax.set_ylim(-6.0, 6.0)
        elif nombre in ("asin", "acos") and modo == "grados":
            ax.set_ylim(-95, 95)

        # Ejes de referencia (x=0, y=0)
        ax.axhline(0, color='#6C757D', linewidth=1.2, linestyle='--', zorder=1)
        ax.axvline(0, color='#6C757D', linewidth=1.2, linestyle='--', zorder=1)

        # Rejilla (Grid)
        ax.grid(True, which='both', linestyle=':', color='#4A5157', alpha=0.6, zorder=0)

        # Personalización del marco
        for spine in ax.spines.values():
            spine.set_color('#4A5157')
            spine.set_linewidth(1.0)

        # Títulos y etiquetas de ejes
        inversa = nombre in ("asin", "acos", "atan")
        lbl_x = "x (valor)" if inversa else f"x (ángulo en {modo})"
        lbl_y = f"y (ángulo en {modo})" if inversa else "y (resultado)"

        ax.set_title(f"Serie #{serie_id} — {titulo_grafica}", fontsize=13, fontweight='bold', color='#FFFFFF', pad=12)
        ax.set_xlabel(lbl_x, fontsize=10, fontweight='bold', color='#BCC4CA', labelpad=8)
        ax.set_ylabel(lbl_y, fontsize=10, fontweight='bold', color='#BCC4CA', labelpad=8)

        ax.tick_params(colors='#BCC4CA', labelsize=9)

        # Leyenda en la esquina superior derecha
        legend = ax.legend(loc='upper right', facecolor='#25292D', edgecolor='#4A5157', fontsize=9)
        plt.setp(legend.get_texts(), color='#FFFFFF')

        # 6. Guardar la imagen en alta resolución PNG (dpi=160)
        plt.savefig(ruta_salida_png, dpi=160, bbox_inches='tight', facecolor=fig.get_facecolor(), edgecolor='none')
        plt.close(fig)

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
    
    # Contrato técnico estricto: python Graficar.py <ruta_db> <serie_id> <salida.png>
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