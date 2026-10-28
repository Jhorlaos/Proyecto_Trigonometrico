/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calculadora.dao;

import com.calculadora.dto.OperacionDTO;
import com.calculadora.integracion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OperacionDAOImpl implements OperacionDAO {

    @Override
    public OperacionDTO buscarPorSimbolo(String simbolo) {
        String sql = "SELECT ID, simbolo, nombre, tipo, lenguaje, aridad FROM operacion WHERE simbolo = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, simbolo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new OperacionDTO(
                        rs.getInt("ID"),
                        rs.getString("simbolo"),
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getString("lenguaje"),
                        rs.getInt("aridad")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar operacion por simbolo: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<OperacionDTO> listarOperaciones() {
        List<OperacionDTO> lista = new ArrayList<>();
        String sql = "SELECT ID, simbolo, nombre, tipo, lenguaje, aridad FROM operacion";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                OperacionDTO dto = new OperacionDTO(
                    rs.getInt("ID"),
                    rs.getString("simbolo"),
                    rs.getString("nombre"),
                    rs.getString("tipo"),
                    rs.getString("lenguaje"),
                    rs.getInt("aridad")
                );
                lista.add(dto);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar operaciones: " + e.getMessage());
        }

        return lista;
    }
}