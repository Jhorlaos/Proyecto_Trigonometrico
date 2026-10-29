package com.calculadora.integracion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PythonBridge {

    // Comando 'py' para Windows y rutas relativas portables hacia los scripts
    private final String pythonExecutable = "py";
    private final String rutaTrig = "../python_scripts/Trig.py";
    private final String rutaGraficar = "../python_scripts/Graficar.py";

    /**
     * Realiza un cálculo puntual (ej. sin(30)) ejecutando Trig.py
     */
    public String calcularTrig(String funcion, double valor, String modo) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            pythonExecutable, 
            rutaTrig, 
            funcion, 
            String.valueOf(valor), 
            "--modo", 
            modo
        );
        return ejecutarProcesoUnaLinea(pb);
    }

    /**
     * Genera una serie de puntos (x;y) ejecutando Trig.py serie ...
     */
    public List<String> generarSerie(String funcion, double xmin, double xmax, double paso, String modo) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            pythonExecutable, 
            rutaTrig, 
            "serie", 
            funcion, 
            String.valueOf(xmin), 
            String.valueOf(xmax), 
            String.valueOf(paso), 
            "--modo", 
            modo
        );
        return ejecutarProcesoMultiplesLineas(pb);
    }

    /**
     * Genera el archivo de imagen PNG invocando Graficar.py
     */
    public boolean generarGrafica(String dbPath, int serieId, String salidaPng) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            pythonExecutable, 
            rutaGraficar, 
            dbPath, 
            String.valueOf(serieId), 
            salidaPng
        );
        String respuesta = ejecutarProcesoUnaLinea(pb);
        return respuesta != null && respuesta.startsWith("OK;");
    }

    // --- MÉTODOS AUXILIARES ---

    private String ejecutarProcesoUnaLinea(ProcessBuilder pb) throws Exception {
        pb.redirectErrorStream(true);
        Process proceso = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()))) {
            String linea = reader.readLine();
            proceso.waitFor();
            return linea;
        }
    }

    private List<String> ejecutarProcesoMultiplesLineas(ProcessBuilder pb) throws Exception {
        pb.redirectErrorStream(true);
        Process proceso = pb.start();
        List<String> lineas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                lineas.add(linea);
            }
            proceso.waitFor();
        }
        return lineas;
    }
}