package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Integer> {
    List<Compra> findAllByCuentaCredito_Negocio_Id(Integer negocioId);
    List<Compra> findAllByCuentaCredito_Cliente_Id(Integer clienteId);
    List<Compra> findAllByCuentaCredito_Id(Integer cuentaCreditoId);
}
