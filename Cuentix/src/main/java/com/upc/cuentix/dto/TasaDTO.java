package com.upc.cuentix.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TasaDTO {
    private Integer id;

    @NotNull
    private Double porcentaje;

    @NotNull
    private Integer periodicidad;

    @NotNull
    private Integer capitalizacion;

    @NotNull
    private Boolean nominalEfectiva;

    @NotNull
    private Boolean nomrmalMoratoria;

    @NotNull
    private Integer ccId;
}
