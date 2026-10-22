package com.upc.cuentix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NegocioDTO {
    private Integer id;

    @NotBlank
    private String ruc;

    @NotBlank
    private String contrasena;

    @NotBlank
    private String razonSocial;

    @NotNull
    private Integer giroId;

    @NotBlank
    private String direccion;

    @NotBlank
    private String telefono;

    private Boolean estado;
}
