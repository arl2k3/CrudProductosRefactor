package com.epam.productos.exception;

import com.epam.productos.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacionDeCuerpo(MethodArgumentNotValidException ex) {
        List<String> mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mensajeDeCampo)
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, mensajes);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> validacionDeParametros(HandlerMethodValidationException ex) {
        List<String> mensajes = ex.getParameterValidationResults().stream()
                .flatMap(result -> {
                    String parametro = result.getMethodParameter().getParameterName();
                    String nombre = parametro == null ? "parametro" : parametro;
                    return result.getResolvableErrors().stream()
                            .map(error -> nombre + ": " + error.getDefaultMessage());
                })
                .toList();
        if (mensajes.isEmpty()) {
            mensajes = List.of("Parámetros de consulta inválidos");
        }
        return respuesta(HttpStatus.BAD_REQUEST, mensajes);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> violaciones(ConstraintViolationException ex) {
        List<String> mensajes = ex.getConstraintViolations().stream()
                .map(this::mensajeDeViolacion)
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, mensajes);
    }

    @ExceptionHandler({PrecioInvalidoException.class, InventarioInvalidoException.class, PaginacionInvalidaException.class})
    public ResponseEntity<ErrorResponse> reglaDeNegocio(RuntimeException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, List.of(ex.getMessage()));
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(ProductoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, List.of(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, List.of(ex.getName() + ": valor inválido"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, List.of("El cuerpo de la petición no es válido"));
    }

    private String mensajeDeCampo(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private String mensajeDeViolacion(ConstraintViolation<?> violation) {
        String ruta = violation.getPropertyPath().toString();
        int separador = ruta.lastIndexOf('.');
        String campo = separador >= 0 ? ruta.substring(separador + 1) : ruta;
        return campo + ": " + violation.getMessage();
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, List<String> mensajes) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensajes
        );
        return ResponseEntity.status(status).body(body);
    }
}
