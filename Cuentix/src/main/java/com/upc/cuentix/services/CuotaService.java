package com.upc.cuentix.services;

import com.upc.cuentix.dto.CuotaDTO;
import com.upc.cuentix.entities.Compra;
import com.upc.cuentix.entities.CuentaCredito;
import com.upc.cuentix.entities.Cuota;
import com.upc.cuentix.instances.ICuotaService;
import com.upc.cuentix.repositories.CompraRepository;
import com.upc.cuentix.repositories.CuentaCreditoRepository;
import com.upc.cuentix.repositories.CuotaRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuotaService implements ICuotaService {
    @Autowired private CuotaRepository cuotaRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private CuentaCreditoRepository ccRepository;
    @Autowired private ModelMapper modelMapper;

    @Transactional
    @Override
    public CuotaDTO pagarCuota(Integer idCuota) {
        Cuota cuota = cuotaRepository.findById(idCuota).get();
        if (cuotaRepository.existsByCompra_IdAndNumLessThanAndCanceladoFalse(
                cuota.getCompra().getId(), cuota.getNum()))
            throw new RuntimeException("Debe pagar primero las cuotas anteriores");

        cuota.setMoras(calculoMoras(cuota.getTotal(), cuota.getCompra().getTasaMora(), cuota.getFechaVencimiento()));
        cuota.setCancelado(true);
        cuota.setFechaPago(LocalDate.now());
        cuotaRepository.save(cuota);

        Compra compra = cuota.getCompra();
        compra.setMoras(compra.getMoras() + cuota.getMoras());
        compraRepository.save(compra);

        CuentaCredito cc = compra.getCuentaCredito();
        cc.setDeuda(cc.getDeuda() - cuota.getTotal());
        ccRepository.save(cc);

        return modelMapper.map(cuota, CuotaDTO.class);
    }

    @Override
    public List<CuotaDTO> cuotasPorCompra(Integer idCompra) {
        return cuotaRepository.findByCompra_Id(idCompra)
                .stream().map(cuota -> modelMapper.map(cuota, CuotaDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CuotaDTO> cuotasPorClienteMes(Integer idCliente) {
        return cuotaRepository.pendientesCliente(idCliente, YearMonth.now().atEndOfMonth()).stream()
                .map(c -> {
                    CuotaDTO dto = modelMapper.map(c, CuotaDTO.class);
                    double mora = calculoMoras(c.getTotal(), c.getCompra().getTasaMora(),
                            c.getFechaVencimiento());
                    dto.setMoras(mora);
                    dto.setTotal(c.getTotal() + mora);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CuotaDTO> cuotasPorNegocioMes(Integer idNegocio) {
        LocalDate fechaLimite = LocalDate.now().plusMonths(1);
        return cuotaRepository.pendientesNegocio(idNegocio, YearMonth.now().atEndOfMonth()).stream()
                .map(c -> {
                    CuotaDTO dto = modelMapper.map(c, CuotaDTO.class);
                    double mora = calculoMoras(c.getTotal(), c.getCompra().getTasaMora(),
                            c.getFechaVencimiento());
                    dto.setMoras(mora);
                    dto.setTotal(c.getTotal() + mora);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Double calculoMoras(Double C, Double TED, LocalDate fechaVencimiento) {
        if (LocalDate.now().isAfter(fechaVencimiento)) {
            Long n = ChronoUnit.DAYS.between(fechaVencimiento, LocalDate.now());
            return C * (Math.pow(1 + TED, n) -1);
        }
        return 0.0;
    }
}
