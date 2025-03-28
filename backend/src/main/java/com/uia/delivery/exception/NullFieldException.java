package com.uia.delivery.exception;

public class NullFieldException extends RuntimeException 
{
    public NullFieldException(String typeObject) 
    {
        super(typeObject + " is null.");
    }
}
