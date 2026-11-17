package com.upc.cuentix.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name ="cuota")
public class Cuota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuota", nullable = false)
    private Integer id;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "num", nullable = false)
    private Integer num;

    @Column(name = "amortizacion", nullable = false)
    private Double amortizacion;

    @Column(name = "intereses", nullable = false)
    private Double intereses;

    @Column(name = "moras", nullable = false)
    private Double moras;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "cancelado", nullable = false)
    private Boolean cancelado = false;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;
}
