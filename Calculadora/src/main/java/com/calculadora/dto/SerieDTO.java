package com.calculadora.dto;

import java.time.LocalDateTime;

public class SerieDTO {
    private Integer id;
    private int operacionId;
    private String nombre;
    private double xMin;
    private double xMax;
    private double paso;
    private String modoAngulo;
    private LocalDateTime creadoEn;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getOperacionId() { return operacionId; }
    public void setOperacionId(int operacionId) { this.operacionId = operacionId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getxMin() { return xMin; }
    public void setxMin(double xMin) { this.xMin = xMin; }

    public double getxMax() { return xMax; }
    public void setxMax(double xMax) { this.xMax = xMax; }

    public double getPaso() { return paso; }
    public void setPaso(double paso) { this.paso = paso; }

    public String getModoAngulo() { return modoAngulo; }
    public void setModoAngulo(String modoAngulo) { this.modoAngulo = modoAngulo; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}