package com.calculadora.dto;

public class OperacionDTO {

    private int id;
    private String simbolo;
    private String nombre;
    private String tipo;
    private String lenguaje;
    private int aridad;

    // Constructor vacío obligatorio
    public OperacionDTO() {}

    // Constructor completo para mapear lecturas de la Base de Datos
    public OperacionDTO(int id, String simbolo, String nombre, String tipo, String lenguaje, int aridad) {
        this.id = id;
        this.simbolo = simbolo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.lenguaje = lenguaje;
        this.aridad = aridad;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSimbolo() { return simbolo; }
    public void setSimbolo(String simbolo) { this.simbolo = simbolo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getLenguaje() { return lenguaje; }
    public void setLenguaje(String lenguaje) { this.lenguaje = lenguaje; }

    public int getAridad() { return aridad; }
    public void setAridad(int aridad) { this.aridad = aridad; }
}