package com.upc.cuentix.dto;

import com.upc.cuentix.validations.Create;
import com.upc.cuentix.validations.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class ClienteDTO {
    private Integer id;

    @NotBlank(groups = Create.class, message = "Tiene que ingresar su DNI")
    @Pattern(groups = {Create.class, Update.class}, regexp = "(^$|^.{8}$)", message = "El formato del DNI ingresado es incorrecto")
    private String dni;

    @NotBlank(groups = Create.class, message = "Tiene que ingresar su contraseña")
    private String contrasena;

    @NotBlank(groups = Create.class, message = "Ingrese el nombre")
    private String nombre;

    @NotBlank(groups = Create.class, message = "Ingrese el apellido")
    private String apellido;

    @NotBlank(groups = Create.class, message = "Ingrese el teléfono")
    private String telefono;

    private Boolean estado;
}