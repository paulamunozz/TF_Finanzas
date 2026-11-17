package com.upc.cuentix.services;

import com.upc.cuentix.dto.ItemDTO;
import com.upc.cuentix.entities.Item;
import com.upc.cuentix.instances.IItemService;
import com.upc.cuentix.repositories.CompraRepository;
import com.upc.cuentix.repositories.ItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService implements IItemService {
    @Autowired private ItemRepository itemRepository;
    @Autowired private CompraRepository compraRepository;
    @Autowired private ModelMapper modelMapper;

    @Override
    public ItemDTO registrarItem(ItemDTO itemDTO) {
        if (!compraRepository.existsById(itemDTO.getCompraId())) {
            throw new RuntimeException("No existe");
        }
        Item item = modelMapper.map(itemDTO, Item.class);
        itemRepository.save(item);
        return modelMapper.map(item, ItemDTO.class);
    }

    @Override
    public ItemDTO editarItem(ItemDTO itemDTO) {
        Item item = itemRepository.findById(itemDTO.getId()).get();
        item.setCantidad(itemDTO.getCantidad());
        itemRepository.save(item);
        return modelMapper.map(item, ItemDTO.class);
    }

    @Override
    public void eliminarItem(Integer id) {
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDTO> itemsPorCompra(Integer compraId) {
        return itemRepository.findByCompra_Id(compraId)
                .stream().map(item -> modelMapper.map(item, ItemDTO.class))
                .collect(Collectors.toList());
    }
}
