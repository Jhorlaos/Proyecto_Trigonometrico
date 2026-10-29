import sys
import math

# Umbral para limpiar el ruido de precisión en punto flotante (ej: sin(180°) -> 0)
EPSILON = 1e-12

def limpiar_flotante(val):
    if abs(val) < EPSILON:
        return 0.0
    return val

def calcular_trig(fn, valor, modo="grados"):
    # Convertir a radianes si la entrada viene en grados
    if modo == "grados" and fn in ["sin", "cos", "tan"]:
        rad = math.radians(valor)
    else:
        rad = valor

    try:
        if fn == "sin":
            res = math.sin(rad)
        elif fn == "cos":
            res = math.cos(rad)
        elif fn == "tan":
            if modo == "grados" and abs(valor % 180 - 90) < EPSILON:
                return "ERROR;Indefinido en tan(90° + k*180°)"
            res = math.tan(rad)
            if abs(res) > 1e10:
                return "ERROR;Indefinido en tan(pi/2 + k*pi)"
        elif fn == "asin":
            if valor < -1.0 or valor > 1.0:
                return "ERROR;Dominio invalido para asin [-1, 1]"
            res = math.asin(valor)
            if modo == "grados":
                res = math.degrees(res)
        elif fn == "acos":
            if valor < -1.0 or valor > 1.0:
                return "ERROR;Dominio invalido para acos [-1, 1]"
            res = math.acos(valor)
            if modo == "grados":
                res = math.degrees(res)
        elif fn == "atan":
            res = math.atan(valor)
            if modo == "grados":
                res = math.degrees(res)
        else:
            return f"ERROR;Funcion '{fn}' no reconocida"

        res = limpiar_flotante(res)
        return f"OK;{res}"

    except Exception as e:
        return f"ERROR;{str(e)}"

def generar_serie(fn, xmin, xmax, paso, modo="grados"):
    x = xmin
    while x <= xmax + 1e-9:
        resp = calcular_trig(fn, x, modo)
        if resp.startswith("OK;"):
            y_val = resp.split(";")[1]
            print(f"{x};{y_val}")
        else:
            # Puntos indefinidos se imprimen con 'y' vacío (ej: 90;)
            print(f"{x};")
        x += paso

def main():
    args = sys.argv[1:]
    if not args:
        print("ERROR;Sin argumentos")
        return

    # Leer el modo de ángulo opcional (--modo grados o --modo radianes)
    modo = "grados"
    if "--modo" in args:
        idx = args.index("--modo")
        if idx + 1 < len(args):
            modo = args[idx + 1]

    # Subcomando 1: Generar serie de puntos para gráficas
    if args[0] == "serie":
        if len(args) < 5:
            print("ERROR;Faltan argumentos para la serie")
            return
        fn = args[1]
        xmin = float(args[2])
        xmax = float(args[3])
        paso = float(args[4])
        generar_serie(fn, xmin, xmax, paso, modo)
    
    # Subcomando 2: Cálculo puntual
    else:
        fn = args[0]
        valor = float(args[1])
        print(calcular_trig(fn, valor, modo))

if __name__ == "__main__":
    main()
             