/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calculadora.dominio;

/**
 *
 * @author Esteban
 */
public class Resta extends OperacionBasica{
    
        public Resta() {
        super("-", "Resta", 2);
    }
    
    @Override
    public double calcular(double[] operandos) {
        return operandos[0] - operandos[1];
    }
    
}
