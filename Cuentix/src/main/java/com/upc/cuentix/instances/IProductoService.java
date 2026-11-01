package com.upc.cuentix.instances;

import com.upc.cuentix.dto.ProductoDTO;
import java.util.List;

public interface IProductoService {
    ProductoDTO registrar(ProductoDTO productoDTO);
    ProductoDTO modificar(ProductoDTO productoDTO);
    String eliminar(Integer id);
    ProductoDTO buscarPorId(Integer id);
    List<ProductoDTO> listar();
    List<ProductoDTO> listarPorNegocio(Integer idNegocio);
}