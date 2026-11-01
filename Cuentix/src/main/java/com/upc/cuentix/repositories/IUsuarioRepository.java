package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByIdAndEstado(Integer id, Boolean estado);
    boolean existsByCodigoAndEstado(String codigo, Boolean estado);
    Usuario findByCodigoAndEstado(String codigo, Boolean estado);
    List<Usuario> findByEstado(Boolean estado);
}
