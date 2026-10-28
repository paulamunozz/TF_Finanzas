package com.upc.cuentix.dto;

<<<<<<< HEAD
=======
import com.upc.cuentix.validations.Create;
import com.upc.cuentix.validations.Update;
import jakarta.validation.constraints.DecimalMin;
>>>>>>> giovanni
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

<<<<<<< HEAD
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
=======
    @NotNull(groups = Create.class, message = "Ingrese el negocio")
    private Integer idNegocio;

    @NotBlank(groups = Create.class, message = "Ingrese el nombre")
    private String nombre;

    @NotBlank(groups = Create.class, message = "Ingrese la descripción")
    private String descripcion;

    @NotBlank(groups = Create.class, message = "Ingrese la marca")
    private String marca;

    @NotBlank(groups = Create.class, message = "Ingrese el proveedor")
    private String proveedor;

    @NotBlank(groups = Create.class, message = "Ingrese la imagen")
    private String imagenUrl;

    @NotNull(groups = Create.class, message = "Ingrese la unidad de medida")
    private Integer idUnidadMedida;

    @NotNull(groups = Create.class, message = "Ingrese el precio al contado")
    @DecimalMin(value = "0.0", groups = {Create.class, Update.class}, message = "El precio no puede ser negativo")
    private Double precioContado;

    @NotNull(groups = Create.class, message = "Ingrese el precio de lista")
    @DecimalMin(value = "0.0", groups = {Create.class, Update.class}, message = "El precio no puede ser negativo")
>>>>>>> giovanni
    private Double precioLista;

    private Boolean pagoCuotas;
    private Boolean estado;
<<<<<<< HEAD

    @NotNull
    private Integer negocioId;
=======
>>>>>>> giovanni
}
