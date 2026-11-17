package com.upc.cuentix.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        log.error("Error occurred", ex);
        ErrorResponse error = new ErrorResponse(ex.getStatusCode().value(), ex.getReason());
        return new ResponseEntity<>(error, ex.getStatusCode());
    }

    //404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        log.error("Error occurred", ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Recurso no encontrado");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorResponse> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {
        log.error("Error occurred", ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Recurso no encontrado");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    //400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Error occurred", ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> manejarValidaciones(MethodArgumentNotValidException ex) {
//        Map<String, List<String>> errores = new HashMap<>();
//        ex.getBindingResult().getFieldErrors().forEach(error -> {
//            String campo = error.getField();
//            String mensaje = error.getDefaultMessage();
//            errores.computeIfAbsent(campo, key -> new ArrayList<>()).add(mensaje);
//        });
//
//        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errores.toString());
//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(MethodArgumentNotValidException ex) {
        // Tomamos solo el primer error (o concatenamos todos en un solo string si prefieres)
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación");

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                mensaje
        );

        return ResponseEntity.badRequest().body(error);
    }


    //500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleGeneralExceptionRuntime(RuntimeException ex) {
        log.error("Error occurred", ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Error occurred", ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ocurrió un error inesperado");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
