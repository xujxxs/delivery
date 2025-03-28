package com.uia.delivery.exception;

public class NotFoundException extends RuntimeException 
{
    public NotFoundException(String typeObject) 
    {
        super(typeObject + " not found.");
    }
}
