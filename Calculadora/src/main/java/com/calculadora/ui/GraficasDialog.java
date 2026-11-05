package com.calculadora.ui;

import com.calculadora.servicio.GestorGraficas;
import com.calculadora.servicio.ParametrosGrafica;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import javax.swing.*;

/** Ventana independiente para conservar intacto el formulario de NetBeans. */
public final class GraficasDialog extends JDialog {
    private final JComboBox<String> funcion = new JComboBox<>(new String[]{"sin", "cos", "tan", "asin", "acos", "atan"});
    private final JComboBox<String> modo = new JComboBox<>(new String[]{"grados", "radianes"});
    private final JTextField minimo = new JTextField(), maximo = new JTextField(), paso = new JTextField();
    private final JLabel ayuda = new JLabel(), estado = new JLabel("Ingresa los datos o usa 'Rango recomendado'.");
    private final JLabel imagen = new JLabel("La gráfica aparecerá aquí cuando pulses Generar gráfica", SwingConstants.CENTER);
    private final JButton generar = new JButton("Generar gráfica"), cerrar = new JButton("Cerrar"), restablecer = new JButton("Rango recomendado");
    private final GestorGraficas gestor;
    private boolean gestorPropio;
    private boolean trabajando;

    public GraficasDialog(JFrame padre, String inicial, String unidad) {
        this(padre, inicial, unidad, new GestorGraficas(), false);
        gestorPropio = true;
    }

