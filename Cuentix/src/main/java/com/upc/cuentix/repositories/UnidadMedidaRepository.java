package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.UnidadMedida;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {
    Optional<UnidadMedida> findFirstByNombreIgnoreCaseOrderById(String nombre);
}
