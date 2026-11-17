package com.upc.cuentix.repositories;

import com.upc.cuentix.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByCodigoAndEstado(String codigo, Boolean estado);
    Usuario findByCodigoAndEstado(String codigo, Boolean estado);
}
