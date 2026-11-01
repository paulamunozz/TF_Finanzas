package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByIdAndEstado(Integer id, Boolean estado);
    Cliente findByUsuario_Codigo(String codigo);
}