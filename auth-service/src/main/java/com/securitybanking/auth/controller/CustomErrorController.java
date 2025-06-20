package com.securitybanking.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get error attributes safely
            Integer statusCode = getStatusCode(request);
            String errorMessage = getErrorMessage(request);
            String requestUri = getRequestUri(request);
            Exception exception = getException(request);

            logger.error("Error occurred - Status: {}, URI: {}, Message: {}",
                    statusCode, requestUri, errorMessage);

            if (exception != null) {
                logger.error("Exception details: {}", exception.getMessage(), exception);
            }

            // Handle OAuth2 related errors
            if (requestUri != null && (requestUri.contains("/oauth2") || requestUri.contains("/login/oauth2"))) {
                response.put("error", "OAuth2 Authentication Error");
                response.put("message", "There was an error during OAuth2 authentication. Please try again.");
                response.put("redirectUrl", "http://localhost:3000/login?error=oauth2_failed");
                response.put("timestamp", LocalDateTime.now().toString());
                response.put("status", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Build error response
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("status", statusCode);
            response.put("error", getErrorReason(statusCode));
            response.put("message", errorMessage != null && !errorMessage.isEmpty() ?
                    errorMessage : "An unexpected error occurred");
            response.put("path", requestUri);

            // Add additional debugging info in development
            if (exception != null) {
                response.put("exception", exception.getClass().getSimpleName());
            }

            HttpStatus status = statusCode != null ?
                    HttpStatus.valueOf(statusCode) : HttpStatus.INTERNAL_SERVER_ERROR;

            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            logger.error("Error in error controller", e);

            // Fallback error response
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("status", 500);
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while processing the error");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Integer getStatusCode(HttpServletRequest request) {
        try {
            Object statusObj = request.getAttribute("javax.servlet.error.status_code");
            if (statusObj == null) {
                statusObj = request.getAttribute("jakarta.servlet.error.status_code");
            }
            return statusObj instanceof Integer ? (Integer) statusObj : null;
        } catch (Exception e) {
            logger.debug("Could not get status code", e);
            return null;
        }
    }

    private String getErrorMessage(HttpServletRequest request) {
        try {
            String message = (String) request.getAttribute("javax.servlet.error.message");
            if (message == null) {
                message = (String) request.getAttribute("jakarta.servlet.error.message");
            }
            return message;
        } catch (Exception e) {
            logger.debug("Could not get error message", e);
            return null;
        }
    }

    private String getRequestUri(HttpServletRequest request) {
        try {
            String uri = (String) request.getAttribute("javax.servlet.error.request_uri");
            if (uri == null) {
                uri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
            }
            return uri;
        } catch (Exception e) {
            logger.debug("Could not get request URI", e);
            return request.getRequestURI();
        }
    }

    private Exception getException(HttpServletRequest request) {
        try {
            Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
            if (exception == null) {
                exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
            }
            return exception;
        } catch (Exception e) {
            logger.debug("Could not get exception", e);
            return null;
        }
    }

    private String getErrorReason(Integer statusCode) {
        if (statusCode == null) return "Internal Server Error";

        return switch (statusCode) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "Error";
        };
    }
}