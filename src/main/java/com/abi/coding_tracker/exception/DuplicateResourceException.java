package com.abi.coding_tracker.exception;

public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message){
        super(message);
    }
}
