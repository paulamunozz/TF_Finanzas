package com.upc.cuentix.instances;

import com.upc.cuentix.dto.UsuarioDTO;

import java.util.List;

public interface IUsuarioService {
    UsuarioDTO registrar(UsuarioDTO usuarioDTO);
    UsuarioDTO modificar(UsuarioDTO usuarioDTO);
    String eliminar(Integer id);
    UsuarioDTO buscarPorId(Integer id);
    List<UsuarioDTO> listar();
    UsuarioDTO login(UsuarioDTO usuarioDTO);
}