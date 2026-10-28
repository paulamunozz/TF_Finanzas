package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INegocioRepository extends JpaRepository<Negocio, Integer> {
    boolean existsByIdAndEstado(Integer id, Boolean estado);
    Negocio findByUsuario_Codigo(String codigo);
    List<Negocio> findByEstado(Boolean estado);
}