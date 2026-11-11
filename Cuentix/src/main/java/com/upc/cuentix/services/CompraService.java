package com.upc.cuentix.services;

import com.upc.cuentix.dto.CompraDTO;
import com.upc.cuentix.entities.Compra;
import com.upc.cuentix.entities.Cuota;
import com.upc.cuentix.entities.Item;
import com.upc.cuentix.entities.Tasa;
import com.upc.cuentix.instances.ICompraService;
import com.upc.cuentix.repositories.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.upc.cuentix.entities.CuentaCredito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompraService implements ICompraService{
    @Autowired private CompraRepository compraRepository;
    @Autowired private CuentaCreditoRepository cuentaCreditoRepository;
    @Autowired private TasaRepository tasaRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CuotaRepository cuotaRepository;
    @Autowired private ModelMapper modelMapper;


    @Override
    public CompraDTO preRegistrar(CompraDTO compraDTO) {

        // Verificar que se envió el ID de la cuenta.
        if (compraDTO.getCcId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar una cuenta de crédito"
            );
        }

        // Buscar la cuenta de crédito en PostgreSQL.
        CuentaCredito cuenta = cuentaCreditoRepository
                .findById(compraDTO.getCcId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "La cuenta de crédito no existe"
                        )
                );

        if (!Boolean.TRUE.equals(cuenta.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cuenta de crédito no está activa"
            );
        }

        // Validar el plazo de la compra a crédito.
        if (Boolean.TRUE.equals(compraDTO.getContadoCredito())) {
            if (compraDTO.getNumMeses() == null ||
                    compraDTO.getNumMeses() < 1 ||
                    compraDTO.getNumMeses() > cuenta.getMaxMeses()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El plazo de la compra no es válido"
                );
            }
        }

        // Convertir el DTO a entidad.
        Compra compra = modelMapper.map(
                compraDTO,
                Compra.class
        );

        // Asociar explícitamente la cuenta de crédito.
        compra.setCuentaCredito(cuenta);

        // Guardar la compra.
        Compra compraGuardada = compraRepository.save(compra);

        // Preparar respuesta.
        CompraDTO respuesta = modelMapper.map(
                compraGuardada,
                CompraDTO.class
        );

        respuesta.setCcId(cuenta.getId());

        return respuesta;
    }

    @Override
    public CompraDTO registrar(CompraDTO compraDTO) {
        Compra compra = compraRepository.findById(compraDTO.getId()).get();
        compra.setFecha(LocalDate.now());
        compra.setTotalInicial(sumaTotalInicial(compra.getId(), compraDTO.getContadoCredito()));
        compra.setMoras(0.0);

        if (!compra.getContadoCredito()){
            compra.setTasa(0.0);
            compra.setTasaMora(0.0);
            compra.setTotalGracia(0.0);
            compra.setIntereses(0.0);
            compra.setTotalFinal(compra.getTotalInicial());
            compra.setNumMeses(0);
            compra.setFinalCuotas(false);
        }
        else {
            compra.setTasa(
                    conversorTNA(
                            tasaRepository.getTasaByCuentaCredito_IdAndNomrmalMoratoria(
                                    compra.getCuentaCredito().getId(),
                                    false)));
            compra.setTasaMora(
                    conversorTNA(
                            tasaRepository.getTasaByCuentaCredito_IdAndNomrmalMoratoria(
                                    compra.getCuentaCredito().getId(),
                                    true)));
            compra.setTotalGracia(totalGracia(compra));
            compra.setIntereses(intereses(compra));
            compra.setTotalFinal(compra.getTotalGracia() +  compra.getIntereses());
        }

        compraRepository.save(compra);
        return modelMapper.map(compra, CompraDTO.class);
    }

    @Override
    public void eliminar(Integer id) {
        List<Cuota> cuotas = cuotaRepository.findByCompra_Id(id);
        List<Item> items = itemRepository.findByCompra_Id(id);
        cuotaRepository.deleteAll(cuotas);
        itemRepository.deleteAll(items);
        compraRepository.deleteById(id);
    }

    @Override
    public List<CompraDTO> comprasPorNegocio(Integer negocioId) {
        return compraRepository.findAllByCuentaCredito_Negocio_Id(negocioId)
                .stream().map(compra -> modelMapper.map(compra, CompraDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CompraDTO> comprasPorCliente(Integer clienteId) {
        return compraRepository.findAllByCuentaCredito_Cliente_Id(clienteId)
                .stream().map(compra -> modelMapper.map(compra, CompraDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CompraDTO> comprasPorCuentaCredito(Integer cuentaCreditoId) {
        return compraRepository.findAllByCuentaCredito_Id(cuentaCreditoId)
                .stream().map(compra -> modelMapper.map(compra, CompraDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CompraDTO buscarPorId(Integer id) {
        return compraRepository.findById(id)
                .map(compra -> modelMapper.map(compra, CompraDTO.class))
                .orElse(null);
    }

    @Override
    public Double conversorTNA(Tasa tasa) {
        if (!tasa.getNominalEfectiva()){
            Double m = tasa.getPeriodicidad()/tasa.getCapitalizacion();
            Double n = 360/tasa.getCapitalizacion();
            Double j = tasa.getPorcentaje();
            return(Math.pow(1+(j/m), n)-1);
        }
        else if (tasa.getPeriodicidad()!=360){
            Double n = 360/tasa.getPeriodicidad();
            Double i = tasa.getPorcentaje();
            return (Math.pow(1+i, 360/n)-1);
        }
        else{return tasa.getPorcentaje();}
    }

    @Override
    public Double sumaTotalInicial(Integer compraId, Boolean contadoCredito) {
        Double totalInicial = 0.0;
        List<Item> items = itemRepository.findByCompra_Id(compraId);
        if (!contadoCredito){
            for(Item item:items){
                totalInicial += item.getProducto().getPrecioContado() * item.getCantidad();
            }
        }
        else {
            for(Item item:items){
                totalInicial += item.getProducto().getPrecioLista() * item.getCantidad();
            }
        }
        return totalInicial;
    }

    @Override
    public Double totalGracia(Compra compra) {
        Double tep = compra.getTasa();
        Double c = compra.getTotalInicial();
        LocalDate fechaPago;
        if(!compra.getFecha().isAfter(LocalDate.of(
                LocalDate.now().getYear(),
                LocalDate.now().getMonth(),
                compra.getCuentaCredito().getDiaCorte()))){
            fechaPago = LocalDate.of(
                    LocalDate.now().getYear(),
                    LocalDate.now().getMonth(),
                    compra.getCuentaCredito().getDiaPago());
        }
        else{
            fechaPago = LocalDate.of(
                    LocalDate.now().getYear(),
                    LocalDate.now().getMonth(),
                    compra.getCuentaCredito().getDiaPago()).plusMonths(1);
        }
        Long n = ChronoUnit.DAYS.between(fechaPago, LocalDate.now());
        return Math.pow(1+tep, n/360);
    }

    @Override
    public Double intereses(Compra compra) {
        Double tep = compra.getTasa();
        Double c = compra.getTotalGracia();
        LocalDate fechaInicio;
        if(!compra.getFecha().isAfter(LocalDate.of(
                LocalDate.now().getYear(),
                LocalDate.now().getMonth(),
                compra.getCuentaCredito().getDiaCorte()))){
            fechaInicio = LocalDate.of(
                    LocalDate.now().getYear(),
                    LocalDate.now().getMonth(),
                    compra.getCuentaCredito().getDiaPago());
        }
        else{
            fechaInicio = LocalDate.of(
                    LocalDate.now().getYear(),
                    LocalDate.now().getMonth(),
                    compra.getCuentaCredito().getDiaPago()).plusMonths(1);
        }
        LocalDate fechaFin = fechaInicio.plusMonths(compra.getNumMeses());
        Long n = ChronoUnit.DAYS.between(fechaInicio, fechaFin);

        return Math.pow(1+tep, n/360);
    }
}
