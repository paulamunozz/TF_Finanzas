package com.upc.cuentix.dto;

import com.upc.cuentix.entities.Cliente;
import com.upc.cuentix.entities.Moneda;
import com.upc.cuentix.entities.Negocio;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @NotNull
    private Double deuda;

    @NotNull
    private Double limite;

    @NotNull
    private Integer maxMeses;

    @NotNull
    private Integer diaCorte;

    @NotNull
    private Integer diaPago;

    @NotNull
    private Integer monedaId;

    private Boolean estado;

    @NotNull
    private Integer negocioId;

    @NotNull
    private Integer clienteId;
}
