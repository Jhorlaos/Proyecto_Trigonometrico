package com.calculadora.servicio;

import com.calculadora.dominio.Operacion;

public interface CalculadoraService {

    double ejecutarYGuardar(Operacion operacion, double[] operandos);
    
}