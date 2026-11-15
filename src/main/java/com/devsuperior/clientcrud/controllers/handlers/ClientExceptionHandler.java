package com.devsuperior.clientcrud.controllers.handlers;

import java.time.Instant;
import java.util.List;

import com.devsuperior.clientcrud.services.exceptions.ClientNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ClientExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(ClientNotFoundException exception, HttpServletRequest request) {
        return new ApiError(Instant.now(), 404, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ValidationError handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldMessage> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldMessage(error.getField(), error.getDefaultMessage()))
                .toList();
        return new ValidationError(Instant.now(), 422, "Erro de validação.", request.getRequestURI(), errors);
    }

    public record ApiError(Instant timestamp, int status, String error, String path) {
    }

    public record ValidationError(Instant timestamp, int status, String error, String path,
                                  List<FieldMessage> errors) {
    }

    public record FieldMessage(String fieldName, String message) {
    }
}
