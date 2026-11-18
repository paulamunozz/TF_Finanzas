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
public class CuentaCreditoDTO {
  private Integer id;

  @NotNull private Double deuda;

  @NotNull private Double limite;

  @NotNull private Integer maxMeses;

  @NotNull private Integer diaCorte;

  @NotNull private Integer diaPago;

  @NotNull private Integer monedaId;

  private Boolean estado;

  @NotNull private Integer negocioId;

  @NotNull private Integer clienteId;
}
