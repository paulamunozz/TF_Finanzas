package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.NegocioDTO;
import com.upc.cuentix.entities.Giro;
import com.upc.cuentix.repositories.GiroRepository;
import com.upc.cuentix.services.NegocioService;
import com.upc.cuentix.validations.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/businesses")
@RequiredArgsConstructor
public class AdminBusinessController {
    private final NegocioService service;
    private final GiroRepository giros;
    @GetMapping public List<NegocioDTO> list() { return service.adminList(); }
    @GetMapping("/giros") public List<Giro> giros() { return giros.findAll(); }
    @PostMapping public NegocioDTO create(@Validated(Create.class) @RequestBody NegocioDTO data) { return service.registrar(data); }
    @PutMapping("/{id}") public NegocioDTO edit(@PathVariable Integer id, @Validated(Update.class) @RequestBody NegocioDTO data) {
        data.setId(id);
        return service.modificar(data);
    }
    @PutMapping("/{id}/active") public NegocioDTO active(@PathVariable Integer id, @RequestParam boolean value) {
        return service.setActive(id, value);
    }
}
