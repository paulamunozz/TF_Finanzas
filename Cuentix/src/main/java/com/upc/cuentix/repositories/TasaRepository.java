
package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Tasa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TasaRepository extends JpaRepository<Tasa, Integer> {

    Tasa getTasaById(Integer id);
    Tasa getTasaByCuentaCredito_Id(Integer cuentaCreditoId);
    Tasa getTasaByCuentaCredito_IdAndNomrmalMoratoria(
            Integer cuentaCreditoId,
            Boolean nomrmalMoratoria
    );
}

