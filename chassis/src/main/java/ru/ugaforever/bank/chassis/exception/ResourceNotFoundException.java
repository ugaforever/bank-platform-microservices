package ru.ugaforever.bank.chassis.exception;

import org.springframework.http.HttpStatus;

// 404 Not Found
public class ResourceNotFoundException extends BankApplicationException {
    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with id %s not found", resourceType, resourceId),
                "NOT_FOUND_001", HttpStatus.NOT_FOUND);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message) {
        super(message, "NOT_FOUND_001", HttpStatus.NOT_FOUND);
        this.resourceType = null;
        this.resourceId = null;
    }
}
