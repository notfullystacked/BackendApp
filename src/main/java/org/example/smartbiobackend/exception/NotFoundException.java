package org.example.smartbiobackend.exception;

// Kastes når noget ikke findes, fx GET /api/movies/999. Bliver til 404 i GlobalExceptionHandler
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
