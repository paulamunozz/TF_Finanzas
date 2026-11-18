package com.upc.cuentix.instances;

import com.upc.cuentix.dto.CuotaDTO;
import com.upc.cuentix.entities.CuentaCredito;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface ICuotaService{
    CuotaDTO pagarCuota(Integer idCuota);
    List<CuotaDTO> cuotasPorCompra(Integer idCompra);
    List<CuotaDTO> cuotasPorClienteMes(Integer idCliente);
    List<CuotaDTO> cuotasPorNegocioMes(Integer idNegocio);
}
