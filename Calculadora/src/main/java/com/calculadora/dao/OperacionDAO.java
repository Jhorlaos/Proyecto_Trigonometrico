/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.calculadora.dao;

import com.calculadora.dto.OperacionDTO;
import java.util.List;

public interface OperacionDAO {

    OperacionDTO buscarPorSimbolo(String simbolo);

    List<OperacionDTO> listarOperaciones();
}
