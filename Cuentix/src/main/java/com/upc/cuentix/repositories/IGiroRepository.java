package com.upc.cuentix.repositories;


import com.upc.cuentix.entities.Giro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IGiroRepository extends JpaRepository<Giro, Integer> {
}
