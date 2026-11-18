package com.upc.cuentix.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name ="compra")
public class Compra {
    @Column(name = "hora_compra")
    private java.time.LocalTime horaCompra;

    @Column(name = "fecha_corte")
    private LocalDate fechaCorte;

    @Column(name = "dia_pago_pactado")
    private Integer diaPagoPactado;

    @Column(name = "dia_corte_pactado")
    private Integer diaCortePactado;

    @Column(name = "hora_corte_pactada")
    private java.time.LocalTime horaCortePactada;

    @Column(name = "modalidad")
    private String modalidad;

    @Column(name = "mora_efectiva")
    private Boolean moraEfectiva;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra", nullable = false)
    private Integer id;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "tasa")
    private Double tasa;

    @Column(name = "tasa_mora")
    private Double tasaMora;

    @Column(name = "total_inicial")
    private Double totalInicial;

    @Column(name = "total_gracia")
    private Double totalGracia;

    @Column(name = "intereses")
    private Double intereses;

    @Column(name = "moras")
    private Double moras;

    @Column(name = "total_final")
    private Double totalFinal;

    @Column(name = "num_meses")
    private Integer numMeses;

    @Column(name = "contado_credito")
    private Boolean contadoCredito;

    @Column(name = "final_cuotas")
    private Boolean finalCuotas;

    @ManyToOne
    @JoinColumn(name = "cc_id")
    private CuentaCredito cuentaCredito;
}
