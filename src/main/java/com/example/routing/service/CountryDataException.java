package com.example.routing.service;

public class CountryDataException extends RuntimeException {

    public CountryDataException(String message) {
        super(message);
    }

    public CountryDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
