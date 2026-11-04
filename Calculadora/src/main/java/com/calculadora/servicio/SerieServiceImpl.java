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

    public int generarSerie(String funcion, double xmin, double xmax, double paso, String modo) throws Exception {
        com.calculadora.dto.OperacionDTO operacion = new com.calculadora.dao.OperacionDAOImpl().buscarPorSimbolo(funcion);
        if (operacion == null) {
            throw new IllegalArgumentException("La función no está disponible en el catálogo: " + funcion);
        }
        return generarSerie(operacion.getId(), funcion, funcion, xmin, xmax, paso, modo);
    }

    public boolean tienePuntosIndefinidos(int serieId) {
        return serieDAO.obtenerPuntos(serieId).stream().anyMatch(p -> p.getY() == null);
    }

    @Override
    public boolean validarParametros(double xmin, double xmax, double paso) {
        double intervalos = (xmax - xmin) / paso;
        return Double.isFinite(xmin) && Double.isFinite(xmax) && Double.isFinite(paso)
                && xmin < xmax && paso > 0 && paso <= xmax - xmin
                && xmin + paso > xmin && xmax - paso < xmax
                && Double.isFinite(intervalos) && intervalos <= 9999;
    }

    @Override
    public int generarSerie(int operacionId, String funcion, String nombre, double xmin, double xmax, double paso, String modo) throws Exception {
        if (!validarParametros(xmin, xmax, paso)) {
            throw new IllegalArgumentException("Use números finitos, mínimo menor que máximo y un paso positivo que genere entre 2 y 10000 puntos.");
        }

        if (!java.util.Set.of("sin", "cos", "tan", "asin", "acos", "atan").contains(funcion)
                || !("grados".equals(modo) || "radianes".equals(modo))) {
            throw new IllegalArgumentException("Seleccione una función trigonométrica y grados o radianes.");
        }
        if (("asin".equals(funcion) || "acos".equals(funcion)) && (xmin < -1 || xmax > 1)) {
            throw new IllegalArgumentException("El rango de arcoseno y arcocoseno debe estar entre -1 y 1.");
        }
        // 1. Obtener puntos ejecutando Trig.py a través de PythonBridge
        List<String> lineasSalida = pythonBridge.generarSerie(funcion, xmin, xmax, paso, modo);
        
        // 2. Convertir la respuesta de texto en DTOs
        List<PuntoDTO> puntos = convertirPuntos(lineasSalida);
        
        if (puntos.isEmpty() || puntos.stream().allMatch(p -> p.getY() == null)) {
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
            String[] partes = linea.split(";", -1);
            if (partes.length != 2) {
                throw new IllegalArgumentException("Python devolvió un punto malformado: " + linea);
            }
            try {
                double x = Double.parseDouble(partes[0].trim());
                String valorY = partes[1].trim();
                Double y = valorY.isEmpty() || valorY.equalsIgnoreCase("None")
                        ? null : Double.valueOf(valorY);
                if (!Double.isFinite(x) || (y != null && !Double.isFinite(y))) {
                    throw new NumberFormatException("Punto no finito");
                }
                if (!puntos.isEmpty() && x <= puntos.get(puntos.size() - 1).getX()) {
                    throw new NumberFormatException("Los valores de x deben aumentar");
                }
                puntos.add(new PuntoDTO(x, y));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Python devolvió un punto inválido: " + linea, e);
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