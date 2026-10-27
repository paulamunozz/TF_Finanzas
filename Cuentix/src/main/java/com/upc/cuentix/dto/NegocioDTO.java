package com.upc.cuentix.dto;

import com.upc.cuentix.validations.Create;
import com.upc.cuentix.validations.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class NegocioDTO {
    private Integer id;

    @NotBlank(groups = Create.class, message = "Ingrese el RUC")
    @Pattern(groups = {Create.class, Update.class}, regexp = "(^$|^\\d{11}$)", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @NotBlank(groups = Create.class, message = "Ingrese la contraseña")
    private String contrasena;

    @NotBlank(groups = Create.class, message = "Ingrese la razón social")
    private String razonSocial;

    @NotNull(groups = Create.class, message = "Ingrese el giro")
    private Integer idGiro;

    @NotBlank(groups = Create.class, message = "Ingrese la dirección")
    private String direccion;

    @NotBlank(groups = Create.class, message = "Ingrese el teléfono")
    @Pattern(groups = {Create.class, Update.class}, regexp = "(^$|^9\\d{8}$)", message = "El teléfono debe tener 9 dígitos y empezar con 9")
    private String telefono;

    private Boolean estado;
}
