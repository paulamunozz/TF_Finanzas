package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.CuentaCreditoDTO;
import com.upc.cuentix.services.CuentaCreditoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CuentaCreditoController {
    @Autowired
    private CuentaCreditoService cuentaCreditoService;

    @PostMapping("/registrar")
    public CuentaCreditoDTO registrar(@Valid @RequestBody CuentaCreditoDTO dto) {
        return cuentaCreditoService.registrar(dto);
    }

    @GetMapping("/listar")
    public List<CuentaCreditoDTO> listar() {
        return cuentaCreditoService.listar();
    }

    @GetMapping("/buscar/{id}")
    public CuentaCreditoDTO buscarPorId(@PathVariable Integer id) {
        return cuentaCreditoService.buscarPorId(id);
    }

    @PutMapping("/modificar")
    public CuentaCreditoDTO modificar(@Valid @RequestBody CuentaCreditoDTO dto) {
        return cuentaCreditoService.modificar(dto);
    }

    @DeleteMapping("/eliminar/{id}")
    public void eliminar(@PathVariable Integer id) {
        cuentaCreditoService.eliminar(id);
    }
}
