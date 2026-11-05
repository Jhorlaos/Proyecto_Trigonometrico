/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calculadora.ui;

import com.calculadora.dominio.Division;
import com.calculadora.dominio.Operacion;
import com.calculadora.dominio.Suma;
import com.calculadora.dominio.Resta;
import com.calculadora.dominio.Multiplicacion;
import com.calculadora.excepciones.CalculadoraException;
import com.calculadora.servicio.CalculadoraService;
import com.calculadora.servicio.CalculadoraServiceImpl;



public class Calculator extends javax.swing.JFrame {

// Variables para el flujo de la vista
    double firstNum;
    double secondNum;
    double result;
    String operation;

    // --- CONEXIÓN CON EL BACKEND (N-TIER) ---
    private CalculadoraService servicio;
    
    // --- INTEGRACIÓN Y SERVICIOS ---
    private String modoActual = "grados"; 
    private boolean esperandoOperando;
    private boolean resultadoMostrado;
    private boolean cambioInterno;
    private String operandoVisual;
    private String funcionActual = "";
    private final com.calculadora.servicio.GestorGraficas gestorGraficas = new com.calculadora.servicio.GestorGraficas();
    private com.calculadora.servicio.ParametrosGrafica ultimaGrafica;
    

    /**
     * Creates new form Calculator
     */
    public Calculator() {
        initComponents();
        servicio = new CalculadoraServiceImpl();
        expresionVisible.setText("");
        prepararPantalla();
    }
    
    private void ejecutarTrigonometria(String simbolo, String nombre) {
        try {
            double valor = leerNumero();
            var op = new com.calculadora.dominio.OperacionTrigonometrica(simbolo, nombre);
            op.setModoAngulo(modoActual);
            double respuesta = servicio.ejecutarYGuardar(op, new double[]{valor});
            String unidad = simbolo.startsWith("a") ? "" : modoActual.equals("grados") ? "°" : " rad";
            String detalle = simbolo + "(" + formato(valor) + unidad + ")";
            esperandoOperando = false;
            mostrarNumero(formato(respuesta));
            if (operation != null) {
                resultadoMostrado = false;
                operandoVisual = detalle;
                actualizarExpresion();
            } else {
                resultadoMostrado = true;
                mostrarExpresion(detalle + " =");
                ayudaPantalla.setText(simbolo.startsWith("a") ? "Resultado en " + modoActual : "Resultado");
            }
            funcionActual = simbolo;
            prepararUltimaGrafica();
            seleccionarNumero();
        } catch (NumberFormatException ex) {
            ayudaPantalla.setText("Escribe un número válido antes de elegir la función.");
        } catch (CalculadoraException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void prepararUltimaGrafica() {
        var solicitud = com.calculadora.servicio.ParametrosGrafica.recomendados(funcionActual, modoActual);
        ultimaGrafica = solicitud;
        jBtn18.setText("Graficar");
        jBtn18.setToolTipText("Preparando " + funcionActual + " en segundo plano…");
        gestorGraficas.preparar(solicitud).whenComplete((grafica, error) ->
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (!isDisplayable() || !solicitud.equals(ultimaGrafica)) return;
                    jBtn18.setText(error == null ? "Ver gráfica" : "Graficar");
                    jBtn18.setToolTipText(error == null
                            ? "La gráfica de " + solicitud.funcion() + " ya está preparada."
                            : "No se pudo preparar. Abre Graficar para reintentar.");
                }));
    }

    @Override public void dispose() {
        gestorGraficas.close();
        super.dispose();
    }

    private void agregarDigito(String digito) {
        prepararEntrada();
        String texto = jTxtDisplay.getText();
        mostrarNumero(texto.equals("0") ? digito : texto.equals("-0") ? "-" + digito : texto + digito);
        actualizarExpresion();
    }

