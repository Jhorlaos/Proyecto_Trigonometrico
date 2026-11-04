package com.calculadora.integracion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PythonBridge {
    private final String pythonExecutable = System.getProperty("calculadora.python", Files.isRegularFile(Path.of("../.venv/Scripts/python.exe")) ? Path.of("../.venv/Scripts/python.exe").toAbsolutePath().normalize().toString() : "py");
    private final String rutaTrig = "../python_scripts/Trig.py";
    private final String rutaGraficar = "../python_scripts/Graficar.py";

    public String calcularTrig(String funcion, double valor, String modo) throws Exception {
        return ejecutar(rutaTrig, funcion, String.valueOf(valor), "--modo", modo).get(0);
    }
    public List<String> generarSerie(String funcion, double xmin, double xmax, double paso, String modo) throws Exception {
        return ejecutar(rutaTrig, "serie", funcion, String.valueOf(xmin), String.valueOf(xmax), String.valueOf(paso), "--modo", modo);
    }
    public boolean generarGrafica(String dbPath, int serieId, String salidaPng) throws Exception {
        return ejecutar(rutaGraficar, dbPath, String.valueOf(serieId), salidaPng).get(0).startsWith("OK;");
    }
    private List<String> ejecutar(String script, String... argumentos) throws Exception {
        List<String> comando = new ArrayList<>(List.of(pythonExecutable, "-B", "-X", "utf8", script));
        comando.addAll(List.of(argumentos));
        Path salida = Files.createTempFile("calculadora-python-", ".txt");
        Path errores = Files.createTempFile("calculadora-python-error-", ".txt");
        Process proceso = null;
        try {
            // Archivos temporales evitan bloquear el proceso por una tubería llena.
            proceso = new ProcessBuilder(comando).redirectOutput(salida.toFile()).redirectError(errores.toFile()).start();
            if (!proceso.waitFor(60, TimeUnit.SECONDS)) throw new IllegalStateException("Python tardó más de 60 segundos. Reduce el rango o aumenta el paso.");
            List<String> lineas = Files.readAllLines(salida, StandardCharsets.UTF_8);
            String error = Files.readString(errores, StandardCharsets.UTF_8).trim();
            String respuesta = String.join("\n", lineas);
            if (proceso.exitValue() != 0 || lineas.isEmpty() || lineas.stream().anyMatch(l -> l.startsWith("ERROR;"))) {
                String detalle = !respuesta.isBlank() ? respuesta : error;
                if (detalle.length() > 1500) detalle = detalle.substring(0, 1500);
                throw new IllegalStateException("Error de Python: " + (detalle.isBlank() ? "No devolvió una respuesta." : detalle));
            }
            return lineas;
        } finally {
            if (proceso != null && proceso.isAlive()) { proceso.destroyForcibly(); proceso.waitFor(5, TimeUnit.SECONDS); }
            Files.deleteIfExists(salida); Files.deleteIfExists(errores);
        }
    }
}
