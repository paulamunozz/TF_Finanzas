package com.upc.cuentix.instances;

import com.upc.cuentix.dto.ItemDTO;

import java.util.List;

public interface IItemService {
    ItemDTO registrarItem(ItemDTO itemDTO);
    ItemDTO editarItem(ItemDTO itemDTO);
    void eliminarItem(Integer id);
    List<ItemDTO> itemsPorCompra(Integer compraId);
}
