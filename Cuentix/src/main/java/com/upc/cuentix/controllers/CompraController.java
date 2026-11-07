package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.CompraDTO;
import com.upc.cuentix.services.CompraService;
import com.upc.cuentix.validations.Create;
import com.upc.cuentix.validations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping
public class CompraController {
    @Autowired private CompraService compraService;

    @PostMapping("/preRegistrarCompra")
    public CompraDTO preRegistrar(@RequestBody @Validated(Create.class) CompraDTO compraDTO) {
        return compraService.preRegistrar(compraDTO);
    }

    @PutMapping("/registrarCompra")
    public CompraDTO registrar(@RequestBody @Validated(Update.class) CompraDTO compraDTO) {
        return compraService.registrar(compraDTO);
    }

    @DeleteMapping("/eliminarCompra/{idCompra}")
    public void eliminar(@PathVariable Integer idCompra) {
        compraService.eliminar(idCompra);
    }

    @PostMapping("/comprasPorNegocio")
    public List<CompraDTO> comprasPorNegocio(@RequestBody Integer negocioId) {
        return compraService.comprasPorNegocio(negocioId);
    }

    @PostMapping("/comprasPorCliente")
    public List<CompraDTO> comprasPorCliente(@RequestBody Integer clienteId) {
        return compraService.comprasPorCliente(clienteId);
    }

    @PostMapping("/comprasPorCuentaCredito")
    public List<CompraDTO> comprasPorCuentaCredito(@RequestBody Integer cuentaCreditoId) {
        return compraService.comprasPorCuentaCredito(cuentaCreditoId);
    }

    @PostMapping("/comprarPorID")
    public CompraDTO buscarPorId(@RequestBody Integer id) {
        return compraService.buscarPorId(id);
    }
}
