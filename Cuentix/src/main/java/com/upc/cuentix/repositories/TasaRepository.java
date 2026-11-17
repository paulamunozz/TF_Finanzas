package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Tasa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TasaRepository extends JpaRepository<Tasa, Integer> {
    Tasa getTasaById(Integer id);
    Tasa getTasaByCcId(Integer ccId);
    Tasa getTasaByCuentaCredito_IdAndNomrmalMoratoria(Integer cuentaCreditoId, Boolean nomrmalMoratoria);
}
