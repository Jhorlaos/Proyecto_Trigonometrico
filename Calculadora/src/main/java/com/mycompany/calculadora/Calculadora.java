package com.mycompany.calculadora;

import com.calculadora.ui.Calculator;
import javax.swing.SwingUtilities;

public class Calculadora {

    public static void main(String[] args) {
        // Ejecutamos la interfaz gráfica en el hilo de eventos de Swing (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            Calculator ventana = new Calculator();
            ventana.setLocationRelativeTo(null); // Centra la ventana en la pantalla
            ventana.setVisible(true);            // Muestra la interfaz gráfica
        });
    }
}