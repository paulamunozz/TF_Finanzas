package com.upc.cuentix.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name ="tasa")
public class Tasa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tasa", nullable = false)
    private Integer id;

    @Column(name = "porcentaje", nullable = false)
    private Double porcentaje;

    @Column(name = "periodicidad", nullable = false)
    private Integer periodicidad;

    @Column(name = "capitalizacion", nullable = false)
    private Integer capitalizacion;

    @Column(name = "nominal_efectiva", nullable = false)
    private Boolean nominalEfectiva;

    @Column(name = "normal_moratoria", nullable = false)
    private Boolean nomrmalMoratoria;

    @ManyToOne
    @JoinColumn(name = "cc_id")
    private CuentaCredito cuentaCredito;
}
