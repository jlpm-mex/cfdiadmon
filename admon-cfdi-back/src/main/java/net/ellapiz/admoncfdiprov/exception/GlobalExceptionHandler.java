package net.ellapiz.admoncfdiprov.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(
            MultipartException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            "INVALID_REQUEST",
            "La petición debe ser multipart/form-data con archivos",
            request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // ← 400 Bad Request
                .body(error);
    }
    
    @ExceptionHandler(AdmonCfdiProvException.class)
    public ResponseEntity<ErrorResponse> handleAdmonCfdiProvException(
            AdmonCfdiProvException ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode(), 
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) 
                .body(error);
    }
    
    // Manejo de errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación");
        
        LOGGER.warn("Validation error: {}", errorMessage);
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            errorMessage,
            request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
    
    // Manejo de errores genéricos (catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        LOGGER.error("Unexpected error: ", ex);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "Ha ocurrido un error inesperado",
            request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}