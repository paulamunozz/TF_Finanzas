package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CuotaRepository extends JpaRepository<Cuota,Integer> {
    List<Cuota> findByCompra_Id(Integer compraId);

    List<Cuota> findByCompra_IdIn(Collection<Integer> purchaseIds);

    @Query("""
        SELECT q FROM Cuota q JOIN FETCH q.compra p
        WHERE p.cuentaCredito.id = :account AND q.cancelado = false
          AND q.fechaVencimiento >= :start AND q.fechaVencimiento < :end
        ORDER BY p.fecha, p.horaCompra, p.id, q.num
        """)
    List<Cuota> pendingAt(Integer account, Date start, Date end);

    @Query("""
    SELECT c FROM Cuota c
    WHERE c.compra.cuentaCredito.cliente.id = :clienteId
      AND c.cancelado = false
      AND c.fechaVencimiento <= :fechaLimite
    ORDER BY c.compra.cuentaCredito.cliente.id ASC, c.fechaVencimiento ASC, c.compra.fecha ASC, c.num ASC""")
    List<Cuota> pendientesCliente(@Param("clienteId") Integer clienteId,
                                  @Param("fechaLimite") java.util.Date fechaLimite);

    @Query("""
    SELECT c FROM Cuota c
    WHERE c.compra.cuentaCredito.negocio.id = :negocioId
      AND c.cancelado = false
      AND c.fechaVencimiento <= :fechaLimite
    ORDER BY c.compra.cuentaCredito.negocio.id ASC, c.fechaVencimiento ASC, c.compra.fecha ASC, c.num ASC""")
    List<Cuota> pendientesNegocio(@Param("negocioId") Integer negocioId,
                                  @Param("fechaLimite") java.util.Date fechaLimite);

}
