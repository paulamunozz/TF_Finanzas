package com.upc.cuentix.instances;

import com.upc.cuentix.dto.CompraDTO;
import com.upc.cuentix.entities.Compra;
import com.upc.cuentix.entities.Tasa;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface ICompraService {
    CompraDTO preRegistrar(CompraDTO compraDTO);
    CompraDTO registrar(CompraDTO compraDTO);
    void eliminar(Integer id);
    List<CompraDTO> comprasPorNegocio(Integer negocioId);
    List<CompraDTO> comprasPorCliente(Integer clienteId);
    List<CompraDTO> comprasPorCuentaCredito(Integer cuentaCreditoId);
    CompraDTO buscarPorId(Integer id);
    Double conversorTEM(Tasa tasa, Integer periodicidad);
    Double sumaTotalInicial(Integer compraId, Boolean contadoCredito);
    Double totalGracia(Compra compra);
    LocalDate calcularFechaPago(Compra compra);
    LocalDate diaSeguro(YearMonth ym, int dia);
    Double calculoCuota(Compra compra);
}