package org.example.basicfhirserver.exceptions;

import lombok.Getter;

@Getter
public class BulkExportValidationException extends RuntimeException {
    private final int statusCode;

    public BulkExportValidationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

}

