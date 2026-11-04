import sys
import math

# Umbral para limpiar el ruido de precisión en punto flotante (ej: sin(180°) -> 0)
EPSILON = 1e-12

def limpiar_flotante(val):
    if abs(val) < EPSILON:
        return 0.0
    return val

def calcular_trig(fn, valor, modo="grados"):
    if not math.isfinite(valor):
        return "ERROR;Ingrese un número finito"
    if modo not in ("grados", "radianes"):
        return "ERROR;Modo de ángulo inválido"
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
    if fn not in ("sin", "cos", "tan", "asin", "acos", "atan") or modo not in ("grados", "radianes"):
        raise ValueError("Función o modo no válido")
    if not all(math.isfinite(v) for v in (xmin, xmax, paso)) or not xmin < xmax or paso <= 0:
        raise ValueError("Rango o paso no válido")
    intervalos = (xmax - xmin) / paso
    if not math.isfinite(intervalos) or not 1 <= intervalos <= 9999 or xmin + paso == xmin or xmax - paso == xmax:
        raise ValueError("Seleccione un paso que genere entre 2 y 10000 puntos")
    if fn in ("asin", "acos") and (xmin < -1 or xmax > 1):
        raise ValueError("El rango de asin y acos debe estar entre -1 y 1")
    cantidad = math.floor(intervalos + 1e-10) + 1
    for i in range(cantidad):
        x = min(xmin + i * paso, xmax)
        resp = calcular_trig(fn, x, modo)
        y = resp.split(";", 1)[1] if resp.startswith("OK;") else ""
        print(f"{x};{y}")


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
    try:
        main()
    except (ValueError, IndexError, OverflowError) as exc:
        print(f"ERROR;{exc}")
        sys.exit(1)
             