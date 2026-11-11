
package com.upc.cuentix.dto;

public record AcuerdoClienteDTO(
        Integer id,
        String tienda,
        Double tasa,
        String tipoTasa,
        Double linea,
        String moneda,
        Integer meses,
        Integer diaCorte,
        Integer diaPago
) {
}