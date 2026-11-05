package com.calculadora.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/** Tema visual sin dependencias externas ni cambios en los cálculos. */
final class EstiloCalculadora {
    private static final Color VERDE = new Color(0xB5E43B);
    private static final Color OSCURO = new Color(0x25292D);

    private EstiloCalculadora() { }

    static void aplicar(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof AbstractButton boton) {
                boton.setOpaque(false);
                boton.setContentAreaFilled(false);
                boton.setBorderPainted(false);
                boton.setFocusPainted(false);
                boton.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
                boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                boton.setRolloverEnabled(true);
                boton.setUI(new BasicButtonUI() {
                    @Override public void paint(Graphics graphics, JComponent c) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        boolean seleccionado = boton instanceof JToggleButton && boton.isSelected();
                        Color fondo = seleccionado ? VERDE : boton.getBackground();
                        if (boton.getModel().isPressed()) fondo = fondo.darker();
                        else if (boton.getModel().isRollover()) fondo = fondo.brighter();
                        g.setColor(fondo);
                        g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 14, 14);
                        if (Boolean.TRUE.equals(boton.getClientProperty("accionGrafica"))) {
                            g.setColor(VERDE);
                            g.setStroke(new BasicStroke(1.5f));
                            g.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 14, 14);
                        }
                        if (boton.hasFocus()) {
                            g.setColor(Color.WHITE);
                            g.setStroke(new BasicStroke(1.5f));
                            g.drawRoundRect(4, 4, c.getWidth() - 9, c.getHeight() - 9, 10, 10);
                        }
                        g.dispose();
                        super.paint(graphics, c);
                    }
                });
                if (boton instanceof JToggleButton) {
                    Color normal = boton.getForeground();
                    boton.addItemListener(e -> boton.setForeground(boton.isSelected() ? OSCURO : normal));
                }
            } else if (componente instanceof JTextField campo) {
                campo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                campo.setCaretColor(Color.WHITE);
                campo.setSelectionColor(new Color(0x52613A));
            }
            if (componente instanceof Container hijo) aplicar(hijo);
        }
    }
}
