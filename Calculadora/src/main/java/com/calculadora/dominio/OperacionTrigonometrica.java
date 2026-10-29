package com.calculadora.dominio;

import com.calculadora.integracion.PythonBridge;
import com.calculadora.excepciones.CalculadoraException;
import com.calculadora.excepciones.PythonEjecucionException;

public class OperacionTrigonometrica implements Operacion {

    private String simbolo;
    private String nombre;
    private String modoAngulo;
    private PythonBridge pythonBridge;

    public OperacionTrigonometrica(String simbolo, String nombre) {
        this.simbolo = simbolo;
        this.nombre = nombre;
        this.modoAngulo = "grados";
        this.pythonBridge = new PythonBridge();
    }

    @Override
    public String getSimbolo() {
        return simbolo;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public int getAridad() {
        return 1;
    }

    public String getModoAngulo() {
        return modoAngulo;
    }

    public void setModoAngulo(String modoAngulo) {
        this.modoAngulo = modoAngulo;
    }

    @Override
    public double calcular(double[] operandos) throws CalculadoraException {
        if (operandos == null || operandos.length < 1) {
            throw new PythonEjecucionException("Se requiere un operando para la función trigonométrica.");
        }

        double valor = operandos[0];

        try {
            // Invocar a Python mediante el Bridge
            String resultadoPython = pythonBridge.calcularTrig(simbolo, valor, modoAngulo);

            if (resultadoPython != null && resultadoPython.startsWith("OK;")) {
                String valorResultante = resultadoPython.split(";")[1];
                return Double.parseDouble(valorResultante);
                
            } else if (resultadoPython != null && resultadoPython.startsWith("ERROR;")) {
                String mensajeError = resultadoPython.split(";")[1];
                throw new PythonEjecucionException(mensajeError); 
                
            } else {
                throw new PythonEjecucionException("Respuesta desconocida o malformada del script de Python.");
            }

        } catch (PythonEjecucionException e) {
            throw e;
        } catch (Exception e) {
            throw new PythonEjecucionException("Fallo en la ejecución con Python: " + e.getMessage());
        }
    }
}