package com.upc.cuentix.services;

import com.upc.cuentix.config.ResourceAccess;
import com.upc.cuentix.dto.CuotaDTO;
import com.upc.cuentix.entities.Cuota;
import com.upc.cuentix.instances.ICuotaService;
import com.upc.cuentix.repositories.CuotaRepository;
import java.time.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CuotaService implements ICuotaService {
    private final CuotaRepository cuotas;
    private final ResourceAccess access;
    private final BusinessApplicationService business;
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Transactional
    public CuotaDTO pagarCuota(Integer id) {
        business.cancel(id, new BusinessApplicationService.PaymentRequest(LocalDate.now(LIMA).toString(), null, null));
        return view(cuotas.findById(id).orElseThrow(), false);
    }

    public List<CuotaDTO> cuotasPorCompra(Integer id) {
        access.purchase(id);
        return cuotas.findByCompra_Id(id).stream().map(q -> view(q, false)).toList();
    }

    public List<CuotaDTO> cuotasPorClienteMes(Integer id) {
        access.client(id);
        return cuotas.pendientesCliente(id, endOfMonth()).stream()
                .filter(q -> access.ownsBusiness(q.getCompra().getCuentaCredito().getNegocio().getId()))
                .map(q -> view(q, true)).toList();
    }

    public List<CuotaDTO> cuotasPorNegocioMes(Integer id) {
        access.business(id);
        return cuotas.pendientesNegocio(id, endOfMonth()).stream().map(q -> view(q, true)).toList();
    }

    private java.util.Date endOfMonth() {
        return java.sql.Date.valueOf(YearMonth.now(LIMA).atEndOfMonth());
    }

    private CuotaDTO view(Cuota q, boolean includeLate) {
        var payment = business.quoteForReport(q);
        return new CuotaDTO(q.getId(), q.getFechaVencimiento(), q.getFechaPago(), q.getNum(),
                payment.amortization(), q.getIntereses(), payment.lateAmount(),
                q.getTotal() + (includeLate ? payment.lateAmount() : 0), q.getCancelado(), q.getCompra().getId());
    }
}
