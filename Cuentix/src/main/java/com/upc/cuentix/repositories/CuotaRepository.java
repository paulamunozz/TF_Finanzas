package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuotaRepository extends JpaRepository<Cuota,Integer> {
    List<Cuota> findByCompra_Id(Integer compraId);

}
