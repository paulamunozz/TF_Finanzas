package com.upc.cuentix.dto;

import com.upc.cuentix.entities.Compra;
import com.upc.cuentix.entities.Producto;
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
public class ItemDTO {
    private Integer id;

    @NotNull
    private Integer cantidad;

    @NotNull
    private Integer productoId;

    @NotNull
    private Integer compraId;
}
