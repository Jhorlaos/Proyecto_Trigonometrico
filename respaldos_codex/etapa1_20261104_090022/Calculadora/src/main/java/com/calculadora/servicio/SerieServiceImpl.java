package com.calculadora.servicio;

import com.calculadora.dao.SerieDAO;
import com.calculadora.dao.SerieDAOImpl;
import com.calculadora.dto.PuntoDTO;
import com.calculadora.dto.SerieDTO;
import com.calculadora.integracion.PythonBridge;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SerieServiceImpl implements SerieService {

    private final SerieDAO serieDAO;
    private final PythonBridge pythonBridge;
    private final String dbPath = "../db/calculadora.db";

    public SerieServiceImpl() {
        this.serieDAO = new SerieDAOImpl();
        this.pythonBridge = new PythonBridge();
    }

    @Override
    public boolean validarParametros(double xmin, double xmax, double paso) {
        return xmin < xmax && paso > 0;
    }

    @Override
    public int generarSerie(int operacionId, String funcion, String nombre, double xmin, double xmax, double paso, String modo) throws Exception {
        if (!validarParametros(xmin, xmax, paso)) {
            throw new IllegalArgumentException("Parámetros inválidos: xmin debe ser menor a xmax, y el paso mayor a 0.");
        }

        // 1. Obtener puntos ejecutando Trig.py a través de PythonBridge
        List<String> lineasSalida = pythonBridge.generarSerie(funcion, xmin, xmax, paso, modo);
        
        // 2. Convertir la respuesta de texto en DTOs
        List<PuntoDTO> puntos = convertirPuntos(lineasSalida);
        
        if (puntos.isEmpty()) {
            throw new Exception("Python no generó ningún punto válido para la serie.");
        }

        // 3. Crear DTO de la Serie
        SerieDTO serie = new SerieDTO();
        serie.setOperacionId(operacionId);
        serie.setNombre(nombre);
        serie.setxMin(xmin);
        serie.setxMax(xmax);
        serie.setPaso(paso);
        serie.setModoAngulo(modo);
        serie.setCreadoEn(LocalDateTime.now());

        // 4. Persistir de forma transaccional usando SerieDAO
        return serieDAO.guardarSerieConPuntos(serie, puntos);
    }

    @Override
    public List<PuntoDTO> convertirPuntos(List<String> lineas) {
        List<PuntoDTO> puntos = new ArrayList<>();
        for (String linea : lineas) {
            String[] partes = linea.split(";");
            if (partes.length == 2) {
                try {
                    double x = Double.parseDouble(partes[0].trim());
                    Double y = partes[1].trim().equalsIgnoreCase("None") ? null : Double.parseDouble(partes[1].trim());
                    puntos.add(new PuntoDTO(x, y));
                } catch (NumberFormatException e) {
                    System.err.println("Advertencia: No se pudo parsear la línea: " + linea);
                }
            }
        }
        return puntos;
    }

    @Override
    public boolean generarGrafica(int serieId, String rutaSalidaPNG) throws Exception {
        File archivo = new File(rutaSalidaPNG);
        if (archivo.getParentFile() != null) {
            archivo.getParentFile().mkdirs();
        }
        return pythonBridge.generarGrafica(dbPath, serieId, rutaSalidaPNG);
    }
}