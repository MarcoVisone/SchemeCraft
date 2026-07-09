package com.xyra.schemecraft.exception;

/**
 * Generic unchecked exception for data access and persistence layer errors.
 */
public class DAOException extends RuntimeException{

    public DAOException(String message){
        super(message);
    }

    public DAOException(String message, Throwable cause){
        super(message, cause);
    }
}
