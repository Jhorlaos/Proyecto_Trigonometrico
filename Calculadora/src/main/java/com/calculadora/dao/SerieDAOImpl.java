package com.calculadora.dao;

import com.calculadora.dto.PuntoDTO;
import com.calculadora.dto.SerieDTO;
import com.calculadora.integracion.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SerieDAOImpl implements SerieDAO {

    @Override
    public int guardarSerieConPuntos(SerieDTO serie, List<PuntoDTO> puntos) {
        String sqlSerie = "INSERT INTO serie (operacion_id, nombre, x_min, x_max, paso, modo_angulo) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlPunto = "INSERT INTO punto (serie_id, x, y) VALUES (?, ?, ?)";
        
        Connection conn = null;
        int serieIdGenerado = -1;

        try {
            conn = ConexionDB.getConexion();
            // Iniciar transacción (desactivar auto-commit)
            conn.setAutoCommit(false); 

            // 1. Insertar la Serie
            try (PreparedStatement stmtSerie = conn.prepareStatement(sqlSerie, Statement.RETURN_GENERATED_KEYS)) {
                stmtSerie.setInt(1, serie.getOperacionId());
                stmtSerie.setString(2, serie.getNombre());
                stmtSerie.setDouble(3, serie.getxMin());
                stmtSerie.setDouble(4, serie.getxMax());
                stmtSerie.setDouble(5, serie.getPaso());
                stmtSerie.setString(6, serie.getModoAngulo());
                stmtSerie.executeUpdate();

                // Recuperar el ID generado para la serie
                try (ResultSet rs = stmtSerie.getGeneratedKeys()) {
                    if (rs.next()) {
                        serieIdGenerado = rs.getInt(1);
                        serie.setId(serieIdGenerado);
                    }
                }
            }

            // 2. Insertar los Puntos en lote (Batch)
            if (serieIdGenerado != -1 && puntos != null && !puntos.isEmpty()) {
                try (PreparedStatement stmtPunto = conn.prepareStatement(sqlPunto)) {
                    for (PuntoDTO punto : puntos) {
                        stmtPunto.setInt(1, serieIdGenerado);
                        stmtPunto.setDouble(2, punto.getX());
                        
                        if (punto.getY() == null) {
                            stmtPunto.setNull(3, java.sql.Types.DOUBLE);
                        } else {
                            stmtPunto.setDouble(3, punto.getY());
                        }
                        stmtPunto.addBatch();
                    }
                    stmtPunto.executeBatch();
                }
            }

            // Confirmar transacción
            conn.commit(); 

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    // Revertir cambios si hay error
                    conn.rollback(); 
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Error al guardar serie y puntos: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar comportamiento por defecto
                } catch (SQLException e) {
                    System.err.println("Error al restaurar autocommit: " + e.getMessage());
                }
            }
        }
        
        return serieIdGenerado;
    }

    @Override
    public List<PuntoDTO> obtenerPuntos(int serieId) {
        List<PuntoDTO> puntos = new ArrayList<>();
        String sql = "SELECT id, x, y FROM punto WHERE serie_id = ? ORDER BY x ASC";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, serieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double x = rs.getDouble("x");
                    // Manejar valores NULL en la columna y
                    Double y = rs.getObject("y") != null ? rs.getDouble("y") : null;
                    
                    PuntoDTO punto = new PuntoDTO(x, y);
                    punto.setId(rs.getInt("id"));
                    punto.setSerieId(serieId);
                    puntos.add(punto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener puntos: " + e.getMessage(), e);
        }
        return puntos;
    }
}