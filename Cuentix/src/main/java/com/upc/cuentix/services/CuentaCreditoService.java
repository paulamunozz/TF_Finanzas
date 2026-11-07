package com.upc.cuentix.services;

import com.upc.cuentix.dto.ClienteDTO;
import com.upc.cuentix.dto.CuentaCreditoDTO;
import com.upc.cuentix.entities.Cliente;
import com.upc.cuentix.entities.CuentaCredito;
import com.upc.cuentix.entities.Moneda;
import com.upc.cuentix.entities.Negocio;
import com.upc.cuentix.instances.ICuentaCreditoService;
import com.upc.cuentix.repositories.ICuentaCreditoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuentaCreditoService implements ICuentaCreditoService {
    @Autowired
    private ICuentaCreditoRepository cuentaCreditoRepository;

    @Override
    public CuentaCreditoDTO registrar(CuentaCreditoDTO dto) {
        CuentaCredito cuenta = new CuentaCredito();
        copiarDatos(dto, cuenta);
        return aDTO(cuentaCreditoRepository.save(cuenta));
    }

    @Override
    public List<CuentaCreditoDTO> listar() {
        return cuentaCreditoRepository.findAll()
                .stream()
                .map(this::aDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CuentaCreditoDTO buscarPorId(Integer id) {
        CuentaCredito cuenta = cuentaCreditoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta de crédito no encontrada con id: " + id));
        return aDTO(cuenta);
    }

    @Override
    public CuentaCreditoDTO modificar(CuentaCreditoDTO dto) {
        CuentaCredito cuenta = cuentaCreditoRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Cuenta de crédito no encontrada con id: " + dto.getId()));
        copiarDatos(dto, cuenta);
        return aDTO(cuentaCreditoRepository.save(cuenta));
    }


    @Override
    public void eliminar(Integer id) {
        if (!cuentaCreditoRepository.existsById(id)) {
            throw new RuntimeException("Cuenta de crédito no encontrada con id: " + id);
        }
        cuentaCreditoRepository.deleteById(id);
    }

    private void copiarDatos(CuentaCreditoDTO dto, CuentaCredito cuenta) {
        cuenta.setDeuda(dto.getDeuda());
        cuenta.setLimite(dto.getLimite());
        cuenta.setMaxMeses(dto.getMaxMeses());
        cuenta.setDiaCorte(dto.getDiaCorte());
        cuenta.setDiaPago(dto.getDiaPago());
        if (dto.getEstado() != null) {
            cuenta.setEstado(dto.getEstado());
        }

        Moneda moneda = new Moneda();
        moneda.setId(dto.getMonedaId());
        cuenta.setMoneda(moneda);

        Negocio negocio = new Negocio();
        negocio.setId(dto.getNegocioId());
        cuenta.setNegocio(negocio);

        Cliente cliente = new Cliente();
        cliente.setId(dto.getClienteId());
        cuenta.setCliente(cliente);
    }

    private CuentaCreditoDTO aDTO(CuentaCredito cuenta) {
        CuentaCreditoDTO dto = new CuentaCreditoDTO();
        dto.setId(cuenta.getId());
        dto.setDeuda(cuenta.getDeuda());
        dto.setLimite(cuenta.getLimite());
        dto.setMaxMeses(cuenta.getMaxMeses());
        dto.setDiaCorte(cuenta.getDiaCorte());
        dto.setDiaPago(cuenta.getDiaPago());
        dto.setEstado(cuenta.getEstado());
        dto.setMonedaId(cuenta.getMoneda() != null ? cuenta.getMoneda().getId() : null);
        dto.setNegocioId(cuenta.getNegocio() != null ? cuenta.getNegocio().getId() : null);
        dto.setClienteId(cuenta.getCliente() != null ? cuenta.getCliente().getId() : null);
        return dto;
    }
}
