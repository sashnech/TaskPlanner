package com.example.taskplanner.controller.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@RestControllerAdvice(assignableTypes = TaskRestController.class)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new TreeMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        ex.getBody().setDetail("Дані задачі не пройшли валідацію");
        ex.getBody().setProperty("errors", errors);

        return handleExceptionInternal(ex, ex.getBody(), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, List<String>> errors = new TreeMap<>();

        ex.getParameterValidationResults().forEach(result -> errors.put(
                result.getMethodParameter().getParameterName(),
                result.getResolvableErrors().stream()
                        .map(e -> e.getDefaultMessage())
                        .toList()
        ));

        ex.getBody().setDetail("Некоректні параметри запиту");
        ex.getBody().setProperty("errors", errors);

        return handleExceptionInternal(ex, ex.getBody(), headers, status, request);
    }
}
