package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.CuentaCredito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICuentaCreditoRepository extends JpaRepository<CuentaCredito, Integer> {

  List<CuentaCredito> findAllByCliente_Usuario_IdAndEstadoTrue(Integer usuarioId);
}
