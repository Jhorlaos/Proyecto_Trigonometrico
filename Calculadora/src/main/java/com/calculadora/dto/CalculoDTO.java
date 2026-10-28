package com.calculadora.dto;

import java.time.LocalDateTime;

public class CalculoDTO {

    private Integer id;
    private int operacionId;
    private double operandoA;
    private Double operandoB;
    private String modoAngulo;
    private Double resultado;
    private boolean exito;
    private String mensajeError;
    private LocalDateTime creadoEn;
    private boolean guardado;

    public CalculoDTO() {}

    public CalculoDTO(Integer id, int operacionId, double operandoA, Double operandoB,
                      String modoAngulo, Double resultado, boolean exito,
                      String mensajeError, LocalDateTime creadoEn) {
        this.id = id;
        this.operacionId = operacionId;
        this.operandoA = operandoA;
        this.operandoB = operandoB;
        this.modoAngulo = modoAngulo;
        this.resultado = resultado;
        this.exito = exito;
        this.mensajeError = mensajeError;
        this.creadoEn = creadoEn;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getOperacionId() { return operacionId; }
    public void setOperacionId(int operacionId) { this.operacionId = operacionId; }

    public double getOperandoA() { return operandoA; }
    public void setOperandoA(double operandoA) { this.operandoA = operandoA; }

    public Double getOperandoB() { return operandoB; }
    public void setOperandoB(Double operandoB) { this.operandoB = operandoB; }

    public String getModoAngulo() { return modoAngulo; }
    public void setModoAngulo(String modoAngulo) { this.modoAngulo = modoAngulo; }

    public Double getResultado() { return resultado; }
    public void setResultado(Double resultado) { this.resultado = resultado; }

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public boolean isGuardado() { return guardado; }
    public void setGuardado(boolean guardado) { this.guardado = guardado; }
}