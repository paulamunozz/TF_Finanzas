package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.UsuarioDTO;
import com.upc.cuentix.services.UsuarioService;
import com.upc.cuentix.validations.Create;
import com.upc.cuentix.validations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UsuarioController {
    @Autowired private UsuarioService usuarioService;

    @PostMapping("/registrarUsuario")
    public UsuarioDTO registrar(@RequestBody @Validated(Create.class) UsuarioDTO usuarioDTO) {
        return usuarioService.registrar(usuarioDTO);
    }

    @PutMapping("/modificarUsuario")
    public UsuarioDTO modificar(@RequestBody @Validated(Update.class) UsuarioDTO usuarioDTO) {
        return usuarioService.modificar(usuarioDTO);
    }

    @DeleteMapping("/eliminarUsuario/{idUsuario}")
    public String eliminar(@PathVariable Integer idUsuario) {
        return usuarioService.eliminar(idUsuario);
    }

    @PostMapping("/buscarUsuarioPorID")
    public UsuarioDTO buscarPorId(@RequestBody Integer idUsuario) {
        return usuarioService.buscarPorId(idUsuario);
    }

    @GetMapping("/listarUsuarios")
    public List<UsuarioDTO> listar() {
        return usuarioService.listar();
    }

    @PostMapping("/login")
    public UsuarioDTO login(@RequestBody @Validated(Create.class) UsuarioDTO usuarioDTO) {
        return usuarioService.login(usuarioDTO);
    }
}