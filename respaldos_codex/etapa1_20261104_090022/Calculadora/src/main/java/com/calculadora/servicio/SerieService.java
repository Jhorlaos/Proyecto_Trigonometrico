package com.calculadora.servicio;

import com.calculadora.dto.PuntoDTO;
import java.util.List;

public interface SerieService {

    boolean validarParametros(double xmin, double xmax, double paso);

    int generarSerie(int operacionId, String funcion, String nombre, double xmin, double xmax, double paso, String modo) throws Exception;

    List<PuntoDTO> convertirPuntos(List<String> lineas);

    boolean generarGrafica(int serieId, String rutaSalidaPNG) throws Exception;
}