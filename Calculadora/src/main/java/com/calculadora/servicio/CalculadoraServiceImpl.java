package com.calculadora.servicio;

import com.calculadora.dao.CalculoDAO;
import com.calculadora.dao.CalculoDAOImpl;
import com.calculadora.dao.OperacionDAO;
import com.calculadora.dao.OperacionDAOImpl;
import com.calculadora.dominio.Operacion;
import com.calculadora.dto.CalculoDTO;
import com.calculadora.dto.OperacionDTO;
import com.calculadora.excepciones.CalculadoraException;
import java.util.List;

public class CalculadoraServiceImpl implements CalculadoraService {

    private final OperacionDAO operacionDAO;
    private final CalculoDAO calculoDAO;

    public CalculadoraServiceImpl() {
        this.operacionDAO = new OperacionDAOImpl();
        this.calculoDAO = new CalculoDAOImpl();
    }

    @Override
    public double ejecutarYGuardar(Operacion operacion, double[] operandos) throws CalculadoraException {
        // 1. Obtener el ID de la operación desde la base de datos
        OperacionDTO opDTO = operacionDAO.buscarPorSimbolo(operacion.getSimbolo());
        int idOperacion = (opDTO != null) ? opDTO.getId() : -1;

        // 2. Preparar el objeto DTO para guardar el historial
        CalculoDTO registro = new CalculoDTO();
        registro.setOperacionId(idOperacion);
        registro.setOperandoA(operandos[0]);
        if (operandos.length > 1) {
            registro.setOperandoB(operandos[1]);
        }

        try {
            // 3. Ejecutar el cálculo matemático puro (Capa de Dominio)
            double resultado = operacion.calcular(operandos);
            
            // 4. Si sale bien, completar el registro de éxito
            registro.setResultado(resultado);
            registro.setExito(true);
            
            calculoDAO.guardarCalculo(registro); // Guardar en BD
            return resultado;
            
        } catch (CalculadoraException e) {
            // 5. Si falla (ej. División por cero), registrar el error
            registro.setExito(false);
            registro.setMensajeError(e.getMessage());
            
            calculoDAO.guardarCalculo(registro); // Guardar el fallo en BD
            throw e; // Relanzar la excepción para que la UI sepa que ocurrió un error
        }
    }

    @Override
    public List<CalculoDTO> obtenerHistorial() throws CalculadoraException {
        try {
            // Pasamos (null, null) para traer todos los cálculos sin filtrar por ID de operación ni por éxito
            return calculoDAO.obtenerHistorial(null, null);
        } catch (Exception e) {
            throw new CalculadoraException("Error al consultar el historial desde el servicio: " + e.getMessage());
        }
    }
}