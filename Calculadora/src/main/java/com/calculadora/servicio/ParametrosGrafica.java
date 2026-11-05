package com.calculadora.servicio;

import java.util.Set;

/** Datos inmutables: identifican una gráfica y permiten reutilizarla. */
public record ParametrosGrafica(String funcion, String modo, double minimo, double maximo, double paso) {
    public ParametrosGrafica {
        if (funcion == null || !Set.of("sin", "cos", "tan", "asin", "acos", "atan").contains(funcion)) {
            throw new IllegalArgumentException("Seleccione una función trigonométrica válida.");
        }
        if (!("grados".equals(modo) || "radianes".equals(modo))) {
            throw new IllegalArgumentException("Seleccione grados o radianes.");
        }
        if (!rangoValido(minimo, maximo, paso)) {
            throw new IllegalArgumentException("Use mínimo menor que máximo y un paso positivo para generar entre 2 y 10000 puntos.");
        }
        if ((funcion.equals("asin") || funcion.equals("acos")) && (minimo < -1 || maximo > 1)) {
            throw new IllegalArgumentException("Para asin y acos, el intervalo debe estar entre -1 y 1.");
        }
    }

    public static boolean rangoValido(double minimo, double maximo, double paso) {
        double intervalos = (maximo - minimo) / paso;
        return Double.isFinite(minimo) && Double.isFinite(maximo) && Double.isFinite(paso)
                && minimo < maximo && paso > 0 && paso <= maximo - minimo
                && minimo + paso > minimo && maximo - paso < maximo
                && Double.isFinite(intervalos) && intervalos <= 9999;
    }

    /** Genera segmentos adaptativos optimizados según la función y el modo para no saturar SQLite. */
    public static ParametrosGrafica recomendados(String funcion, String modo) {
        double min, max, paso;
        boolean esGrados = "grados".equals(modo);

        switch (funcion) {
            case "asin":
            case "acos":
                // Dominio [-1, 1] -> ~41 puntos guardados en la BD
                min = -1.0;
                max = 1.0;
                paso = 0.05;
                break;

            case "atan":
                // Rango continuo [-5, 5] -> ~51 puntos guardados en la BD
                min = -5.0;
                max = 5.0;
                paso = 0.2;
                break;

            case "tan":
                // 1 período completo [-180, 180] / [-π, π] -> ~73 o ~63 puntos guardados en la BD
                min = esGrados ? -180.0 : -Math.PI;
                max = esGrados ? 180.0 : Math.PI;
                paso = esGrados ? 5.0 : 0.1;
                break;

            case "sin":
            case "cos":
            default:
                // 2 ondas completas [-360, 360] / [-2π, 2π] -> ~73 o ~63 puntos guardados en la BD
                min = esGrados ? -360.0 : -2 * Math.PI;
                max = esGrados ? 360.0 : 2 * Math.PI;
                paso = esGrados ? 10.0 : 0.2;
                break;
        }
        return new ParametrosGrafica(funcion, modo, min, max, paso);
    }
}