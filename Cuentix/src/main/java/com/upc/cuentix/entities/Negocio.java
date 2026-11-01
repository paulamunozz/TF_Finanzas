package com.upc.cuentix.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name ="negocio")
public class Negocio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_negocio", nullable = false)
    private Integer id;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @ManyToOne
    @JoinColumn(name = "giro_id")
    private Giro giro;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Column(name = "estado")
    private Boolean estado = true;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
