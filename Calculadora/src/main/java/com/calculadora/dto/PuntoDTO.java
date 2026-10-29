package com.calculadora.dto;

public class PuntoDTO {
    private Integer id;
    private Integer serieId;
    private double x;
    private Double y; // Puede ser null

    public PuntoDTO(double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getSerieId() { return serieId; }
    public void setSerieId(Integer serieId) { this.serieId = serieId; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }
}