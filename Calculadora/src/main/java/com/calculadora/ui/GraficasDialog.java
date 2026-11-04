package com.calculadora.ui;

import com.calculadora.servicio.SerieServiceImpl;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Ventana independiente para conservar intacto el formulario de NetBeans. */
public final class GraficasDialog extends JDialog {
    private final JComboBox<String> funcion = new JComboBox<>(new String[]{"sin", "cos", "tan", "asin", "acos", "atan"});
    private final JComboBox<String> modo = new JComboBox<>(new String[]{"grados", "radianes"});
    private final JTextField minimo = new JTextField(), maximo = new JTextField(), paso = new JTextField();
    private final JLabel ayuda = new JLabel(), estado = new JLabel("Elige una función y revisa el intervalo.");
    private final JLabel imagen = new JLabel("La gráfica aparecerá aquí", SwingConstants.CENTER);
    private final JButton generar = new JButton("Generar gráfica"), cerrar = new JButton("Cerrar"), restablecer = new JButton("Rango recomendado");
    private final com.calculadora.servicio.SerieService servicio = new SerieServiceImpl();
    private boolean trabajando;

    public GraficasDialog(JFrame padre, String inicial, String unidad) {
        super(padre, "Graficar función", true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { if (!trabajando) dispose(); }
        });
        if (java.util.Set.of("sin", "cos", "tan", "asin", "acos", "atan").contains(inicial)) funcion.setSelectedItem(inicial);
        modo.setSelectedItem(unidad);
        JPanel campos = new JPanel(new GridLayout(0, 2, 12, 8));
        campo(campos, "Función f(x)", funcion); campo(campos, "Unidad angular", modo);
        campo(campos, "Desde x =", minimo); campo(campos, "Hasta x =", maximo); campo(campos, "Paso entre puntos", paso);
        JPanel superior = new JPanel(new BorderLayout(0, 10));
        superior.add(new JLabel("Se grafica una función en un intervalo, no un resultado aislado."), BorderLayout.NORTH);
        superior.add(campos, BorderLayout.CENTER); superior.add(ayuda, BorderLayout.SOUTH);
        JPanel botones = new JPanel(); botones.add(restablecer); botones.add(generar); botones.add(cerrar);
        JPanel inferior = new JPanel(new BorderLayout(0, 8));
        inferior.add(estado, BorderLayout.CENTER); inferior.add(botones, BorderLayout.SOUTH);
        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contenido.add(superior, BorderLayout.NORTH);
        JScrollPane vista = new JScrollPane(imagen); vista.setPreferredSize(new Dimension(840, 430));
        contenido.add(vista, BorderLayout.CENTER); contenido.add(inferior, BorderLayout.SOUTH);
        setContentPane(contenido);
        funcion.addActionListener(e -> recomendar()); modo.addActionListener(e -> recomendar());
        restablecer.addActionListener(e -> recomendar()); cerrar.addActionListener(e -> dispose());
        generar.addActionListener(e -> generar()); recomendar(); getRootPane().setDefaultButton(generar);
        pack(); setMinimumSize(new Dimension(680, 520)); setLocationRelativeTo(padre);
    }

    private static void campo(JPanel panel, String texto, JComponent entrada) {
        JLabel etiqueta = new JLabel(texto); etiqueta.setLabelFor(entrada); panel.add(etiqueta); panel.add(entrada);
    }

    private void recomendar() {
        String fn = (String) funcion.getSelectedItem();
        boolean inversa = fn.startsWith("a"), grados = "grados".equals(modo.getSelectedItem());
        double limite = ("asin".equals(fn) || "acos".equals(fn)) ? 1 : inversa ? 5 : grados ? 360 : 2 * Math.PI;
        minimo.setText(Double.toString(-limite)); maximo.setText(Double.toString(limite)); paso.setText(Double.toString(limite / 360));
        ayuda.setText(inversa ? "x es un valor; y es un ángulo. asin y acos requieren x entre -1 y 1."
                : "x es un ángulo. La tangente tiene discontinuidades: sus ramas se separarán.");
    }

    private static double numero(JTextField campo) {
        double n = Double.parseDouble(campo.getText().trim().replace(',', '.'));
        if (!Double.isFinite(n)) throw new NumberFormatException("Número no finito");
        return n;
    }

    private void habilitar(boolean disponible) {
        trabajando = !disponible;
        for (JComponent c : new JComponent[]{funcion, modo, minimo, maximo, paso, generar, restablecer, cerrar}) c.setEnabled(disponible);
    }

    private void generar() {
        final double xmin, xmax, incremento;
        final String fn = (String) funcion.getSelectedItem(), unidad = (String) modo.getSelectedItem();
        try {
            xmin = numero(minimo); xmax = numero(maximo); incremento = numero(paso);
            if (!servicio.validarParametros(xmin, xmax, incremento)) throw new IllegalArgumentException("Usa mínimo menor que máximo y un paso positivo para generar entre 2 y 10000 puntos.");
            if (("asin".equals(fn) || "acos".equals(fn)) && (xmin < -1 || xmax > 1)) throw new IllegalArgumentException("Para asin y acos, el intervalo debe estar entre -1 y 1.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex instanceof NumberFormatException ? "Escribe números finitos en mínimo, máximo y paso." : ex.getMessage(), "Revisa los datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        imagen.setIcon(null); imagen.setText("Generando…"); estado.setText("Generando puntos, guardando la serie y preparando la imagen…"); habilitar(false);
        new SwingWorker<Resultado, Void>() {
            @Override protected Resultado doInBackground() throws Exception {
                int id = servicio.generarSerie(fn, xmin, xmax, incremento, unidad);
                try {
                    Path carpeta = Path.of("..", "graficas").toAbsolutePath().normalize(); Files.createDirectories(carpeta);
                    Path png = Files.createTempFile(carpeta, "serie-" + id + "-", ".png");
                    if (!servicio.generarGrafica(id, png.toString())) throw new IllegalStateException("Python no confirmó la creación de la imagen.");
                    var buffer = ImageIO.read(png.toFile());
                    if (buffer == null) throw new IllegalStateException("No se puede leer la imagen generada.");
                    return new Resultado(new ImageIcon(buffer), servicio.tienePuntosIndefinidos(id), png, id);
                } catch (Exception ex) {
                    throw new Exception("La serie #" + id + " quedó guardada, pero no se pudo mostrar la gráfica: " + ex.getMessage(), ex);
                }
            }
            @Override protected void done() {
                habilitar(true);
                try {
                    Resultado r = get(); imagen.setText(null); imagen.setIcon(r.icono());
                    estado.setText(r.indefinidos() ? "Serie #" + r.id() + ": puntos indefinidos o cortes de asíntotas; se muestran separados." : "Serie #" + r.id() + " generada correctamente.");
                    estado.setToolTipText("PNG guardado en: " + r.archivo());
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); fallo("Se interrumpió la generación.");
                } catch (ExecutionException ex) { fallo(ex.getCause().getMessage()); }
            }
        }.execute();
    }

    private void fallo(String mensaje) {
        imagen.setText("No se pudo mostrar la gráfica"); estado.setText("Revisa el mensaje y vuelve a intentarlo.");
        JOptionPane.showMessageDialog(this, mensaje, "Error al graficar", JOptionPane.ERROR_MESSAGE);
    }
    private record Resultado(ImageIcon icono, boolean indefinidos, Path archivo, int id) { }
}
