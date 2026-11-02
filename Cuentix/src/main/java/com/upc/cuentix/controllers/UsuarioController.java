package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.UsuarioDTO;
import com.upc.cuentix.services.UsuarioService;
import com.upc.cuentix.validations.Create;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class UsuarioController {
    @Autowired private UsuarioService usuarioService;

    @PostMapping("/login")
    public UsuarioDTO login(@RequestBody @Validated(Create.class) UsuarioDTO usuarioDTO) {
        return usuarioService.login(usuarioDTO);
    }
}