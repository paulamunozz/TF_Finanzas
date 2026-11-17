package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.ItemDTO;
import com.upc.cuentix.services.ItemService;
import com.upc.cuentix.validations.Create;
import com.upc.cuentix.validations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ItemController {
    @Autowired private ItemService itemService;

    @PostMapping("/registrarItem")
    public ItemDTO registrarItem(@RequestBody @Validated(Create.class) ItemDTO itemDTO) {
        return itemService.registrarItem(itemDTO);
    }

    @PutMapping("/editarItem")
    public ItemDTO editarItem(@RequestBody @Validated(Update.class) ItemDTO itemDTO) {
        return itemService.editarItem(itemDTO);
    }

    @DeleteMapping("/eliminarItem/{idItem}")
    public void eliminarItem(@PathVariable Integer idItem) {
        itemService.eliminarItem(idItem);
    }

    @PostMapping("/itemsPorCompra")
    public List<ItemDTO> itemsPorCompra(Integer compraId) {
        return itemService.itemsPorCompra(compraId);
    }
}