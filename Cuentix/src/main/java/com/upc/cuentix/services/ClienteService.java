package com.upc.cuentix.services;

import com.upc.cuentix.dto.ClienteDTO;
import com.upc.cuentix.entities.Cliente;
import com.upc.cuentix.entities.Rol;
import com.upc.cuentix.entities.Usuario;
import com.upc.cuentix.instances.IClienteService;
import com.upc.cuentix.repositories.IClienteRepository;
import com.upc.cuentix.repositories.IRolRepository;
import com.upc.cuentix.repositories.IUsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClienteService implements IClienteService {

    private static final Integer ROL_CLIENTE = 2;

    @Autowired private IClienteRepository clienteRepository;
    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private IRolRepository rolRepository;

    @Override
    @Transactional
    public ClienteDTO registrar(ClienteDTO dto) {
        if (usuarioRepository.existsByCodigoAndEstado(dto.getDni(), true)) {
            throw new RuntimeException("Ya existe el cliente de DNI " + dto.getDni());
        }

        Rol rol = rolRepository.findById(ROL_CLIENTE)
                .orElseThrow(() -> new RuntimeException("No existe el rol de cliente"));

        Usuario usuario = new Usuario();
        usuario.setCodigo(dto.getDni());
        usuario.setContrasena(dto.getContrasena());
        usuario.setRol(rol);
        usuario.setEstado(true);
        usuario = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEstado(true);
        cliente.setUsuario(usuario);

        return convertir(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteDTO modificar(ClienteDTO dto) {
        if (!clienteRepository.existsByIdAndEstado(dto.getId(), true)) {
            throw new RuntimeException("No existe el registro de id " + dto.getId());
        }

        Cliente cliente = clienteRepository.findById(dto.getId()).get();
        Usuario usuario = cliente.getUsuario();

        if (tiene(dto.getDni()) && !dto.getDni().equals(usuario.getCodigo())
                && usuarioRepository.existsByCodigoAndEstado(dto.getDni(), true)) {
            throw new RuntimeException("El DNI " + dto.getDni() + " ya ha sido registrado");
        }

        if (tiene(dto.getDni())) usuario.setCodigo(dto.getDni());
        if (tiene(dto.getContrasena())) usuario.setContrasena(dto.getContrasena());
        if (tiene(dto.getNombre())) cliente.setNombre(dto.getNombre());
        if (tiene(dto.getApellido())) cliente.setApellido(dto.getApellido());
        if (tiene(dto.getTelefono())) cliente.setTelefono(dto.getTelefono());

        usuarioRepository.save(usuario);
        return convertir(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public String eliminar(Integer id) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null || !cliente.getEstado()) {
            return "No se encontró el cliente";
        }
        cliente.setEstado(false);
        clienteRepository.save(cliente);
        cliente.getUsuario().setEstado(false);
        usuarioRepository.save(cliente.getUsuario());
        return "Cliente eliminado exitosamente";
    }

    @Override
    public ClienteDTO buscarPorDni(String dni) {
        if (!tiene(dni)) throw new RuntimeException("Ingrese el DNI");
        Cliente cliente = clienteRepository.findByUsuario_Codigo(dni);
        if (cliente == null) throw new RuntimeException("Este DNI no se encuentra registrado");
        if (!cliente.getEstado()) throw new RuntimeException("Este cliente no existe");
        return convertir(cliente);
    }

    @Override
    public ClienteDTO buscarPorId(Integer id) {
        if (id == null) return null;
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) return null;
        if (!cliente.getEstado()) throw new RuntimeException("Este cliente no existe");
        return convertir(cliente);
    }

    private boolean tiene(String s) {
        return s != null && !s.isBlank();
    }

    private ClienteDTO convertir(Cliente c) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(c.getId());
        dto.setDni(c.getUsuario().getCodigo());
        dto.setNombre(c.getNombre());
        dto.setApellido(c.getApellido());
        dto.setTelefono(c.getTelefono());
        dto.setEstado(c.getEstado());
        return dto;
    }
}