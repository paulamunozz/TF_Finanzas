package com.upc.cuentix.instances;

import com.upc.cuentix.dto.UsuarioDTO;

import java.util.List;

public interface IUsuarioService {
    UsuarioDTO login(UsuarioDTO usuarioDTO);
}