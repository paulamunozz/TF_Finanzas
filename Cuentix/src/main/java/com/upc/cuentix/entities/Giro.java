package com.upc.cuentix.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name ="giro")
public class Giro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_giro", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;
}
