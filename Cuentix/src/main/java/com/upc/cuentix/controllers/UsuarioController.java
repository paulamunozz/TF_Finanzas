
package com.upc.cuentix.controllers;

import com.upc.cuentix.dto.ClienteDTO;
import com.upc.cuentix.dto.UsuarioDTO;
import com.upc.cuentix.services.ClienteService;
import com.upc.cuentix.services.UsuarioService;
import com.upc.cuentix.validations.Create;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import com.upc.cuentix.dto.EliminarCuentaRequest;

import com.upc.cuentix.dto.AcuerdoClienteDTO;
import com.upc.cuentix.services.CuentaCreditoService;
import java.util.List;


@RestController
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private CuentaCreditoService cuentaCreditoService;

    @PostMapping("/login")
    public UsuarioDTO login(
            @RequestBody @Validated(Create.class)
            UsuarioDTO usuarioDTO,
            HttpServletRequest request
    ) {

        UsuarioDTO respuesta =
                usuarioService.login(usuarioDTO);

        HttpSession anterior = request.getSession(false);

        if (anterior != null) {
            anterior.invalidate();
        }

        HttpSession sesion = request.getSession(true);

        String token = UUID.randomUUID().toString();

        sesion.setAttribute("usuarioId", respuesta.getId());
        sesion.setAttribute("csrf", token);

        respuesta.setCsrfToken(token);

        return respuesta;
    }

    @PutMapping("/api/cliente/mi-perfil")
    public ClienteDTO actualizarPerfil(
            @RequestBody ClienteDTO datos,
            @RequestHeader("X-Cuentix-CSRF") String token,
            HttpServletRequest request
    ) {

        Integer usuarioId = validarSesion(request, token);

        return clienteService.actualizarMiPerfil(
                usuarioId,
                datos
        );
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Void> cerrarSesion(
            @RequestHeader("X-Cuentix-CSRF") String token,
            HttpServletRequest request
    ) {

        validarSesion(request, token);

        request.getSession(false).invalidate();

        return ResponseEntity.noContent().build();
    }

    private Integer validarSesion(
            HttpServletRequest request,
            String token
    ) {

        HttpSession sesion = request.getSession(false);

        if (sesion == null ||
                !(sesion.getAttribute("usuarioId")
                        instanceof Integer)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debe iniciar sesión"
            );
        }

        String esperado = (String) sesion.getAttribute("csrf");

        if (esperado == null ||
                !MessageDigest.isEqual(
                        esperado.getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8)
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solicitud no autorizada"
            );
        }

        return (Integer) sesion.getAttribute("usuarioId");
    }

    @DeleteMapping("/api/cliente/mi-cuenta")
    public ResponseEntity<Void> eliminarMiCuenta(
            @RequestBody EliminarCuentaRequest datos,
            @RequestHeader("X-Cuentix-CSRF") String token,
            HttpServletRequest request
    ) {

        // Obtener el usuario de la sesión validada.
        Integer usuarioId = validarSesion(
                request,
                token
        );

        // Desactivar la cuenta en PostgreSQL.
        clienteService.eliminarMiCuenta(
                usuarioId,
                datos.contrasenaActual()
        );

        // Invalidar la sesión después de eliminar.
        HttpSession sesion = request.getSession(false);

        if (sesion != null) {
            sesion.invalidate();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/cliente/mis-acuerdos")
    public List<AcuerdoClienteDTO> obtenerMisAcuerdos(
            @RequestHeader("X-Cuentix-CSRF") String token,
            HttpServletRequest request
    ) {

        Integer usuarioId = validarSesion(
                request,
                token
        );

        return cuentaCreditoService.acuerdosDelCliente(
                usuarioId
        );
    }


}