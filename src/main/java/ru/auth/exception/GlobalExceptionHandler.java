package ru.auth.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import ru.auth.dto.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponse>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ErrorResponse(err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            UserNotFoundException.class,
            RoleNotFoundException.class,
            InvalidTokenException.class,
            InvalidCredentialsException.class
    })
    public ResponseEntity<ErrorResponse> handleCustom(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        var error = new ErrorResponse("error", "Internal server error");
        return ResponseEntity.status(500).body(error);
    }
}
