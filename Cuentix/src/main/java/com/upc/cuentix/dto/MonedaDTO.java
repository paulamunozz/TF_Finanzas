package com.upc.cuentix.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MonedaDTO {
    private Integer id;
    private String nombre;
    private String simbolo;
}
