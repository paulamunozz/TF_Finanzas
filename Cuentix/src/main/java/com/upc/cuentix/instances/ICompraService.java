package com.upc.cuentix.instances;

import com.upc.cuentix.dto.CompraDTO;
import com.upc.cuentix.entities.Compra;
import com.upc.cuentix.entities.Tasa;

import java.util.List;

public interface ICompraService {
    CompraDTO preRegistrar(CompraDTO compraDTO);
    CompraDTO registrar(CompraDTO compraDTO);
    void eliminar(Integer id);
    List<CompraDTO> comprasPorNegocio(Integer negocioId);
    List<CompraDTO> comprasPorCliente(Integer clienteId);
    List<CompraDTO> comprasPorCuentaCredito(Integer cuentaCreditoId);
    CompraDTO buscarPorId(Integer id);
    Double conversorTNA(Tasa tasa);
    Double sumaTotalInicial(Integer compraId, Boolean contadoCredito);
    Double totalGracia(Compra compra);
    Double intereses(Compra compra);
}
