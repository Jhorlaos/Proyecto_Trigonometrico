package com.calculadora.excepciones;

public class PythonEjecucionException extends CalculadoraException {

    public PythonEjecucionException(String mensaje) {
        super(mensaje);
    }

    public PythonEjecucionException(String mensaje, Throwable causa) {
        super(mensaje);
    }
}