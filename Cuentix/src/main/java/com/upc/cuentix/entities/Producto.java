package com.upc.cuentix.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name ="producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "marca", nullable = false)
    private String marca;

    @Column(name = "proovedor", nullable = false)
    private String proovedor;

    @Column(name = "imagen_url", nullable = false)
    private String imagenURL;

    @ManyToOne
    @JoinColumn(name = "um_id")
    private UnidadMedida unidadMedida;

    @Column(name = "precio_contado", nullable = false)
    private Double precioContado;

    @Column(name = "precio_lista", nullable = false)
    private Double precioLista;

    @Column(name = "pago_cuotas")
    private Boolean pagoCuotas = false;

    @Column(name = "estado")
    private Boolean estado = true;

    @ManyToOne
    @JoinColumn(name = "negocio_id")
    private Negocio negocio;
}