    private void prepararPantalla() {
        EstiloCalculadora.aplicar(panelPrincipal);
        jBtn18.putClientProperty("accionGrafica", true);
        expresionVisible.setFocusable(false);
        expresionVisible.getAccessibleContext().setAccessibleName("Operación completa");
        jTxtDisplay.getAccessibleContext().setAccessibleName("Número actual o resultado");
        jTxtDisplay.setToolTipText("Enter calcula el resultado. La operación completa se muestra arriba.");
        jBtn10.setToolTipText("Consultar y recuperar cálculos anteriores");
        jBtn11.setToolTipText("Seno"); jBtn12.setToolTipText("Coseno"); jBtn13.setToolTipText("Tangente");
        jBtn14.setToolTipText("Arcoseno"); jBtn15.setToolTipText("Arcocoseno"); jBtn16.setToolTipText("Arcotangente");
        javax.swing.ButtonGroup unidades = new javax.swing.ButtonGroup();
        unidades.add(jBtn17); unidades.add(jBtnRadianes);
        cambiarModo("grados");
        jTxtDisplay.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { entradaManual(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { entradaManual(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { entradaManual(); }
        });
        limpiarPantalla();
        setMinimumSize(new java.awt.Dimension(740, 690));
        pack();
    }

    private void cambiarModo(String modo) {
        modoActual = modo;
        boolean grados = "grados".equals(modo);
        jBtn17.setSelected(grados);
        jBtnRadianes.setSelected(!grados);
        estadoModo.setText(grados ? "Grados activo" : "Radianes activo");
    }

    private void entradaManual() {
        if (cambioInterno) return;
        esperandoOperando = false;
        resultadoMostrado = false;
        operandoVisual = null;
        actualizarExpresion();
    }

    private void mostrarNumero(String texto) {
        cambioInterno = true;
        try { jTxtDisplay.setText(texto); }
        finally { cambioInterno = false; }
    }

    private void mostrarExpresion(String texto) {
        expresionVisible.setText(texto);
        expresionVisible.setToolTipText(texto);
        expresionVisible.setCaretPosition(texto.length());
    }

    private static String signo(String operador) {
        return switch (operador) { case "*" -> "×"; case "/" -> "÷"; case "-" -> "−"; default -> operador; };
    }

    private static String formato(double numero) {
        if (!Double.isFinite(numero)) return String.valueOf(numero);
        if (numero != 0 && (Math.abs(numero) >= 1e15 || Math.abs(numero) < 1e-8)) return Double.toString(numero);
        return java.math.BigDecimal.valueOf(numero).stripTrailingZeros().toPlainString();
    }

    private double leerNumero() {
        double numero = Double.parseDouble(jTxtDisplay.getText().trim());
        if (!Double.isFinite(numero)) throw new NumberFormatException("Número no finito");
        return numero;
    }

    private void seleccionarNumero() {
        jTxtDisplay.selectAll();
        jTxtDisplay.requestFocusInWindow();
    }

    private void prepararEntrada() {
        if (esperandoOperando || resultadoMostrado || jTxtDisplay.getText().equals("Error") || jTxtDisplay.getText().isBlank()) {
            mostrarNumero("");
        }
        esperandoOperando = false;
        resultadoMostrado = false;
        operandoVisual = null;
    }

    private void actualizarExpresion() {
        if (operation != null) {
            String segundo = esperandoOperando ? "" : operandoVisual != null ? operandoVisual : jTxtDisplay.getText();
            if (segundo.startsWith("-")) segundo = "(" + segundo + ")";
            mostrarExpresion(formato(firstNum) + " " + signo(operation) + (segundo.isEmpty() ? "" : " " + segundo));
            ayudaPantalla.setText(esperandoOperando ? "Escribe el segundo número" : "Pulsa = para calcular");
        } else {
            mostrarExpresion(jTxtDisplay.getText());
            ayudaPantalla.setText("Elige una operación");
        }
    }

    private void limpiarPantalla() {
        firstNum = secondNum = result = 0;
        operation = null;
        esperandoOperando = resultadoMostrado = false;
        operandoVisual = null;
        mostrarNumero("");
        mostrarExpresion(" ");
        ayudaPantalla.setText("Escribe un número para comenzar");
    }

    private void seleccionarOperacion(String operador) {
        try {
            if (operation != null && !esperandoOperando) {
                if (!resolverOperacion()) return;
            }
            firstNum = leerNumero();
            operation = operador;
            esperandoOperando = true;
            resultadoMostrado = false;
            operandoVisual = null;
            actualizarExpresion();
            seleccionarNumero();
        } catch (NumberFormatException ex) {
            ayudaPantalla.setText("Escribe un número válido antes de elegir el signo.");
        }
    }

    private boolean resolverOperacion() {
        if (operation == null) return false;
        if (esperandoOperando) {
            ayudaPantalla.setText("Falta el segundo número");
            return false;
        }
        try {
            secondNum = leerNumero();
            actualizarExpresion();
            String expresion = expresionVisible.getText();
            Operacion op = switch (operation) {
                case "+" -> new Suma(); case "-" -> new Resta();
                case "*" -> new Multiplicacion(); case "/" -> new Division();
                default -> throw new IllegalStateException("Operación desconocida");
            };
            result = servicio.ejecutarYGuardar(op, new double[]{firstNum, secondNum});
            if (!Double.isFinite(result)) {
                mostrarError("El resultado excede el rango admitido.");
                return false;
            }
            mostrarNumero(formato(result));
            mostrarExpresion(expresion + " =");
            ayudaPantalla.setText("Resultado · escribe para comenzar otro cálculo");
            operation = null;
            esperandoOperando = false;
            resultadoMostrado = true;
            operandoVisual = null;
            seleccionarNumero();
            return true;
        } catch (NumberFormatException ex) {
            ayudaPantalla.setText("Revisa el número escrito antes de pulsar =");
        } catch (CalculadoraException ex) {
            mostrarError(ex.getMessage());
        }
        return false;
    }

    private void mostrarError(String mensaje) {
        mostrarNumero("Error");
        ayudaPantalla.setText(mensaje.length() > 85 ? mensaje.substring(0, 82) + "…" : mensaje);
        ayudaPantalla.setToolTipText(mensaje);
        operation = null;
        esperandoOperando = false;
        resultadoMostrado = true;
        operandoVisual = null;
        seleccionarNumero();
    }

    private void cargarDesdeHistorial(double numero) {
        mostrarNumero(formato(numero));
        esperandoOperando = false;
        resultadoMostrado = operation == null;
        operandoVisual = null;
        actualizarExpresion();
        if (operation == null) ayudaPantalla.setText("Resultado recuperado del historial");
        seleccionarNumero();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panelPrincipal = new javax.swing.JPanel();
        panelPantalla = new javax.swing.JPanel();
        panelExpresion = new javax.swing.JPanel();
        etiquetaPantalla = new javax.swing.JLabel();
        expresionVisible = new javax.swing.JTextField();
        jTxtDisplay = new javax.swing.JTextField();
        ayudaPantalla = new javax.swing.JLabel();
        panelCuerpo = new javax.swing.JPanel();
        panelOperaciones = new javax.swing.JPanel();
        tituloOperaciones = new javax.swing.JLabel();
        panelTeclado = new javax.swing.JPanel();
        panelNumeros = new javax.swing.JPanel();
        jBtn7 = new javax.swing.JButton();
        jBtn8 = new javax.swing.JButton();
        jBtn9 = new javax.swing.JButton();
        jBtnDivision = new javax.swing.JButton();
        jBtn4 = new javax.swing.JButton();
        jBtn5 = new javax.swing.JButton();
        jBtn6 = new javax.swing.JButton();
        jBtnMultiplication = new javax.swing.JButton();
        jBtn1 = new javax.swing.JButton();
        jBtn2 = new javax.swing.JButton();
        jBtn3 = new javax.swing.JButton();
        jBtnMinus = new javax.swing.JButton();
        jBtnNegativeNum = new javax.swing.JButton();
        jBtn0 = new javax.swing.JButton();
        jBtnDot = new javax.swing.JButton();
        jBtnSum = new javax.swing.JButton();
        panelResultado = new javax.swing.JPanel();
        jBtnClear = new javax.swing.JButton();
        jBtnResult = new javax.swing.JButton();
        panelFunciones = new javax.swing.JPanel();
        tituloFunciones = new javax.swing.JLabel();
        panelCientifico = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jBtn11 = new javax.swing.JButton();
        jBtn14 = new javax.swing.JButton();
        jBtn12 = new javax.swing.JButton();
        jBtn15 = new javax.swing.JButton();
        jBtn13 = new javax.swing.JButton();
        jBtn16 = new javax.swing.JButton();
        panelUnidad = new javax.swing.JPanel();
        tituloUnidad = new javax.swing.JLabel();
        panelSelector = new javax.swing.JPanel();
        jBtn17 = new javax.swing.JToggleButton();
        jBtnRadianes = new javax.swing.JToggleButton();
        estadoModo = new javax.swing.JLabel();
        panelAcciones = new javax.swing.JPanel();
        jBtn10 = new javax.swing.JButton();
        jBtn18 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Calculadora científica");

        panelPrincipal.setBackground(new java.awt.Color(37, 41, 45));
        panelPrincipal.setForeground(new java.awt.Color(242, 244, 245));
        panelPrincipal.setPreferredSize(new java.awt.Dimension(760, 700));
        panelPrincipal.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelPrincipal.setLayout(new java.awt.BorderLayout(0, 20));

        panelPantalla.setBackground(new java.awt.Color(29, 33, 36));
        panelPantalla.setForeground(new java.awt.Color(242, 244, 245));
        panelPantalla.setPreferredSize(new java.awt.Dimension(710, 158));
        panelPantalla.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 12, 18));
        panelPantalla.setLayout(new java.awt.BorderLayout(0, 8));

        panelExpresion.setBackground(new java.awt.Color(29, 33, 36));
        panelExpresion.setForeground(new java.awt.Color(242, 244, 245));
        panelExpresion.setLayout(new java.awt.BorderLayout(10, 0));

        etiquetaPantalla.setBackground(new java.awt.Color(29, 33, 36));
        etiquetaPantalla.setForeground(new java.awt.Color(188, 196, 202));
        etiquetaPantalla.setText("RESULTADO");
        panelExpresion.add(etiquetaPantalla, java.awt.BorderLayout.WEST);

        expresionVisible.setEditable(false);
        expresionVisible.setBackground(new java.awt.Color(29, 33, 36));
        expresionVisible.setFont(new java.awt.Font("Segoe UI", 0, 21)); // NOI18N
        expresionVisible.setForeground(new java.awt.Color(242, 244, 245));
        expresionVisible.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        expresionVisible.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expresionVisibleActionPerformed(evt);
            }
        });
        panelExpresion.add(expresionVisible, java.awt.BorderLayout.CENTER);

        panelPantalla.add(panelExpresion, java.awt.BorderLayout.NORTH);

        jTxtDisplay.setBackground(new java.awt.Color(29, 33, 36));
        jTxtDisplay.setForeground(new java.awt.Color(242, 244, 245));
        jTxtDisplay.setFont(new java.awt.Font("Segoe UI", 0, 54)); // NOI18N
        jTxtDisplay.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTxtDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTxtDisplayActionPerformed(evt);
            }
        });
        panelPantalla.add(jTxtDisplay, java.awt.BorderLayout.CENTER);

        ayudaPantalla.setBackground(new java.awt.Color(29, 33, 36));
        ayudaPantalla.setForeground(new java.awt.Color(188, 196, 202));
        ayudaPantalla.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        ayudaPantalla.setText("Escribe un número para comenzar");
        panelPantalla.add(ayudaPantalla, java.awt.BorderLayout.SOUTH);

        panelPrincipal.add(panelPantalla, java.awt.BorderLayout.NORTH);

        panelCuerpo.setBackground(new java.awt.Color(37, 41, 45));
        panelCuerpo.setForeground(new java.awt.Color(242, 244, 245));
        panelCuerpo.setLayout(new java.awt.GridBagLayout());

        panelOperaciones.setBackground(new java.awt.Color(37, 41, 45));
        panelOperaciones.setForeground(new java.awt.Color(242, 244, 245));
        panelOperaciones.setLayout(new java.awt.BorderLayout(0, 12));

        tituloOperaciones.setBackground(new java.awt.Color(37, 41, 45));
        tituloOperaciones.setForeground(new java.awt.Color(242, 244, 245));
        tituloOperaciones.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        tituloOperaciones.setText("OPERACIONES");
        panelOperaciones.add(tituloOperaciones, java.awt.BorderLayout.NORTH);

        panelTeclado.setBackground(new java.awt.Color(37, 41, 45));
        panelTeclado.setForeground(new java.awt.Color(242, 244, 245));
        panelTeclado.setLayout(new java.awt.BorderLayout(0, 10));

        panelNumeros.setBackground(new java.awt.Color(37, 41, 45));
        panelNumeros.setForeground(new java.awt.Color(242, 244, 245));
        panelNumeros.setLayout(new java.awt.GridLayout(4, 4, 10, 10));

        jBtn7.setBackground(new java.awt.Color(54, 60, 64));
        jBtn7.setForeground(new java.awt.Color(242, 244, 245));
        jBtn7.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn7.setText("7");
        jBtn7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn7ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn7);

        jBtn8.setBackground(new java.awt.Color(54, 60, 64));
        jBtn8.setForeground(new java.awt.Color(242, 244, 245));
        jBtn8.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn8.setText("8");
        jBtn8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn8ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn8);

        jBtn9.setBackground(new java.awt.Color(54, 60, 64));
        jBtn9.setForeground(new java.awt.Color(242, 244, 245));
        jBtn9.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn9.setText("9");
        jBtn9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn9ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn9);

        jBtnDivision.setBackground(new java.awt.Color(54, 60, 64));
        jBtnDivision.setForeground(new java.awt.Color(181, 228, 59));
        jBtnDivision.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtnDivision.setText("÷");
        jBtnDivision.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDivisionActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtnDivision);

        jBtn4.setBackground(new java.awt.Color(54, 60, 64));
        jBtn4.setForeground(new java.awt.Color(242, 244, 245));
        jBtn4.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn4.setText("4");
        jBtn4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn4ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn4);

        jBtn5.setBackground(new java.awt.Color(54, 60, 64));
        jBtn5.setForeground(new java.awt.Color(242, 244, 245));
        jBtn5.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn5.setText("5");
        jBtn5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn5ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn5);

        jBtn6.setBackground(new java.awt.Color(54, 60, 64));
        jBtn6.setForeground(new java.awt.Color(242, 244, 245));
        jBtn6.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn6.setText("6");
        jBtn6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn6ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn6);

        jBtnMultiplication.setBackground(new java.awt.Color(54, 60, 64));
        jBtnMultiplication.setForeground(new java.awt.Color(181, 228, 59));
        jBtnMultiplication.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtnMultiplication.setText("×");
        jBtnMultiplication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnMultiplicationActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtnMultiplication);

        jBtn1.setBackground(new java.awt.Color(54, 60, 64));
        jBtn1.setForeground(new java.awt.Color(242, 244, 245));
        jBtn1.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn1.setText("1");
        jBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn1ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn1);

        jBtn2.setBackground(new java.awt.Color(54, 60, 64));
        jBtn2.setForeground(new java.awt.Color(242, 244, 245));
        jBtn2.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn2.setText("2");
        jBtn2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn2ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn2);

        jBtn3.setBackground(new java.awt.Color(54, 60, 64));
        jBtn3.setForeground(new java.awt.Color(242, 244, 245));
        jBtn3.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn3.setText("3");
        jBtn3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn3ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn3);

        jBtnMinus.setBackground(new java.awt.Color(54, 60, 64));
        jBtnMinus.setForeground(new java.awt.Color(181, 228, 59));
        jBtnMinus.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtnMinus.setText("−");
        jBtnMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnMinusActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtnMinus);

        jBtnNegativeNum.setBackground(new java.awt.Color(54, 60, 64));
        jBtnNegativeNum.setForeground(new java.awt.Color(242, 244, 245));
        jBtnNegativeNum.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtnNegativeNum.setText("±");
        jBtnNegativeNum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnNegativeNumActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtnNegativeNum);

        jBtn0.setBackground(new java.awt.Color(54, 60, 64));
        jBtn0.setForeground(new java.awt.Color(242, 244, 245));
        jBtn0.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn0.setText("0");
        jBtn0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn0ActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtn0);

        jBtnDot.setBackground(new java.awt.Color(54, 60, 64));
        jBtnDot.setForeground(new java.awt.Color(242, 244, 245));
        jBtnDot.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtnDot.setText(".");
        jBtnDot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDotActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtnDot);

        jBtnSum.setBackground(new java.awt.Color(54, 60, 64));
        jBtnSum.setForeground(new java.awt.Color(181, 228, 59));
        jBtnSum.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtnSum.setText("+");
        jBtnSum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSumActionPerformed(evt);
            }
        });
        panelNumeros.add(jBtnSum);

        panelTeclado.add(panelNumeros, java.awt.BorderLayout.CENTER);

        panelResultado.setBackground(new java.awt.Color(37, 41, 45));
        panelResultado.setForeground(new java.awt.Color(242, 244, 245));
        panelResultado.setPreferredSize(new java.awt.Dimension(400, 62));
        panelResultado.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        jBtnClear.setBackground(new java.awt.Color(54, 60, 64));
        jBtnClear.setForeground(new java.awt.Color(242, 244, 245));
        jBtnClear.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtnClear.setText("Limpiar");
        jBtnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnClearActionPerformed(evt);
            }
        });
        panelResultado.add(jBtnClear);

        jBtnResult.setBackground(new java.awt.Color(181, 228, 59));
        jBtnResult.setForeground(new java.awt.Color(37, 41, 45));
        jBtnResult.setFont(new java.awt.Font("Segoe UI", 0, 32)); // NOI18N
        jBtnResult.setText("=");
        jBtnResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnResultActionPerformed(evt);
            }
        });
        panelResultado.add(jBtnResult);

        panelTeclado.add(panelResultado, java.awt.BorderLayout.SOUTH);

        panelOperaciones.add(panelTeclado, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.62;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        panelCuerpo.add(panelOperaciones, gridBagConstraints);

        panelFunciones.setBackground(new java.awt.Color(37, 41, 45));
        panelFunciones.setForeground(new java.awt.Color(242, 244, 245));
        panelFunciones.setLayout(new java.awt.BorderLayout(0, 12));

        tituloFunciones.setBackground(new java.awt.Color(37, 41, 45));
        tituloFunciones.setForeground(new java.awt.Color(242, 244, 245));
        tituloFunciones.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        tituloFunciones.setText("FUNCIONES");
        panelFunciones.add(tituloFunciones, java.awt.BorderLayout.NORTH);

        panelCientifico.setBackground(new java.awt.Color(37, 41, 45));
        panelCientifico.setForeground(new java.awt.Color(242, 244, 245));
        panelCientifico.setLayout(new java.awt.BorderLayout(0, 16));

        jPanel1.setBackground(new java.awt.Color(37, 41, 45));
        jPanel1.setForeground(new java.awt.Color(242, 244, 245));
        jPanel1.setLayout(new java.awt.GridLayout(3, 2, 10, 10));

        jBtn11.setBackground(new java.awt.Color(54, 60, 64));
        jBtn11.setForeground(new java.awt.Color(181, 228, 59));
        jBtn11.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn11.setText("sin");
        jBtn11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn11ActionPerformed(evt);
            }
        });
        jPanel1.add(jBtn11);

        jBtn14.setBackground(new java.awt.Color(54, 60, 64));
        jBtn14.setForeground(new java.awt.Color(181, 228, 59));
        jBtn14.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn14.setText("asin");
        jBtn14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn14ActionPerformed(evt);
            }
        });
        jPanel1.add(jBtn14);

        jBtn12.setBackground(new java.awt.Color(54, 60, 64));
        jBtn12.setForeground(new java.awt.Color(181, 228, 59));
        jBtn12.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn12.setText("cos");
        jBtn12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn12ActionPerformed(evt);
            }
        });
        jPanel1.add(jBtn12);

        jBtn15.setBackground(new java.awt.Color(54, 60, 64));
        jBtn15.setForeground(new java.awt.Color(181, 228, 59));
        jBtn15.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn15.setText("acos");
        jBtn15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn15ActionPerformed(evt);
            }
        });
        jPanel1.add(jBtn15);

        jBtn13.setBackground(new java.awt.Color(54, 60, 64));
        jBtn13.setForeground(new java.awt.Color(181, 228, 59));
        jBtn13.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn13.setText("tan");
        jBtn13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn13ActionPerformed(evt);
            }
        });
        jPanel1.add(jBtn13);

        jBtn16.setBackground(new java.awt.Color(54, 60, 64));
        jBtn16.setForeground(new java.awt.Color(181, 228, 59));
        jBtn16.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn16.setText("atan");
        jBtn16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn16ActionPerformed(evt);
            }
        });
        jPanel1.add(jBtn16);

        panelCientifico.add(jPanel1, java.awt.BorderLayout.CENTER);

        panelUnidad.setBackground(new java.awt.Color(37, 41, 45));
        panelUnidad.setForeground(new java.awt.Color(242, 244, 245));
        panelUnidad.setPreferredSize(new java.awt.Dimension(250, 124));
        panelUnidad.setLayout(new java.awt.GridLayout(3, 1, 0, 6));

        tituloUnidad.setBackground(new java.awt.Color(37, 41, 45));
        tituloUnidad.setForeground(new java.awt.Color(188, 196, 202));
        tituloUnidad.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tituloUnidad.setText("Unidad angular");
        panelUnidad.add(tituloUnidad);

        panelSelector.setBackground(new java.awt.Color(37, 41, 45));
        panelSelector.setForeground(new java.awt.Color(242, 244, 245));
        panelSelector.setLayout(new java.awt.GridLayout(1, 2, 6, 0));

        jBtn17.setBackground(new java.awt.Color(54, 60, 64));
        jBtn17.setForeground(new java.awt.Color(242, 244, 245));
        jBtn17.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jBtn17.setText("Grados");
        jBtn17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn17ActionPerformed(evt);
            }
        });
        panelSelector.add(jBtn17);

        jBtnRadianes.setBackground(new java.awt.Color(54, 60, 64));
        jBtnRadianes.setForeground(new java.awt.Color(242, 244, 245));
        jBtnRadianes.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jBtnRadianes.setText("Radianes");
        jBtnRadianes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnRadianesActionPerformed(evt);
            }
        });
        panelSelector.add(jBtnRadianes);

        panelUnidad.add(panelSelector);

        estadoModo.setBackground(new java.awt.Color(37, 41, 45));
        estadoModo.setForeground(new java.awt.Color(188, 196, 202));
        estadoModo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        estadoModo.setText("Grados activo");
        panelUnidad.add(estadoModo);

        panelCientifico.add(panelUnidad, java.awt.BorderLayout.SOUTH);

        panelFunciones.add(panelCientifico, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.38;
        gridBagConstraints.weighty = 1.0;
        panelCuerpo.add(panelFunciones, gridBagConstraints);

        panelPrincipal.add(panelCuerpo, java.awt.BorderLayout.CENTER);

        panelAcciones.setBackground(new java.awt.Color(37, 41, 45));
        panelAcciones.setForeground(new java.awt.Color(242, 244, 245));
        panelAcciones.setPreferredSize(new java.awt.Dimension(710, 58));
        panelAcciones.setLayout(new java.awt.GridLayout(1, 2, 12, 0));

        jBtn10.setBackground(new java.awt.Color(54, 60, 64));
        jBtn10.setForeground(new java.awt.Color(242, 244, 245));
        jBtn10.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn10.setText("Historial");
        jBtn10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn10ActionPerformed(evt);
            }
        });
        panelAcciones.add(jBtn10);

        jBtn18.setBackground(new java.awt.Color(54, 60, 64));
        jBtn18.setForeground(new java.awt.Color(181, 228, 59));
        jBtn18.setFont(new java.awt.Font("Segoe UI", 0, 23)); // NOI18N
        jBtn18.setText("Ver gráfica");
        jBtn18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtn18ActionPerformed(evt);
            }
        });
        panelAcciones.add(jBtn18);

        panelPrincipal.add(panelAcciones, java.awt.BorderLayout.SOUTH);

        getContentPane().add(panelPrincipal, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTxtDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTxtDisplayActionPerformed
        resolverOperacion();
        jTxtDisplay.setText("");
    }//GEN-LAST:event_jTxtDisplayActionPerformed

    private void jBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn1ActionPerformed
        agregarDigito("1");
    }//GEN-LAST:event_jBtn1ActionPerformed

    private void jBtn2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn2ActionPerformed
       agregarDigito("2");
       
    }//GEN-LAST:event_jBtn2ActionPerformed

    private void jBtn3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn3ActionPerformed
        agregarDigito("3");
        
    }//GEN-LAST:event_jBtn3ActionPerformed

    private void jBtn4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn4ActionPerformed
        agregarDigito("4");
    }//GEN-LAST:event_jBtn4ActionPerformed

    private void jBtn5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn5ActionPerformed
      agregarDigito("5");
       
    }//GEN-LAST:event_jBtn5ActionPerformed

    private void jBtn6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn6ActionPerformed
     agregarDigito("6");
    }//GEN-LAST:event_jBtn6ActionPerformed

    private void jBtn7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn7ActionPerformed
      agregarDigito("7");
     
    }//GEN-LAST:event_jBtn7ActionPerformed

    private void jBtn8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn8ActionPerformed
       agregarDigito("8");
    }//GEN-LAST:event_jBtn8ActionPerformed

    private void jBtn9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn9ActionPerformed
     agregarDigito("9");
    }//GEN-LAST:event_jBtn9ActionPerformed

    private void jBtn0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn0ActionPerformed
         agregarDigito("0");
    }//GEN-LAST:event_jBtn0ActionPerformed

    private void jBtnNegativeNumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnNegativeNumActionPerformed
        if (esperandoOperando || jTxtDisplay.getText().isBlank() || jTxtDisplay.getText().equals("Error")) mostrarNumero("0");
        esperandoOperando = resultadoMostrado = false;
        operandoVisual = null;
        String texto = jTxtDisplay.getText();
        mostrarNumero(texto.startsWith("-") ? texto.substring(1) : "-" + texto);
        actualizarExpresion();
    }//GEN-LAST:event_jBtnNegativeNumActionPerformed

    private void jBtnDotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDotActionPerformed
        prepararEntrada();
        String texto = jTxtDisplay.getText();
        if (!texto.contains(".")) mostrarNumero(texto + ".");
        actualizarExpresion();
    }//GEN-LAST:event_jBtnDotActionPerformed

    private void jBtnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnClearActionPerformed
        expresionVisible.setText("");
        limpiarPantalla();
    }//GEN-LAST:event_jBtnClearActionPerformed

    private void jBtnSumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSumActionPerformed
        seleccionarOperacion("+");
    }//GEN-LAST:event_jBtnSumActionPerformed

    private void jBtnResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnResultActionPerformed
        resolverOperacion();
    }//GEN-LAST:event_jBtnResultActionPerformed

    private void jBtnMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnMinusActionPerformed
        seleccionarOperacion("-");
    }//GEN-LAST:event_jBtnMinusActionPerformed

    private void jBtnMultiplicationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnMultiplicationActionPerformed
        seleccionarOperacion("*");
    }//GEN-LAST:event_jBtnMultiplicationActionPerformed

    private void jBtnDivisionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDivisionActionPerformed
        seleccionarOperacion("/");
    }//GEN-LAST:event_jBtnDivisionActionPerformed

    private void jBtn10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn10ActionPerformed
