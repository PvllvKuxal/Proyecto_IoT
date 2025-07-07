package com.example.Proyecto_IoT.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = "Error interno";

        if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST.value();
            message = ex.getMessage();
        } else if (ex instanceof org.springframework.web.client.HttpClientErrorException.NotFound) {
            status = HttpStatus.NOT_FOUND.value();
            message = "No encontrado";
        } else if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            message = ex.getMessage();
        }

        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("message", message);

        return ResponseEntity.status(status).body(error);
    }
}

