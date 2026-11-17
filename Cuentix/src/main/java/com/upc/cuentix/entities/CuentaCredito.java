package com.upc.cuentix.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name ="cuenta_credito")
public class CuentaCredito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cc", nullable = false)
    private Integer id;

    @Column(name = "deuda", nullable = false)
    private Double deuda;

    @Column(name = "limite", nullable = false)
    private Double limite;

    @Column(name = "max_meses", nullable = false)
    private Integer maxMeses;

    @Column(name = "dia_corte", nullable = false)
    private Integer diaCorte;

    @Column(name = "dia_pago", nullable = false)
    private Integer diaPago;

    @ManyToOne
    @JoinColumn(name = "moneda_id")
    private Moneda moneda;

    @Column(name = "estado")
    private Boolean estado = true;

    @ManyToOne
    @JoinColumn(name = "negocio_id")
    private Negocio negocio;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
