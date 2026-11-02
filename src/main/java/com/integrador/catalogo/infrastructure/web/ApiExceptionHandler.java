package com.integrador.catalogo.infrastructure.web;

import com.integrador.catalogo.application.exception.ProfessionalNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce errores a application/problem+json con un "code" estable (mismo formato que la catedra).
 * El cliente decide por status y code, nunca por el texto. No se filtran trazas ni detalles internos.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> invalidArgument(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> invalidParameter(MethodArgumentTypeMismatchException e) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Valor invalido para el parametro " + e.getName());
    }

    @ExceptionHandler(ProfessionalNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(ProfessionalNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "PROFESSIONAL_NOT_FOUND", e.getMessage());
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(status.getReasonPhrase());
        body.setProperty("code", code);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
    }
}
