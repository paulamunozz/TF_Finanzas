package com.upc.cuentix.entities;

import jakarta.persistence.*;
import java.time.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
@Table(name="cierre", uniqueConstraints=@UniqueConstraint(columnNames={"cuenta_id","periodo"}))
public class Cierre {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional=false) @JoinColumn(name="cuenta_id") private CuentaCredito cuenta;
    @Column(nullable=false) private LocalDate periodo;
    @Column(nullable=false) private LocalDateTime corte;
    @Column(nullable=false) private LocalDate vencimiento;
    @Column(nullable=false) private String moneda;
}
