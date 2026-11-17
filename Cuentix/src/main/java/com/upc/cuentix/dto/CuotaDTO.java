package com.upc.cuentix.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CuotaDTO {
    private Integer id;

    @NotNull
    private Date fechaVencimiento;

    @NotNull
    private LocalDate fechaPago;

    @NotNull
    private Integer num;

    @NotNull
    private Double amortizacion;

    @NotNull
    private Double intereses;

    @NotNull
    private Double moras;

    @NotNull
    private Double total;

    private Boolean cancelado;

    @NotNull
    private Integer compraId;
}