    public GraficasDialog(JFrame padre, String inicial, String unidad, GestorGraficas gestor, boolean automatica) {
        super(padre, "Graficar función", true);
        this.gestor = gestor;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });

        if (java.util.Set.of("sin", "cos", "tan", "asin", "acos", "atan").contains(inicial)) {
            funcion.setSelectedItem(inicial);
        }
        modo.setSelectedItem(unidad);

        // 1. Los campos inician en blanco para que el usuario ingrese sus propios puntos
        minimo.setText("");
        maximo.setText("");
        paso.setText("");

        JPanel campos = new JPanel(new GridLayout(0, 2, 12, 8));
        campo(campos, "Función f(x)", funcion); 
        campo(campos, "Unidad angular", modo);
        campo(campos, "Desde x =", minimo); 
        campo(campos, "Hasta x =", maximo); 
        campo(campos, "Paso entre puntos", paso);

        JPanel superior = new JPanel(new BorderLayout(0, 10));
        superior.add(new JLabel("Se grafica una función en un intervalo, no un resultado aislado."), BorderLayout.NORTH);
        superior.add(campos, BorderLayout.CENTER); 
        superior.add(ayuda, BorderLayout.SOUTH);

        JPanel botones = new JPanel(); 
        botones.add(restablecer); 
        botones.add(generar); 
        botones.add(cerrar);

        JPanel inferior = new JPanel(new BorderLayout(0, 8));
        inferior.add(estado, BorderLayout.CENTER); 
        inferior.add(botones, BorderLayout.SOUTH);

        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contenido.add(superior, BorderLayout.NORTH);

        JScrollPane vista = new JScrollPane(imagen); 
        vista.setPreferredSize(new Dimension(840, 430));
        contenido.add(vista, BorderLayout.CENTER); 
        contenido.add(inferior, BorderLayout.SOUTH);

        setContentPane(contenido);

        funcion.addActionListener(e -> actualizarAyudaYValores()); 
        modo.addActionListener(e -> actualizarAyudaYValores());

        restablecer.setToolTipText("Rellena las cajas con el rango ideal recomendado.");
        restablecer.addActionListener(e -> aplicarRecomendados()); 

        cerrar.addActionListener(e -> dispose());
        generar.addActionListener(e -> generar()); 

        // 2. Solo actualizamos ayuda visual, SIN auto-generar ni poblar campos por defecto
        actualizarAyudaYValores(); 
        getRootPane().setDefaultButton(generar);

        pack(); 
        setMinimumSize(new Dimension(680, 520)); 
        setLocationRelativeTo(padre);

        // SE ELIMINÓ: if (automatica) generar(); -> Evita ejecutar Python y SQLite al abrir.
    }

    private static void campo(JPanel panel, String texto, JComponent entrada) {
        JLabel etiqueta = new JLabel(texto); 
        etiqueta.setLabelFor(entrada); 
        panel.add(etiqueta); 
        panel.add(entrada);
    }

    /** Carga explícitamente los parámetros recomendados solo si el usuario pulsa el botón 'Rango recomendado'. */
    private void aplicarRecomendados() {
        String fn = (String) funcion.getSelectedItem();
        String m = (String) modo.getSelectedItem();
        if (fn == null || m == null) return;

        ParametrosGrafica recomendados = ParametrosGrafica.recomendados(fn, m);

        minimo.setText(formatearNumero(recomendados.minimo()));
        maximo.setText(formatearNumero(recomendados.maximo()));
        paso.setText(formatearNumero(recomendados.paso()));

        actualizarAyudaYValores();
        estado.setText("Rango recomendado cargado: " + minimo.getText() + " a " + maximo.getText()
                + ", paso " + paso.getText() + ". Pulsa Generar gráfica.");
    }

    /** Actualiza los mensajes contextuales sin alterar lo que el usuario haya escrito en los campos. */
    private void actualizarAyudaYValores() {
        String fn = (String) funcion.getSelectedItem();
        if (fn == null) return;

        boolean inversa = fn.startsWith("a");
        ayuda.setText(inversa ? "x es un valor; y es un ángulo. asin y acos requieren x entre -1 y 1."
                : "x es un ángulo. La tangente tiene discontinuidades: sus ramas se separarán.");
    }

    private static String formatearNumero(double v) {
        if (v == (long) v) {
            return String.format(Locale.US, "%d", (long) v);
        }
        return String.format(Locale.US, "%.4f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /** Extrae los números garantizando que ningún campo quede en blanco. */
    private static double numero(JTextField campo, String nombreCampo) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' está vacío. Ingresa un valor numérico.");
        }
        try {
            double n = Double.parseDouble(texto.replace(',', '.'));
            if (!Double.isFinite(n)) {
                throw new IllegalArgumentException("El campo '" + nombreCampo + "' debe ser un número finito.");
            }
            return n;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' contiene un valor no numérico.");
        }
    }

    private void habilitar(boolean disponible) {
        trabajando = !disponible;
        for (JComponent c : new JComponent[]{funcion, modo, minimo, maximo, paso, generar, restablecer}) {
            c.setEnabled(disponible);
        }
    }

    private void generar() {
        final ParametrosGrafica parametros;
        try {
            // Se leen estrictamente las entradas actuales del usuario
            double minVal = numero(minimo, "Desde x (mínimo)");
            double maxVal = numero(maximo, "Hasta x (máximo)");
            double pasoVal = numero(paso, "Paso entre puntos");

            if (pasoVal <= 0) {
                throw new IllegalArgumentException("El paso entre puntos debe ser mayor que cero (0).");
            }
            if (minVal >= maxVal) {
                throw new IllegalArgumentException("El valor 'Desde x' debe ser menor que 'Hasta x'.");
            }

            parametros = new ParametrosGrafica(
                (String) funcion.getSelectedItem(),
                (String) modo.getSelectedItem(),
                minVal, maxVal, pasoVal
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Campos requeridos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        imagen.setIcon(null);
        imagen.setText("Preparando…");
        estado.setText("Enviando rango a Python e insertando puntos en la base de datos…");
        habilitar(false);

        gestor.preparar(parametros).whenComplete((resultado, error) -> SwingUtilities.invokeLater(() -> {
            if (!isDisplayable()) return;
            habilitar(true);
            if (error != null) {
                fallo(error.getMessage());
                return;
            }
            imagen.setText(null);
            imagen.setIcon(new ImageIcon(resultado.imagen()));
            estado.setText(resultado.indefinidos()
                    ? "Serie #" + resultado.serieId() + ": contiene puntos indefinidos."
                    : "Serie #" + resultado.serieId() + " desplegada correctamente.");
            estado.setToolTipText("PNG guardado en: " + resultado.archivo());
        }));
    }

    @Override public void dispose() {
        if (gestorPropio) gestor.close();
        super.dispose();
    }

    private void fallo(String mensaje) {
        imagen.setText("No se pudo generar la gráfica"); 
        estado.setText("Revisa los parámetros e inténtalo de nuevo.");
        JOptionPane.showMessageDialog(this, mensaje, "Error al graficar", JOptionPane.ERROR_MESSAGE);
    }
}