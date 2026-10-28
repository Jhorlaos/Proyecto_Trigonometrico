/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.calculadora.dominio;

/**
 *
 * @author Esteban
 */
public interface Operacion {
    
    String getSimbolo();
    String getNombre();
    int getAridad();
    
    double calcular(double[] operandos);
    
    
}
