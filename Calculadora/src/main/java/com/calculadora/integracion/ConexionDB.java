package com.calculadora.integracion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionDB {

    // Ruta absoluta a tu base de datos SQLite
    private static final String URL = "jdbc:sqlite:C:/Users/Esteban/Desktop/Proyecto_Trigonometrico/db/calculadora.db";
    private static Connection conexion = null;

    private ConexionDB() {} // Constructor privado (Patrón Singleton)

    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(URL);
            
            // Habilitar el soporte de llaves foráneas en SQLite
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return conexion;
    }

    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
