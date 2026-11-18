package com.upc.cuentix.dto;

import com.upc.cuentix.entities.CuentaCredito;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompraDTO {
    private Integer id;
    private Date fecha;
    private Double totalInicial;
    private Double interesesGracia;
    private Double intereses;
    private Double moras;
    private Double totalFinal;
    private Integer numMeses;
    private Boolean contadoCredito;
    private Boolean finalCuotas;
    private Double tasa;
    private Double tasaMora;
    private Integer ccId;
}
