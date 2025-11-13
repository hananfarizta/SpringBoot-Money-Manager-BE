package dev.hananfarizta.moneymanager.exception;

import dev.hananfarizta.moneymanager.dto.ApiResponseDTO;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {

    /**
     * 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseDTO<Object>> handleEmptyRequestBody(HttpMessageNotReadableException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>(
                "error",
                "Request body cannot be empty or is invalid JSON",
                null);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 405 Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        var supportedMethods = ex.getSupportedHttpMethods();
        String message = String.format(
                "Method %s is not supported for this endpoint. Supported methods are %s",
                ex.getMethod(),
                supportedMethods != null
                        ? String.join(", ", supportedMethods.stream().map(HttpMethod::name).toList())
                        : "NONE");

        ApiResponseDTO<Object> response = new ApiResponseDTO<>(
                "error",
                message,
                null);

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        ApiResponseDTO<Object> response = new ApiResponseDTO<>(
                "error",
                "Validation failed",
                errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 400 Bad Request
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleBadCredentials(BadCredentialsException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>("error", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleGeneric(IllegalArgumentException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>(
                "error",
                ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleServerError(Exception ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>(
                "error",
                "Internal server error: " + ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
