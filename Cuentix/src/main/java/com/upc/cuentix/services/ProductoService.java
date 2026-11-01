package com.upc.cuentix.services;

import com.upc.cuentix.dto.ProductoDTO;
import com.upc.cuentix.entities.Negocio;
import com.upc.cuentix.entities.Producto;
import com.upc.cuentix.entities.UnidadMedida;
import com.upc.cuentix.instances.IProductoService;
import com.upc.cuentix.repositories.INegocioRepository;
import com.upc.cuentix.repositories.IProductoRepository;
import com.upc.cuentix.repositories.IUnidadMedidaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService implements IProductoService {

    @Autowired private IProductoRepository productoRepository;
    @Autowired private INegocioRepository negocioRepository;
    @Autowired private IUnidadMedidaRepository unidadMedidaRepository;

    @Override
    public ProductoDTO registrar(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setNegocio(buscarNegocio(dto.getIdNegocio()));
        producto.setUnidadMedida(buscarUnidad(dto.getIdUnidadMedida()));
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setMarca(dto.getMarca());
        producto.setProovedor(dto.getProveedor());
        producto.setImagenURL(dto.getImagenUrl());
        producto.setPrecioContado(dto.getPrecioContado());
        producto.setPrecioLista(dto.getPrecioLista());
        producto.setPagoCuotas(dto.getPagoCuotas() != null ? dto.getPagoCuotas() : false);
        producto.setEstado(true);

        return convertir(productoRepository.save(producto));
    }

    @Override
    public ProductoDTO modificar(ProductoDTO dto) {
        if (!productoRepository.existsByIdAndEstado(dto.getId(), true)) {
            throw new RuntimeException("No existe el registro de id " + dto.getId());
        }
        Producto producto = productoRepository.findById(dto.getId()).get();

        if (dto.getIdNegocio() != null) producto.setNegocio(buscarNegocio(dto.getIdNegocio()));
        if (dto.getIdUnidadMedida() != null) producto.setUnidadMedida(buscarUnidad(dto.getIdUnidadMedida()));
        if (tiene(dto.getNombre())) producto.setNombre(dto.getNombre());
        if (tiene(dto.getDescripcion())) producto.setDescripcion(dto.getDescripcion());
        if (tiene(dto.getMarca())) producto.setMarca(dto.getMarca());
        if (tiene(dto.getProveedor())) producto.setProovedor(dto.getProveedor());
        if (tiene(dto.getImagenUrl())) producto.setImagenURL(dto.getImagenUrl());
        if (dto.getPrecioContado() != null) producto.setPrecioContado(dto.getPrecioContado());
        if (dto.getPrecioLista() != null) producto.setPrecioLista(dto.getPrecioLista());
        if (dto.getPagoCuotas() != null) producto.setPagoCuotas(dto.getPagoCuotas());

        return convertir(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public String eliminar(Integer id) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null || !producto.getEstado()) {
            return "No se encontró el producto";
        }
        producto.setEstado(false);
        productoRepository.save(producto);
        return "Producto eliminado exitosamente";
    }

    @Override
    public ProductoDTO buscarPorId(Integer id) {
        if (id == null) return null;
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null) return null;
        if (!producto.getEstado()) throw new RuntimeException("Este producto no existe");
        return convertir(producto);
    }

    @Override
    public List<ProductoDTO> listar() {
        return productoRepository.findByEstado(true).stream()
                .map(this::convertir).collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> listarPorNegocio(Integer idNegocio) {
        return productoRepository.findByNegocio_IdAndEstado(idNegocio, true).stream()
                .map(this::convertir).collect(Collectors.toList());
    }

    private Negocio buscarNegocio(Integer idNegocio) {
        return negocioRepository.findById(idNegocio)
                .filter(Negocio::getEstado)
                .orElseThrow(() -> new RuntimeException("No existe el negocio de id " + idNegocio));
    }

    private UnidadMedida buscarUnidad(Integer idUnidad) {
        return unidadMedidaRepository.findById(idUnidad)
                .orElseThrow(() -> new RuntimeException("No existe la unidad de medida de id " + idUnidad));
    }

    private boolean tiene(String s) {
        return s != null && !s.isBlank();
    }

    private ProductoDTO convertir(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(p.getId());
        dto.setIdNegocio(p.getNegocio().getId());
        dto.setIdUnidadMedida(p.getUnidadMedida() != null ? p.getUnidadMedida().getId() : null);
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setMarca(p.getMarca());
        dto.setProveedor(p.getProovedor());
        dto.setImagenUrl(p.getImagenURL());
        dto.setPrecioContado(p.getPrecioContado());
        dto.setPrecioLista(p.getPrecioLista());
        dto.setPagoCuotas(p.getPagoCuotas());
        dto.setEstado(p.getEstado());
        return dto;
    }
}