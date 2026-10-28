package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Integer> {
    boolean existsByIdAndEstado(Integer id, Boolean estado);
    List<Producto> findByEstado(Boolean estado);
    List<Producto> findByNegocio_IdAndEstado(Integer id, Boolean estado);
}