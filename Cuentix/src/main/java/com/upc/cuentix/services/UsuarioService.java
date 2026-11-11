
package com.upc.cuentix.services;

import com.upc.cuentix.dto.UsuarioDTO;
import com.upc.cuentix.entities.Usuario;
import com.upc.cuentix.entities.Cliente;
import com.upc.cuentix.instances.IUsuarioService;
import com.upc.cuentix.repositories.UsuarioRepository;
import com.upc.cuentix.repositories.ClienteRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public UsuarioDTO login(UsuarioDTO dto) {

        Usuario usuario = usuarioRepository
                .findByCodigoAndEstado(dto.getCodigo(), true);

        if (usuario == null) {
            throw new RuntimeException(
                    "Código o contraseña incorrectos"
            );
        }

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String almacenada = usuario.getContrasena();

        boolean esHash =
                almacenada.startsWith("$2a$") ||
                        almacenada.startsWith("$2b$") ||
                        almacenada.startsWith("$2y$");

        boolean coincide = esHash
                ? encoder.matches(dto.getContrasena(), almacenada)
                : almacenada.equals(dto.getContrasena());

        if (!coincide) {
            throw new RuntimeException(
                    "Código o contraseña incorrectos"
            );
        }

        // Migrar contraseñas antiguas de prueba a BCrypt.
        if (!esHash) {
            usuario.setContrasena(
                    encoder.encode(dto.getContrasena())
            );
            usuarioRepository.save(usuario);
        }

        return convertir(usuario);
    }

    private UsuarioDTO convertir(Usuario usuario) {

        UsuarioDTO dto = new UsuarioDTO();

        dto.setId(usuario.getId());
        dto.setCodigo(usuario.getCodigo());
        dto.setRolId(
                usuario.getRol() != null
                        ? usuario.getRol().getId()
                        : null
        );
        dto.setEstado(usuario.getEstado());

        // Obtener los datos del cliente
        // después de validar sus credenciales.
        if (usuario.getRol() != null &&
                Integer.valueOf(2).equals(usuario.getRol().getId())) {

            Cliente cliente = clienteRepository
                    .findByUsuario_Codigo(usuario.getCodigo());

            if (cliente != null &&
                    Boolean.TRUE.equals(cliente.getEstado())) {

                dto.setNombre(cliente.getNombre());
                dto.setApellido(cliente.getApellido());
                dto.setTelefono(cliente.getTelefono());
            }
        }

        return dto;
    }
}