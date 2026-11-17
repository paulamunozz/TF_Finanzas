package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.CuotaDTO;
import com.upc.cuentix.services.CuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class CuotaController {
    @Autowired
    CuotaService cuotaService;

    @PutMapping("/pagarCuota")
    public CuotaDTO pagarCuota(@RequestBody Integer idCuota) {
        return cuotaService.pagarCuota(idCuota);
    }

    @PostMapping("/cuotasPorCompra")
    public List<CuotaDTO> cuotasPorCompra(@RequestBody Integer idCompra) {
        return cuotaService.cuotasPorCompra(idCompra);
    }

    @PostMapping("/cuotasPorClienteMes")
    public List<CuotaDTO> cuotasPorClienteMes(@RequestBody Integer idCliente) {
        return cuotaService.cuotasPorClienteMes(idCliente);
    }

    @PostMapping("/cuotasPorNegocioMes")
    public List<CuotaDTO> cuotasPorNegocioMes(@RequestBody Integer idNegocio) {
        return cuotaService.cuotasPorNegocioMes(idNegocio);
    }
}
