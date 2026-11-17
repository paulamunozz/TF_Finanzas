package com.upc.cuentix.instances;

import com.upc.cuentix.dto.CuentaCreditoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ICuentaCreditoService  {
    CuentaCreditoDTO registrar(CuentaCreditoDTO dto);

    List<CuentaCreditoDTO> listar();

    CuentaCreditoDTO buscarPorId(Integer id);

    CuentaCreditoDTO modificar(CuentaCreditoDTO dto);

    void eliminar(Integer id);
}
