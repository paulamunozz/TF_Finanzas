package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUnidadMedidaRepository extends JpaRepository <UnidadMedida, Integer> {
}
