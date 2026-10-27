package com.upc.cuentix.instances;

import com.upc.cuentix.dto.ClienteDTO;

public interface IClienteService {
    ClienteDTO registrar(ClienteDTO clienteDTO);
    ClienteDTO modificar(ClienteDTO clienteDTO);
    String eliminar(Integer id);
    ClienteDTO buscarPorDni(String dni);
    ClienteDTO buscarPorId(Integer id);
}