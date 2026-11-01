package com.upc.cuentix.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
    private Integer id;

    @NotBlank(message = "Ingrese el código")
    private String codigo;

    @NotBlank(message = "Ingrese la contraseña")
    private String contrasena;

    private Integer idRol;
    private Boolean estado;
}
