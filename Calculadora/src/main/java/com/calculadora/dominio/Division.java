/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.calculadora.dominio;

import com.calculadora.excepciones.DivisionPorCeroException;

public class Division extends OperacionBasica{
    
        public Division() {
        super("/", "Division", 2);
    }
    
    @Override
    public double calcular(double[] operandos) {
            if(operandos[1]==0){
            throw new DivisionPorCeroException("No se puede dividir por cero!!!!");
            }
        return operandos[0] / operandos[1];
    }
    
    
}
    
