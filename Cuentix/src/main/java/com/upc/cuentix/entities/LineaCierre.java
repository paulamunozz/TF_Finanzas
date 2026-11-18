package com.upc.cuentix.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
@Table(name="linea_cierre", uniqueConstraints=@UniqueConstraint(columnNames={"cierre_id","cuota_id"}))
public class LineaCierre {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional=false) @JoinColumn(name="cierre_id") private Cierre cierre;
    @ManyToOne(optional=false) @JoinColumn(name="cuota_id") private Cuota cuota;
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal mora;
    private boolean ajuste;
}
