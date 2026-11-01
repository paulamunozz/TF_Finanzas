package com.upc.cuentix.services;

import com.upc.cuentix.dto.NegocioDTO;
import com.upc.cuentix.entities.Giro;
import com.upc.cuentix.entities.Negocio;
import com.upc.cuentix.entities.Rol;
import com.upc.cuentix.entities.Usuario;
import com.upc.cuentix.instances.INegocioService;
import com.upc.cuentix.repositories.IGiroRepository;
import com.upc.cuentix.repositories.INegocioRepository;
import com.upc.cuentix.repositories.IRolRepository;
import com.upc.cuentix.repositories.IUsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NegocioService implements INegocioService {

    private static final Integer ROL_NEGOCIO = 3;

    @Autowired private INegocioRepository negocioRepository;
    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private IRolRepository rolRepository;
    @Autowired private IGiroRepository giroRepository;

    @Override
    @Transactional
    public NegocioDTO registrar(NegocioDTO dto) {
        if (usuarioRepository.existsByCodigoAndEstado(dto.getRuc(), true)) {
            throw new RuntimeException("Ya existe el negocio de RUC " + dto.getRuc());
        }

        Rol rol = rolRepository.findById(ROL_NEGOCIO)
                .orElseThrow(() -> new RuntimeException("No existe el rol de negocio"));
        Giro giro = buscarGiro(dto.getIdGiro());

        Usuario usuario = new Usuario();
        usuario.setCodigo(dto.getRuc());
        usuario.setContrasena(dto.getContrasena());
        usuario.setRol(rol);
        usuario.setEstado(true);
        usuario = usuarioRepository.save(usuario);

        Negocio negocio = new Negocio();
        negocio.setRazonSocial(dto.getRazonSocial());
        negocio.setGiro(giro);
        negocio.setDireccion(dto.getDireccion());
        negocio.setTelefono(dto.getTelefono());
        negocio.setEstado(true);
        negocio.setUsuario(usuario);

        return convertir(negocioRepository.save(negocio));
    }

    @Override
    @Transactional
    public NegocioDTO modificar(NegocioDTO dto) {
        if (!negocioRepository.existsByIdAndEstado(dto.getId(), true)) {
            throw new RuntimeException("No existe el registro de id " + dto.getId());
        }

        Negocio negocio = negocioRepository.findById(dto.getId()).get();
        Usuario usuario = negocio.getUsuario();

        if (tiene(dto.getRuc()) && !dto.getRuc().equals(usuario.getCodigo())
                && usuarioRepository.existsByCodigoAndEstado(dto.getRuc(), true)) {
            throw new RuntimeException("El RUC " + dto.getRuc() + " ya ha sido registrado");
        }

        if (tiene(dto.getRuc())) usuario.setCodigo(dto.getRuc());
        if (tiene(dto.getContrasena())) usuario.setContrasena(dto.getContrasena());
        if (tiene(dto.getRazonSocial())) negocio.setRazonSocial(dto.getRazonSocial());
        if (dto.getIdGiro() != null) negocio.setGiro(buscarGiro(dto.getIdGiro()));
        if (tiene(dto.getDireccion())) negocio.setDireccion(dto.getDireccion());
        if (tiene(dto.getTelefono())) negocio.setTelefono(dto.getTelefono());

        usuarioRepository.save(usuario);
        return convertir(negocioRepository.save(negocio));
    }

    @Override
    @Transactional
    public String eliminar(Integer id) {
        Negocio negocio = negocioRepository.findById(id).orElse(null);
        if (negocio == null || !negocio.getEstado()) {
            return "No se encontró el negocio";
        }
        negocio.setEstado(false);
        negocioRepository.save(negocio);
        negocio.getUsuario().setEstado(false);
        usuarioRepository.save(negocio.getUsuario());
        return "Negocio eliminado exitosamente";
    }

    @Override
    public NegocioDTO buscarPorRuc(String ruc) {
        if (!tiene(ruc)) throw new RuntimeException("Ingrese el RUC");
        Negocio negocio = negocioRepository.findByUsuario_Codigo(ruc);
        if (negocio == null) throw new RuntimeException("Este RUC no se encuentra registrado");
        if (!negocio.getEstado()) throw new RuntimeException("Este negocio no existe");
        return convertir(negocio);
    }

    @Override
    public NegocioDTO buscarPorId(Integer id) {
        if (id == null) return null;
        Negocio negocio = negocioRepository.findById(id).orElse(null);
        if (negocio == null) return null;
        if (!negocio.getEstado()) throw new RuntimeException("Este negocio no existe");
        return convertir(negocio);
    }

    @Override
    public List<NegocioDTO> listar() {
        return negocioRepository.findByEstado(true).stream()
                .map(this::convertir).collect(Collectors.toList());
    }

    private Giro buscarGiro(Integer idGiro) {
        return giroRepository.findById(idGiro)
                .orElseThrow(() -> new RuntimeException("No existe el giro de id " + idGiro));
    }

    private boolean tiene(String s) {
        return s != null && !s.isBlank();
    }

    private NegocioDTO convertir(Negocio n) {
        NegocioDTO dto = new NegocioDTO();
        dto.setId(n.getId());
        dto.setRuc(n.getUsuario().getCodigo());
        dto.setRazonSocial(n.getRazonSocial());
        dto.setIdGiro(n.getGiro() != null ? n.getGiro().getId() : null);
        dto.setDireccion(n.getDireccion());
        dto.setTelefono(n.getTelefono());
        dto.setEstado(n.getEstado());
        return dto;
    }
}