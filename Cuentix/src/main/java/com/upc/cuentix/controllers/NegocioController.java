package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.NegocioDTO;
import com.upc.cuentix.services.NegocioService;
import com.upc.cuentix.validations.Create;
import com.upc.cuentix.validations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NegocioController {
    @Autowired private NegocioService negocioService;

    @PostMapping("/registrarNegocio")
    public NegocioDTO registrar(@RequestBody @Validated(Create.class) NegocioDTO negocioDTO) {
        return negocioService.registrar(negocioDTO);
    }

    @PutMapping("/modificarNegocio")
    public NegocioDTO modificar(@RequestBody @Validated(Update.class) NegocioDTO negocioDTO) {
        return negocioService.modificar(negocioDTO);
    }

    @DeleteMapping("/eliminarNegocio/{idNegocio}")
    public String eliminar(@PathVariable Integer idNegocio) {
        return negocioService.eliminar(idNegocio);
    }

    @PostMapping("/buscarNegocioPorRUC")
    public NegocioDTO buscarPorRuc(@RequestBody String ruc) {
        return negocioService.buscarPorRuc(ruc);
    }

    @PostMapping("/buscarNegocioPorID")
    public NegocioDTO buscarPorId(@RequestBody Integer idNegocio) {
        return negocioService.buscarPorId(idNegocio);
    }

    @GetMapping("/listarNegocios")
    public List<NegocioDTO> listar() {
        return negocioService.listar();
    }
}