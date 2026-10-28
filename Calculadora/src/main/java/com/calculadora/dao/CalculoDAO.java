package com.calculadora.dao;

import com.calculadora.dto.CalculoDTO;
import java.util.List;

public interface CalculoDAO {

    int guardarCalculo(CalculoDTO calculo);

    List<CalculoDTO> obtenerHistorial(Integer operacionId, Boolean exito);
}