package com.parkar.parksaathi.exception.customexceptions;

public class DuplicateParkingException extends RuntimeException {
    public DuplicateParkingException(String message) {
        super(message);
    }
}
