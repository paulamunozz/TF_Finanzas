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
    @Autowired private IRolRepository rolRepository;

    @Override
    public UsuarioDTO registrar(UsuarioDTO dto) {
        if (usuarioRepository.existsByCodigoAndEstado(dto.getCodigo(), true)) {
            throw new RuntimeException("Ya existe el usuario de código " + dto.getCodigo());
        }

        Usuario usuario = new Usuario();
        usuario.setCodigo(dto.getCodigo());
        usuario.setContrasena(dto.getContrasena());
        usuario.setRol(buscarRol(dto.getIdRol()));
        usuario.setEstado(true);

        return convertir(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO modificar(UsuarioDTO dto) {
        if (dto.getId() == null || !usuarioRepository.existsByIdAndEstado(dto.getId(), true)) {
            throw new RuntimeException("No existe el registro de id " + dto.getId());
        }

        Usuario usuario = usuarioRepository.findById(dto.getId()).get();

        if (tiene(dto.getCodigo()) && !dto.getCodigo().equals(usuario.getCodigo())
                && usuarioRepository.existsByCodigoAndEstado(dto.getCodigo(), true)) {
            throw new RuntimeException("El código " + dto.getCodigo() + " ya ha sido registrado");
        }

        if (tiene(dto.getCodigo())) usuario.setCodigo(dto.getCodigo());
        if (tiene(dto.getContrasena())) usuario.setContrasena(dto.getContrasena());
        if (dto.getIdRol() != null) usuario.setRol(buscarRol(dto.getIdRol()));

        return convertir(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public String eliminar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null || !usuario.getEstado()) {
            return "No se encontró el usuario";
        }
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
        return "Usuario eliminado exitosamente";
    }

    @Override
    public UsuarioDTO buscarPorId(Integer id) {
        if (id == null) return null;
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) return null;
        if (!usuario.getEstado()) throw new RuntimeException("Este usuario no existe");
        return convertir(usuario);
    }

    @Override
    public List<UsuarioDTO> listar() {
        return usuarioRepository.findByEstado(true).stream()
                .map(this::convertir).collect(Collectors.toList());
    }

    @Override
    public UsuarioDTO login(UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findByCodigoAndEstado(dto.getCodigo(), true);
        if (usuario == null || !usuario.getContrasena().equals(dto.getContrasena())) {
            throw new RuntimeException("Código o contraseña incorrectos");
        }
        return convertir(usuario);
    }

    private Rol buscarRol(Integer idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("No existe el rol de id " + idRol));
    }

    private boolean tiene(String s) {
        return s != null && !s.isBlank();
    }

    private UsuarioDTO convertir(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setCodigo(u.getCodigo());
        dto.setIdRol(u.getRol() != null ? u.getRol().getId() : null);
        dto.setEstado(u.getEstado());
        return dto;
    }
}