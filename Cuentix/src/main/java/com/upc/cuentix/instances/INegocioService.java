
package com.upc.cuentix.instances;
import com.upc.cuentix.dto.NegocioDTO;
import java.util.List;

public interface INegocioService {
    NegocioDTO registrar(NegocioDTO negocioDTO);
    NegocioDTO modificar(NegocioDTO negocioDTO);
    String eliminar(Integer id);
    NegocioDTO buscarPorRuc(String ruc);
    NegocioDTO buscarPorId(Integer id);
    List<NegocioDTO> listar();
}