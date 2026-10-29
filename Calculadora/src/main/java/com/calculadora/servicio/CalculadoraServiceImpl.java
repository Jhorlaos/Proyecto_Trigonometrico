package com.calculadora.servicio;

import com.calculadora.dao.CalculoDAO;
import com.calculadora.dao.CalculoDAOImpl;
import com.calculadora.dao.OperacionDAO;
import com.calculadora.dao.OperacionDAOImpl;
import com.calculadora.dominio.Operacion;
import com.calculadora.dominio.OperacionTrigonometrica;
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
        // 1. Buscar la operación por símbolo (insensible a mayúsculas/minúsculas gracias a LOWER)
        OperacionDTO opDTO = operacionDAO.buscarPorSimbolo(operacion.getSimbolo());

        if (opDTO == null) {
            throw new CalculadoraException("La operación '" + operacion.getSimbolo() + "' no se encuentra en la base de datos.");
        }

        int idOperacion = opDTO.getId();

        // 2. Preparar el DTO para el historial
        CalculoDTO registro = new CalculoDTO();
        registro.setOperacionId(idOperacion);
        registro.setOperandoA(operandos[0]);
        if (operandos.length > 1) {
            registro.setOperandoB(operandos[1]);
        }

        // 3. Registrar el modo de ángulo
        if (operacion instanceof OperacionTrigonometrica) {
            OperacionTrigonometrica opTrig = (OperacionTrigonometrica) operacion;
            registro.setModoAngulo(opTrig.getModoAngulo());
        } else {
            registro.setModoAngulo(null);
        }

        try {
            // 4. Ejecutar el cálculo puro
            double resultado = operacion.calcular(operandos);
            
            // 5. Guardar éxito en SQLite
            registro.setResultado(resultado);
            registro.setExito(true);
            registro.setMensajeError(null);
            
            calculoDAO.guardarCalculo(registro);
            return resultado;
            
        } catch (Exception e) {
            // 6. Guardar error en SQLite antes de relanzar la excepción a la UI
            registro.setResultado(null);
            registro.setExito(false);
            registro.setMensajeError(e.getMessage());
            
            calculoDAO.guardarCalculo(registro); // Al tener idOperacion válido, el INSERT pasará sin fallos de Foreign Key
            
            if (e instanceof CalculadoraException) {
                throw (CalculadoraException) e;
            } else {
                throw new CalculadoraException(e.getMessage());
            }
        }
    }

    @Override
    public List<CalculoDTO> obtenerHistorial() throws CalculadoraException {
        try {
            return calculoDAO.obtenerHistorial(null, null);
        } catch (Exception e) {
            throw new CalculadoraException("Error al consultar el historial: " + e.getMessage());
        }
    }
}