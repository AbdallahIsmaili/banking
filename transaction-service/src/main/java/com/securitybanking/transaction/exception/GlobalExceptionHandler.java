package com.securitybanking.transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.access.AccessDeniedException;
import jakarta.transaction.InvalidTransactionException;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AccountNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des exceptions de validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    // Gestion des exceptions métier
    @ExceptionHandler({
            AccountNotFoundException.class,
            // TransactionNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Gestion des exceptions génériques
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne est survenue: " + ex.getMessage(),
                System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Classe interne pour la structure de réponse d'erreur
    public static class ErrorResponse {
        private final int status;
        private final String message;
        private final long timestamp;

        public ErrorResponse(int status, String message, long timestamp) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }

        // Getters
        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<String> handleAuthException(AuthenticationCredentialsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT manquant ou invalide.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé : rôle insuffisant.");
    }

    // SUPPRIMÉ : la deuxième méthode @ExceptionHandler(Exception.class)
}