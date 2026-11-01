package com.upc.cuentix.services;

import com.upc.cuentix.dto.UsuarioDTO;
import com.upc.cuentix.entities.Rol;
import com.upc.cuentix.entities.Usuario;
import com.upc.cuentix.instances.IUsuarioService;
import com.upc.cuentix.repositories.IRolRepository;
import com.upc.cuentix.repositories.IUsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired private IUsuarioRepository usuarioRepository;

    @Override
    public UsuarioDTO login(UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findByCodigoAndEstado(dto.getCodigo(), true);
        if (usuario == null || !usuario.getContrasena().equals(dto.getContrasena())) {
            throw new RuntimeException("Código o contraseña incorrectos");
        }
        return convertir(usuario);
    }

    private UsuarioDTO convertir(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setCodigo(u.getCodigo());
        dto.setRolId(u.getRol() != null ? u.getRol().getId() : null);
        dto.setEstado(u.getEstado());
        return dto;
    }
}