try {
            // 1. Obtenemos el historial y la lista de operaciones
            java.util.List<com.calculadora.dto.CalculoDTO> historial = servicio.obtenerHistorial();
            com.calculadora.dao.OperacionDAO opDAO = new com.calculadora.dao.OperacionDAOImpl();
            java.util.List<com.calculadora.dto.OperacionDTO> listaOperaciones = opDAO.listarOperaciones();

            if (historial.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, 
                        "El historial está vacío. Aún no has realizado operaciones.", 
                        "Historial de Cálculos", 
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 2. Separar listas de DTOs y modelos para la UI
            java.util.List<com.calculadora.dto.CalculoDTO> exitosos = new java.util.ArrayList<>();
            javax.swing.DefaultListModel<String> modeloExitosos = new javax.swing.DefaultListModel<>();
            javax.swing.DefaultListModel<String> modeloFallidos = new javax.swing.DefaultListModel<>();

            for (com.calculadora.dto.CalculoDTO dto : historial) {
                
                // 3. Buscar el símbolo de la operación usando el operacionId
                String nombreOp = "Op";
                for (com.calculadora.dto.OperacionDTO op : listaOperaciones) {
                    if (op.getId() == dto.getOperacionId()) {
                        nombreOp = op.getSimbolo();
                        break;
                    }
                }

                if (dto.isExito()) {
                    exitosos.add(dto);
                    if (dto.getOperandoB() != null) {
                        // Formato para operaciones básicas (ej: Suma, Resta)
                        modeloExitosos.addElement(String.format(" ID: %d  |  [ %s ]  |  %.2f y %.2f  ->  Resultado: %.2f", 
                                dto.getId(), nombreOp, dto.getOperandoA(), dto.getOperandoB(), dto.getResultado()));
                    } else {
                        // Formato para trigonométricas (1 operando)
                        modeloExitosos.addElement(String.format(" ID: %d  |  [ %s ]  |  %.2f  ->  Resultado: %.2f", 
                                dto.getId(), nombreOp, dto.getOperandoA(), dto.getResultado()));
                    }
                } else {
                    // Formato para operaciones fallidas
                    modeloFallidos.addElement(String.format(" ID: %d  |  [ %s ]  |  Falló: %s", 
                            dto.getId(), nombreOp, dto.getMensajeError()));
                }
            }

            // 4. Colores temáticos de la calculadora
            java.awt.Color fondoGris = new java.awt.Color(50, 50, 50);
            java.awt.Color textoBlanco = java.awt.Color.WHITE;
            java.awt.Color seleccionVerde = new java.awt.Color(139, 195, 74); 

            // 5. Configurar el renderizador visual para darle color a las listas
            javax.swing.DefaultListCellRenderer cellRenderer = new javax.swing.DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    java.awt.Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (isSelected) {
                        c.setBackground(seleccionVerde);
                        c.setForeground(java.awt.Color.BLACK);
                    } else {
                        c.setBackground(fondoGris);
                        c.setForeground(textoBlanco);
                    }
                    c.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                    return c;
                }
            };

            // 6. Construir las listas visuales (JList)
            javax.swing.JList<String> listaExitosos = new javax.swing.JList<>(modeloExitosos);
            listaExitosos.setCellRenderer(cellRenderer);
            listaExitosos.setBackground(fondoGris);
            listaExitosos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

            javax.swing.JList<String> listaFallidos = new javax.swing.JList<>(modeloFallidos);
            listaFallidos.setCellRenderer(cellRenderer);
            listaFallidos.setBackground(fondoGris);
            listaFallidos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION); 

            javax.swing.JScrollPane scrollExitosos = new javax.swing.JScrollPane(listaExitosos);
            scrollExitosos.setPreferredSize(new java.awt.Dimension(500, 250));
            scrollExitosos.getViewport().setBackground(fondoGris);
            scrollExitosos.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

            javax.swing.JScrollPane scrollFallidos = new javax.swing.JScrollPane(listaFallidos);
            scrollFallidos.setPreferredSize(new java.awt.Dimension(500, 250));
            scrollFallidos.getViewport().setBackground(fondoGris);
            scrollFallidos.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // 7. Crear el contenedor con pestañas
            javax.swing.JTabbedPane tabbedPane = new javax.swing.JTabbedPane();
            tabbedPane.addTab("Exitosas (Clic para usar)", scrollExitosos);
            tabbedPane.addTab("Operaciones Fallidas", scrollFallidos);

            // 8. CREACIÓN DEL DIÁLOGO PERSONALIZADO
            javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this), "Historial de Cálculos", true);
            dialog.setLayout(new java.awt.BorderLayout());
            
            javax.swing.JPanel panelBotones = new javax.swing.JPanel();
            javax.swing.JButton btnOk = new javax.swing.JButton("Cargar Seleccionado");
            javax.swing.JButton btnCancel = new javax.swing.JButton("Cerrar");
            panelBotones.add(btnOk);
            panelBotones.add(btnCancel);

            dialog.add(tabbedPane, java.awt.BorderLayout.CENTER);
            dialog.add(panelBotones, java.awt.BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(this);

            // Lógica para ocultar el botón OK en la pestaña de errores
            tabbedPane.addChangeListener(e -> {
                if (tabbedPane.getSelectedIndex() == 1) { // Pestaña Fallidas
                    btnOk.setVisible(false);
                } else { // Pestaña Exitosas
                    btnOk.setVisible(true);
                }
            });

            // Arreglo para guardar la respuesta de los botones
            final boolean[] okPresionado = {false};

            btnOk.addActionListener(e -> {
                okPresionado[0] = true;
                dialog.dispose();
            });

            btnCancel.addActionListener(e -> {
                dialog.dispose();
            });

            // Muestra la ventana (se pausa aquí hasta cerrar)
            dialog.setVisible(true);

            // 9. Cargar el resultado a la pantalla si el usuario aceptó
            if (okPresionado[0]) {
                int index = listaExitosos.getSelectedIndex();
                if (index >= 0) {
                    double valorSeleccionado = exitosos.get(index).getResultado();
                    cargarDesdeHistorial(valorSeleccionado);
                }
            }

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                    "Error al consultar el historial: " + ex.getMessage(), 
                    "Error", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jBtn10ActionPerformed

    private void jBtn11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn11ActionPerformed
        ejecutarTrigonometria("sin", "Seno");
    }//GEN-LAST:event_jBtn11ActionPerformed

    private void jBtn12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn12ActionPerformed
       ejecutarTrigonometria("cos", "Coseno");
    }//GEN-LAST:event_jBtn12ActionPerformed

    private void jBtn13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn13ActionPerformed
        ejecutarTrigonometria("tan", "Tangente");
    }//GEN-LAST:event_jBtn13ActionPerformed

    private void jBtn14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn14ActionPerformed
        ejecutarTrigonometria("asin", "Arcoseno");
    }//GEN-LAST:event_jBtn14ActionPerformed

    private void jBtn15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn15ActionPerformed
        ejecutarTrigonometria("acos", "Arcocoseno");
    }//GEN-LAST:event_jBtn15ActionPerformed

    private void jBtn16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn16ActionPerformed
        ejecutarTrigonometria("atan", "Arcotangente");
    }//GEN-LAST:event_jBtn16ActionPerformed

    private void jBtn17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn17ActionPerformed
        cambiarModo("grados");
    }//GEN-LAST:event_jBtn17ActionPerformed

    private void jBtnRadianesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRadianesActionPerformed
        cambiarModo("radianes");
    }//GEN-LAST:event_jBtnRadianesActionPerformed

    private void jBtn18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtn18ActionPerformed
