package com.parkar.parksaathi.exception.customexceptions;

public class InvalidLocationParametersException extends RuntimeException {
    
    public InvalidLocationParametersException(String message) {
        super(message);
    }
    
    public InvalidLocationParametersException(String message, Throwable cause) {
        super(message, cause);
    }
}
