package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.ClienteDTO;
import com.upc.cuentix.services.ClienteService;
import com.upc.cuentix.validations.Create;
import com.upc.cuentix.validations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClienteController {
    @Autowired private ClienteService clienteService;

    @PostMapping("/registrarCliente")
    public ClienteDTO registrar(@RequestBody @Validated(Create.class) ClienteDTO clienteDTO) {
        return clienteService.registrar(clienteDTO);
    }

    @PutMapping("/modificarCliente")
    public ClienteDTO modificar(@RequestBody @Validated(Update.class) ClienteDTO clienteDTO) {
        return clienteService.modificar(clienteDTO);
    }

    @DeleteMapping("/eliminarCliente/{idCliente}")
    public String eliminar(@PathVariable Integer idCliente) {
        return clienteService.eliminar(idCliente);
    }

    @PostMapping("/buscarClientePorDNI")
    public ClienteDTO buscarPorDni(@RequestBody String dni) {
        return clienteService.buscarPorDni(dni);
    }

    @PostMapping("/buscarClientePorID")
    public ClienteDTO buscarPorId(@RequestBody Integer idCliente) {
        return clienteService.buscarPorId(idCliente);
    }
}