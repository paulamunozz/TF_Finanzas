package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.ProductoDTO;
import com.upc.cuentix.services.ProductoService;
import com.upc.cuentix.validations.Create;
import com.upc.cuentix.validations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductoController {
    @Autowired private ProductoService productoService;

    @PostMapping("/registrarProducto")
    public ProductoDTO registrar(@RequestBody @Validated(Create.class) ProductoDTO productoDTO) {
        return productoService.registrar(productoDTO);
    }

    @PutMapping("/modificarProducto")
    public ProductoDTO modificar(@RequestBody @Validated(Update.class) ProductoDTO productoDTO) {
        return productoService.modificar(productoDTO);
    }

    @DeleteMapping("/eliminarProducto/{idProducto}")
    public String eliminar(@PathVariable Integer idProducto) {
        return productoService.eliminar(idProducto);
    }

    @PostMapping("/buscarProductoPorID")
    public ProductoDTO buscarPorId(@RequestBody Integer idProducto) {
        return productoService.buscarPorId(idProducto);
    }

    @GetMapping("/listarProductos")
    public List<ProductoDTO> listar() {
        return productoService.listar();
    }

    @PostMapping("/listarProductosPorNegocio")
    public List<ProductoDTO> listarPorNegocio(@RequestBody Integer idNegocio) {
        return productoService.listarPorNegocio(idNegocio);
    }
}
