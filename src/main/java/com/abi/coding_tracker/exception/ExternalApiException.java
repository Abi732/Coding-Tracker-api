package com.abi.coding_tracker.exception;

public class ExternalApiException extends RuntimeException {
    
    private final int statusCode;
    public ExternalApiException(String message, int statusCode){
        super(message);
        this.statusCode = statusCode;

    }

    public int getStatusCode(){
        return statusCode;
    }
}