// 1. Obtener el texto de la pantalla principal (jTxtDisplay) y de la expresión superior (expresionVisible)
    String displayPrincipal = (jTxtDisplay.getText() != null) ? jTxtDisplay.getText().trim() : "";
    String expresionSuperior = (expresionVisible.getText() != null) ? expresionVisible.getText().trim() : "";

    // 2. VALIDACIÓN ESTRICTA: Si la pantalla está vacía o en estado "Error", NO se abre la ventana
    boolean pantallaVacia = displayPrincipal.isEmpty() && expresionSuperior.isEmpty();
    boolean pantallaConError = displayPrincipal.equalsIgnoreCase("Error");

    if (pantallaVacia || pantallaConError) {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "No hay ningún resultado o dato en pantalla para graficar. Realiza una operación primero.",
            "Resultado vacío",
            javax.swing.JOptionPane.WARNING_MESSAGE
        );
        return; // SE DETIENE LA EJECUCIÓN: Nunca llega a ejecutar new GraficasDialog(...)
    }

    // 3. Determinar función y unidad angular tomando la última gráfica o la función activa
    String funcion = (ultimaGrafica != null) ? ultimaGrafica.funcion() 
                   : (!funcionActual.isEmpty() ? funcionActual : "sin");
    String unidad = (ultimaGrafica != null) ? ultimaGrafica.modo() : modoActual;

    // 4. Abre GraficasDialog forzando que no auto-genere puntos al abrir (último parámetro en false)
    new GraficasDialog(this, funcion, unidad, gestorGraficas, false).setVisible(true);
    }//GEN-LAST:event_jBtn18ActionPerformed

    private void expresionVisibleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expresionVisibleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_expresionVisibleActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Calculator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Calculator().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ayudaPantalla;
    private javax.swing.JLabel estadoModo;
    private javax.swing.JLabel etiquetaPantalla;
    private javax.swing.JTextField expresionVisible;
    private javax.swing.JButton jBtn0;
    private javax.swing.JButton jBtn1;
    private javax.swing.JButton jBtn10;
    private javax.swing.JButton jBtn11;
    private javax.swing.JButton jBtn12;
    private javax.swing.JButton jBtn13;
    private javax.swing.JButton jBtn14;
    private javax.swing.JButton jBtn15;
    private javax.swing.JButton jBtn16;
    private javax.swing.JToggleButton jBtn17;
    private javax.swing.JButton jBtn18;
    private javax.swing.JButton jBtn2;
    private javax.swing.JButton jBtn3;
    private javax.swing.JButton jBtn4;
    private javax.swing.JButton jBtn5;
    private javax.swing.JButton jBtn6;
    private javax.swing.JButton jBtn7;
    private javax.swing.JButton jBtn8;
    private javax.swing.JButton jBtn9;
    private javax.swing.JButton jBtnClear;
    private javax.swing.JButton jBtnDivision;
    private javax.swing.JButton jBtnDot;
    private javax.swing.JButton jBtnMinus;
    private javax.swing.JButton jBtnMultiplication;
    private javax.swing.JButton jBtnNegativeNum;
    private javax.swing.JToggleButton jBtnRadianes;
    private javax.swing.JButton jBtnResult;
    private javax.swing.JButton jBtnSum;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTxtDisplay;
    private javax.swing.JPanel panelAcciones;
    private javax.swing.JPanel panelCientifico;
    private javax.swing.JPanel panelCuerpo;
    private javax.swing.JPanel panelExpresion;
    private javax.swing.JPanel panelFunciones;
    private javax.swing.JPanel panelNumeros;
    private javax.swing.JPanel panelOperaciones;
    private javax.swing.JPanel panelPantalla;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JPanel panelResultado;
    private javax.swing.JPanel panelSelector;
    private javax.swing.JPanel panelTeclado;
    private javax.swing.JPanel panelUnidad;
    private javax.swing.JLabel tituloFunciones;
    private javax.swing.JLabel tituloOperaciones;
    private javax.swing.JLabel tituloUnidad;
    // End of variables declaration//GEN-END:variables
}
