package com.calculadora.dao;

import com.calculadora.dto.CalculoDTO;
import com.calculadora.integracion.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalculoDAOImpl implements CalculoDAO {

    @Override
    public int guardarCalculo(CalculoDTO calculo) {
        String sql = "INSERT INTO calculo (operacion_ID, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, calculo.getOperacionId());
            stmt.setDouble(2, calculo.getOperandoA());

            if (calculo.getOperandoB() != null) {
                stmt.setDouble(3, calculo.getOperandoB());
            } else {
                stmt.setNull(3, Types.REAL);
            }

            stmt.setString(4, calculo.getModoAngulo());

            if (calculo.getResultado() != null) {
                stmt.setDouble(5, calculo.getResultado());
            } else {
                stmt.setNull(5, Types.REAL);
            }

            stmt.setInt(6, calculo.isExito() ? 1 : 0);
            stmt.setString(7, calculo.getMensajeError());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1);
                        calculo.setId(idGenerado);
                        calculo.setGuardado(true);
                        return idGenerado;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar calculo: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public List<CalculoDTO> obtenerHistorial(Integer operacionId, Boolean exito) {
        List<CalculoDTO> historial = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID, operacion_ID, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error, creado_en FROM calculo WHERE 1=1");

        if (operacionId != null) {
            sql.append(" AND operacion_ID = ?");
        }
        if (exito != null) {
            sql.append(" AND exito = ?");
        }
        sql.append(" ORDER BY ID DESC");

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (operacionId != null) {
                stmt.setInt(paramIndex++, operacionId);
            }
            if (exito != null) {
                stmt.setInt(paramIndex++, exito ? 1 : 0);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                while (rs.next()) {
                    CalculoDTO dto = new CalculoDTO();
                    dto.setId(rs.getInt("ID"));
                    dto.setOperacionId(rs.getInt("operacion_ID"));
                    dto.setOperandoA(rs.getDouble("operando_a"));

                    double opB = rs.getDouble("operando_b");
                    dto.setOperandoB(rs.wasNull() ? null : opB);

                    dto.setModoAngulo(rs.getString("modo_angulo"));

                    double res = rs.getDouble("resultado");
                    dto.setResultado(rs.wasNull() ? null : res);

                    dto.setExito(rs.getInt("exito") == 1);
                    dto.setMensajeError(rs.getString("mensaje_error"));
                    
                    String fechaStr = rs.getString("creado_en");
                    if (fechaStr != null) {
                        dto.setCreadoEn(LocalDateTime.parse(fechaStr, formatter));
                    }

                    historial.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar historial: " + e.getMessage());
        }
        return historial;
    }
}