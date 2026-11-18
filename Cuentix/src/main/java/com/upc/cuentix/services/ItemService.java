package com.upc.cuentix.services;

import com.upc.cuentix.dto.ItemDTO;
import com.upc.cuentix.entities.Item;
import com.upc.cuentix.instances.IItemService;
import com.upc.cuentix.repositories.CompraRepository;
import com.upc.cuentix.repositories.ItemRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemService implements IItemService {
  @Autowired private com.upc.cuentix.config.ResourceAccess access;
  @Autowired private ItemRepository itemRepository;
  @Autowired private CompraRepository compraRepository;
  @Autowired private ModelMapper modelMapper;
  @Autowired private com.upc.cuentix.repositories.ProductoRepository productoRepository;

  @Override
  public ItemDTO registrarItem(ItemDTO itemDTO) {
    access.purchase(itemDTO.getCompraId());
    if (!compraRepository.existsById(itemDTO.getCompraId())) {
      throw new RuntimeException("No existe");
    }

    if (itemDTO.getCantidad() == null || itemDTO.getCantidad() <= 0)
      throw new IllegalArgumentException("Cantidad inválida");
    var compra = compraRepository.findById(itemDTO.getCompraId()).orElseThrow();
    var producto = productoRepository.findById(itemDTO.getProductoId()).orElseThrow();
    if (compra.getFecha() != null) throw new IllegalArgumentException("Compra ya finalizada");
    if (!producto.getNegocio().getId().equals(compra.getCuentaCredito().getNegocio().getId()))
      throw new IllegalArgumentException("Producto de otro negocio");
    Item item = new Item();
    item.setCantidad(itemDTO.getCantidad());
    item.setCompra(compra);
    item.setProducto(producto);
    itemRepository.save(item);
    return modelMapper.map(item, ItemDTO.class);
  }

  @Override
  public ItemDTO editarItem(ItemDTO itemDTO) {
    access.item(itemDTO.getId());
    Item item = itemRepository.findById(itemDTO.getId()).get();
    if (itemDTO.getCantidad() == null
        || itemDTO.getCantidad() <= 0
        || item.getCompra().getFecha() != null)
      throw new IllegalArgumentException("No puede modificar el ?tem");
    item.setCantidad(itemDTO.getCantidad());
    itemRepository.save(item);
    return modelMapper.map(item, ItemDTO.class);
  }

  @Override
  public void eliminarItem(Integer id) {
    access.item(id);
    if (itemRepository.findById(id).orElseThrow().getCompra().getFecha() != null)
      throw new IllegalArgumentException("Compra finalizada");
    itemRepository.deleteById(id);
  }

  @Override
  public List<ItemDTO> itemsPorCompra(Integer compraId) {
    access.purchase(compraId);
    return itemRepository.findByCompra_Id(compraId).stream()
        .map(item -> modelMapper.map(item, ItemDTO.class))
        .collect(Collectors.toList());
  }
}
