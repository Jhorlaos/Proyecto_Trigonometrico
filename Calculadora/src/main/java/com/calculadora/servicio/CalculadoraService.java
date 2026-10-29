package com.calculadora.servicio;

import com.calculadora.dominio.Operacion;
import com.calculadora.dto.CalculoDTO;
import com.calculadora.excepciones.CalculadoraException;
import java.util.List;

public interface CalculadoraService {

    double ejecutarYGuardar(Operacion operacion, double[] operandos) throws CalculadoraException;
    
    List<CalculoDTO> obtenerHistorial() throws CalculadoraException;
    
}