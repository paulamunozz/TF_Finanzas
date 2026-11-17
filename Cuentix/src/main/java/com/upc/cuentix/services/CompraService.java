package com.upc.cuentix.services;

import com.upc.cuentix.dto.CompraDTO;
import com.upc.cuentix.entities.*;
import com.upc.cuentix.instances.ICompraService;
import com.upc.cuentix.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CompraService implements ICompraService{
    @Autowired private CompraRepository compraRepository;
    @Autowired private CuentaCreditoRepository cuentaCreditoRepository;
    @Autowired private TasaRepository tasaRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CuotaRepository cuotaRepository;
    @Autowired private CuotaService cuotaService;
    @Autowired private ModelMapper modelMapper;

    @Override
    public CompraDTO preRegistrar(CompraDTO compraDTO) {
        if (!cuentaCreditoRepository.existsById(compraDTO.getCcId())){
            throw new RuntimeException("No existe");
        }

        Compra compra = modelMapper.map(compraDTO, Compra.class);
        compraRepository.save(compra);

        return modelMapper.map(compra, CompraDTO.class);
    }

    @Transactional
    @Override
    public CompraDTO registrar(CompraDTO compraDTO) {
        Compra compra = compraRepository.findById(compraDTO.getId()).get();
        compra.setFecha(LocalDate.now());
        compra.setContadoCredito(compraDTO.getContadoCredito());
        compra.setAmortizacion(sumaTotalInicial(compra.getId(), compraDTO.getContadoCredito()));
        compra.setMoras(0.0);
        if (!compra.getContadoCredito()){
            compra.setTasa(0.0);
            compra.setTasaMora(0.0);
            compra.setTotalGracia(compra.getAmortizacion());
            compra.setIntereses(0.0);
            compra.setTotalFinal(compra.getAmortizacion());
            compra.setNumMeses(0);
            compra.setFinalCuotas(false);
        }
        else {
            compra.setTasa(
                    conversorTEM(
                            tasaRepository.getTasaByCuentaCredito_IdAndNomrmalMoratoria(
                                    compra.getCuentaCredito().getId(),
                                    false), 30));
            compra.setTasaMora(
                    conversorTEM(
                            tasaRepository.getTasaByCuentaCredito_IdAndNomrmalMoratoria(
                                    compra.getCuentaCredito().getId(),
                                    true), 1));
            compra.setTotalGracia(totalGracia(compra));
            compra.setNumMeses(compraDTO.getNumMeses());
            compra.setFinalCuotas(compraDTO.getFinalCuotas());


            LocalDate fechaPago = calcularFechaPago(compra);
            if (!compra.getFinalCuotas()){
                Cuota cuota = new Cuota();
                cuota.setCompra(compra);
                cuota.setNum(1);
                cuota.setFechaVencimiento(fechaPago);
                cuota.setAmortizacion(compra.getAmortizacion());
                cuota.setTotal(compra.getTotalGracia());
                cuota.setIntereses(cuota.getTotal() - cuota.getAmortizacion());
                cuota.setMoras(0.0);
                cuota.setCancelado(false);
                cuotaRepository.save(cuota);

                compra.setTotalFinal(cuota.getTotal());
            }
            else {
                double A = calculoCuota(compra);
                double total = 0.0;
                for (int i = 1; i <= compra.getNumMeses(); i++) {
                    Cuota cuota = new Cuota();
                    cuota.setCompra(compra);
                    cuota.setNum(i);
                    cuota.setFechaVencimiento(fechaPago.plusMonths(i));   // 26/10, 26/11, 26/12
                    cuota.setIntereses(A * (1 - Math.pow(1 + compra.getTasa(), -(compra.getNumMeses() - i + 1))));
                    cuota.setAmortizacion(A - cuota.getIntereses());
                    cuota.setTotal(A);
                    cuota.setMoras(0.0);
                    cuota.setCancelado(false);
                    cuotaRepository.save(cuota);
                    total += A;
                }
                compra.setTotalFinal(total);
            }
            compra.setIntereses(compra.getTotalFinal() - compra.getAmortizacion());

            if (compra.getCuentaCredito().getDeuda() + compra.getTotalFinal() > compra.getCuentaCredito().getLimite()
                    || compra.getNumMeses() > compra.getCuentaCredito().getMaxMeses()) {
                throw new RuntimeException("La compra excede el límite de crédito o el máximo de meses");
            }
            CuentaCredito cc = cuentaCreditoRepository.findById(compra.getCuentaCredito().getId()).orElse(null);
            cc.setDeuda(cc.getDeuda() + compra.getTotalFinal());
            cuentaCreditoRepository.save(cc);
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
    public Double conversorTEM(Tasa tasa, Integer periodicidad) {
        if (!tasa.getNominalEfectiva()) {
            double m = (double) tasa.getPeriodicidad() / tasa.getCapitalizacion();
            double n = (double) periodicidad / tasa.getCapitalizacion();
            return Math.pow(1 + tasa.getPorcentaje() / m, n) - 1;
        } else if (!Objects.equals(tasa.getPeriodicidad(), periodicidad)) {
            double n = (double) periodicidad / tasa.getPeriodicidad();
            return Math.pow(1 + tasa.getPorcentaje(), n) - 1;
        }
        return tasa.getPorcentaje();
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
        double n = ChronoUnit.DAYS.between(compra.getFecha(), calcularFechaPago(compra));
        return compra.getAmortizacion() * Math.pow(1 + compra.getTasa(), n / 30);
    }

    @Override
    public LocalDate calcularFechaPago(Compra compra) {
        LocalDate f = compra.getFecha();
        int diaCorte = compra.getCuentaCredito().getDiaCorte();
        int diaPago = compra.getCuentaCredito().getDiaPago();

        YearMonth mesCorte = YearMonth.from(f);
        if (f.isAfter(diaSeguro(mesCorte, diaCorte))) mesCorte = mesCorte.plusMonths(1);

        YearMonth mesPago = (diaPago > diaCorte) ? mesCorte : mesCorte.plusMonths(1);
        return diaSeguro(mesPago, diaPago);
    }

    @Override
    public LocalDate diaSeguro(YearMonth ym, int dia) {
        return ym.atDay(Math.min(dia, ym.lengthOfMonth()));
    }

    @Override
    public Double calculoCuota(Compra compra) {
        double i = compra.getTasa();
        double C = compra.getTotalGracia();
        int n = compra.getNumMeses();
        if (i == 0) return C / n;
        return C * (i / (1 - Math.pow(1 + i, -n)));
    }
}
