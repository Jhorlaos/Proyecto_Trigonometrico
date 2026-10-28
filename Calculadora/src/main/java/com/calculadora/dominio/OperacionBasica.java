/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calculadora.dominio;

/**
 *
 * @author Esteban
 */
public abstract class OperacionBasica implements Operacion {
    
    protected String simbolo;
    protected String nombre;
    protected int aridad;
    
    public OperacionBasica(String simbolo, String nombre,int aridad ){
        this.simbolo = simbolo;
        this.nombre = nombre;
        this.aridad = aridad;
    }

    @Override
    public String getSimbolo(){
    return simbolo;
    }
    @Override
    public String getNombre(){
    return nombre;
    }
    @Override
    public int getAridad(){
    return aridad;
    }

}
