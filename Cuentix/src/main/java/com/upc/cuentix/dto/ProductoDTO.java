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
public class ProductoDTO {
    private Integer id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String descripcion;

    @NotBlank
    private String marca;

    @NotBlank
    private String proovedor;

    @NotBlank
    private String imagenURL;

    @NotNull
    private Integer umId;

    @NotNull
    private Double precioContado;

    @NotNull
    private Double precioLista;

    private Boolean pagoCuotas;
    private Boolean estado;

    @NotNull
    private Integer negocioId;
}
