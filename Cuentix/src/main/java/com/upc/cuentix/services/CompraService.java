package com.upc.cuentix.services;

import com.upc.cuentix.dto.CompraDTO;
import com.upc.cuentix.entities.Compra;
import com.upc.cuentix.entities.CuentaCredito;
import com.upc.cuentix.entities.Cuota;
import com.upc.cuentix.entities.Item;
import com.upc.cuentix.entities.Tasa;
import com.upc.cuentix.instances.ICompraService;
import com.upc.cuentix.repositories.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@org.springframework.transaction.annotation.Transactional
public class CompraService implements ICompraService {
  @Autowired private com.upc.cuentix.config.ResourceAccess access;
  @Autowired private CompraRepository compraRepository;
  @Autowired private CuentaCreditoRepository cuentaCreditoRepository;
  @Autowired private TasaRepository tasaRepository;
  @Autowired private ItemRepository itemRepository;
  @Autowired private CuotaRepository cuotaRepository;
  @Autowired private ModelMapper modelMapper;
  @Autowired private BusinessApplicationService businessService;

  @Override
  public CompraDTO preRegistrar(CompraDTO compraDTO) {
    if (compraDTO.getCcId() == null || compraDTO.getContadoCredito() == null)
      throw new IllegalArgumentException("Indique cuenta y modalidad");
    access.account(compraDTO.getCcId());

    // Verificar que se envió el ID de la cuenta.
    if (compraDTO.getCcId() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Debe indicar una cuenta de crédito");
    }

    // Buscar la cuenta de crédito en PostgreSQL.
    CuentaCredito cuenta =
        cuentaCreditoRepository
            .findById(compraDTO.getCcId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La cuenta de crédito no existe"));

    if (!Boolean.TRUE.equals(cuenta.getEstado())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "La cuenta de crédito no está activa");
    }

    // Validar el plazo de la compra a crédito.
    if (Boolean.TRUE.equals(compraDTO.getContadoCredito())) {
      if (compraDTO.getNumMeses() == null
          || compraDTO.getNumMeses() < 1
          || compraDTO.getNumMeses() > cuenta.getMaxMeses()) {

        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "El plazo de la compra no es válido");
      }
    }

    // Convertir el DTO a entidad.
    Compra compra = modelMapper.map(compraDTO, Compra.class);

    // Asociar explícitamente la cuenta de crédito.
    compra.setId(null);
    compra.setFecha(null);
    compra.setTotalFinal(null);
    compra.setCuentaCredito(cuenta);

    // Guardar la compra.
    Compra compraGuardada = compraRepository.save(compra);

    // Preparar respuesta.
    CompraDTO respuesta = modelMapper.map(compraGuardada, CompraDTO.class);

    respuesta.setCcId(cuenta.getId());

    return respuesta;
  }

  @Override
  public CompraDTO registrar(CompraDTO compraDTO) {
    access.purchase(compraDTO.getId());
    Compra compra = compraRepository.findById(compraDTO.getId()).orElseThrow();
    if (Boolean.TRUE.equals(compra.getContadoCredito()))
      return businessService.finalizeDraft(compra.getId());
    if (compra.getFecha() != null) throw new IllegalArgumentException("Compra ya finalizada");
    compra.setFecha(LocalDate.now());
    compra.setTotalInicial(sumaTotalInicial(compra.getId(), compra.getContadoCredito()));
    compra.setMoras(0.0);

    compra.setTasa(0.0);
    compra.setTasaMora(0.0);
    compra.setTotalGracia(0.0);
    compra.setIntereses(0.0);
    compra.setTotalFinal(compra.getTotalInicial());
    compra.setNumMeses(0);
    compra.setFinalCuotas(false);

    compraRepository.save(compra);
    return modelMapper.map(compra, CompraDTO.class);
  }

  @Override
  public void eliminar(Integer id) {
    access.purchase(id);
    if (compraRepository.findById(id).orElseThrow().getFecha() != null)
      throw new IllegalArgumentException("No puede eliminar una compra finalizada");
    List<Cuota> cuotas = cuotaRepository.findByCompra_Id(id);
    List<Item> items = itemRepository.findByCompra_Id(id);
    cuotaRepository.deleteAll(cuotas);
    itemRepository.deleteAll(items);
    compraRepository.deleteById(id);
  }

  @Override
  public List<CompraDTO> comprasPorNegocio(Integer negocioId) {
    access.business(negocioId);
    return compraRepository.findAllByCuentaCredito_Negocio_Id(negocioId).stream()
        .map(compra -> modelMapper.map(compra, CompraDTO.class))
        .collect(Collectors.toList());
  }

  @Override
  public List<CompraDTO> comprasPorCliente(Integer clienteId) {
    access.client(clienteId);
    return compraRepository.findAllByCuentaCredito_Cliente_Id(clienteId).stream()
        .filter(compra -> access.ownsBusiness(compra.getCuentaCredito().getNegocio().getId()))
        .map(compra -> modelMapper.map(compra, CompraDTO.class))
        .collect(Collectors.toList());
  }

  @Override
  public List<CompraDTO> comprasPorCuentaCredito(Integer cuentaCreditoId) {
    access.account(cuentaCreditoId);
    return compraRepository.findAllByCuentaCredito_Id(cuentaCreditoId).stream()
        .map(compra -> modelMapper.map(compra, CompraDTO.class))
        .collect(Collectors.toList());
  }

  @Override
  public CompraDTO buscarPorId(Integer id) {
    access.purchase(id);
    return compraRepository
        .findById(id)
        .map(compra -> modelMapper.map(compra, CompraDTO.class))
        .orElse(null);
  }

  @Override
  public Double conversorTNA(Tasa tasa) {
    return FinancialMath.annual(tasa);
  }

  @Override
  public Double sumaTotalInicial(Integer compraId, Boolean contadoCredito) {
    Double totalInicial = 0.0;
    List<Item> items = itemRepository.findByCompra_Id(compraId);
    if (!contadoCredito) {
      for (Item item : items) {
        totalInicial += item.getProducto().getPrecioContado() * item.getCantidad();
      }
    } else {
      for (Item item : items) {
        totalInicial += item.getProducto().getPrecioLista() * item.getCantidad();
      }
    }
    return FinancialMath.money(totalInicial).doubleValue();
  }

  @Override
  public Double totalGracia(Compra compra) {
    LocalDate due =
        FinancialMath.paymentDate(
            compra.getFecha(),
            compra.getCuentaCredito().getDiaCorte(),
            compra.getCuentaCredito().getDiaPago());
    return FinancialMath.compound(
        compra.getTotalInicial(),
        compra.getTasa(),
        ChronoUnit.DAYS.between(compra.getFecha(), due));
  }

  @Override
  public Double intereses(Compra compra) {
    LocalDate start =
        FinancialMath.paymentDate(
            compra.getFecha(),
            compra.getCuentaCredito().getDiaCorte(),
            compra.getCuentaCredito().getDiaPago());
    long days = ChronoUnit.DAYS.between(start, start.plusMonths(compra.getNumMeses()));
    return FinancialMath.money(
            FinancialMath.compound(compra.getTotalGracia(), compra.getTasa(), days)
                - compra.getTotalGracia())
        .doubleValue();
  }
}
