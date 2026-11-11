package com.upc.cuentix.services;

import com.upc.cuentix.dto.ClienteDTO;
import com.upc.cuentix.entities.Cliente;
import com.upc.cuentix.entities.Rol;
import com.upc.cuentix.entities.Usuario;
import com.upc.cuentix.instances.IClienteService;
import com.upc.cuentix.repositories.ClienteRepository;
import com.upc.cuentix.repositories.RolRepository;
import com.upc.cuentix.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


@Service
public class ClienteService implements IClienteService {

    private static final Integer ROL_CLIENTE = 2;
    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;

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
        usuario.setContrasena(
                encoder.encode(dto.getContrasena())
        );
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
        if (tiene(dto.getContrasena())) {
            usuario.setContrasena(
                    encoder.encode(dto.getContrasena())
            );
        }
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

    @Transactional
    public ClienteDTO actualizarMiPerfil(
            Integer usuarioId,
            ClienteDTO datos
    ) {

        // Buscar al cliente vinculado al usuario autenticado
        Cliente cliente =
                clienteRepository.findByUsuario_Id(usuarioId);

        if (cliente == null ||
                !Boolean.TRUE.equals(cliente.getEstado()) ||
                !Boolean.TRUE.equals(cliente.getUsuario().getEstado()) ||
                cliente.getUsuario().getRol() == null ||
                !Integer.valueOf(2).equals(
                        cliente.getUsuario().getRol().getId()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puede modificar este perfil"
            );
        }

        // Obtener los datos modificados
        String nombre = datos.getNombre();
        String apellido = datos.getApellido();
        String telefono = datos.getTelefono();

        // Validar campos obligatorios
        if (nombre == null || nombre.isBlank() ||
                apellido == null || apellido.isBlank() ||
                telefono == null || telefono.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Complete los campos obligatorios"
            );
        }

        nombre = nombre.trim();
        apellido = apellido.trim();
        telefono = telefono.trim();

        // Validar nombre y apellido
        if (!nombre.matches("^[\\p{L} .'-]+$") ||
                !apellido.matches("^[\\p{L} .'-]+$") ||
                nombre.length() > 255 ||
                apellido.length() > 255) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre o apellido inválido"
            );
        }

        // Validar teléfono peruano
        if (!telefono.matches("^9\\d{8}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El teléfono debe tener 9 dígitos"
            );
        }

        Usuario usuario = cliente.getUsuario();

        // Cambio opcional de contraseña
        if (datos.getContrasena() != null &&
                !datos.getContrasena().isBlank()) {

            String nueva = datos.getContrasena();
            String actual = datos.getContrasenaActual();

            if (nueva.length() < 8 ||
                    nueva.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                    ).length > 72) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La nueva contraseña debe tener entre 8 caracteres y 72 bytes"
                );
            }

            if (actual == null || actual.isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ingrese su contraseña actual"
                );
            }

            String guardada = usuario.getContrasena();

            boolean esHash =
                    guardada.startsWith("$2a$") ||
                            guardada.startsWith("$2b$") ||
                            guardada.startsWith("$2y$");

            boolean correcta = esHash
                    ? encoder.matches(actual, guardada)
                    : guardada.equals(actual);

            if (!correcta) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La contraseña actual es incorrecta"
                );
            }

            usuario.setContrasena(
                    encoder.encode(nueva)
            );

            usuarioRepository.save(usuario);
        }

        // Actualizar los datos personales
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setTelefono(telefono);

        // Guardar los cambios en PostgreSQL
        return convertir(clienteRepository.save(cliente));
    }

    @Transactional
    public void eliminarMiCuenta(
            Integer usuarioId,
            String contrasenaActual
    ) {

        if (contrasenaActual == null ||
                contrasenaActual.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ingrese su contraseña actual"
            );
        }

        // Buscar al cliente asociado a la sesión.
        Cliente cliente =
                clienteRepository.findByUsuario_Id(usuarioId);

        if (cliente == null ||
                !Boolean.TRUE.equals(cliente.getEstado()) ||
                cliente.getUsuario() == null ||
                !Boolean.TRUE.equals(cliente.getUsuario().getEstado()) ||
                cliente.getUsuario().getRol() == null ||
                !ROL_CLIENTE.equals(
                        cliente.getUsuario().getRol().getId()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puede eliminar esta cuenta"
            );
        }

        Usuario usuario = cliente.getUsuario();

        // Verificar la contraseña actual.
        String guardada = usuario.getContrasena();

        boolean esHash =
                guardada.startsWith("$2a$") ||
                        guardada.startsWith("$2b$") ||
                        guardada.startsWith("$2y$");

        boolean correcta = esHash
                ? encoder.matches(contrasenaActual, guardada)
                : guardada.equals(contrasenaActual);

        if (!correcta) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña actual es incorrecta"
            );
        }

        // Eliminación lógica del cliente y usuario.
        cliente.setEstado(false);
        usuario.setEstado(false);

        clienteRepository.save(cliente);
        usuarioRepository.save(usuario);
    }
}