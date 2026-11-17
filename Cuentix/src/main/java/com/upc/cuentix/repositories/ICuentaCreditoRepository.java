package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.CuentaCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICuentaCreditoRepository extends JpaRepository<CuentaCredito, Integer> {
}
