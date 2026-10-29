package com.calculadora.dao;

import com.calculadora.dto.PuntoDTO;
import com.calculadora.dto.SerieDTO;
import java.util.List;

public interface SerieDAO {
    int guardarSerieConPuntos(SerieDTO serie, List<PuntoDTO> puntos);
    List<PuntoDTO> obtenerPuntos(int serieId);
}