package com.uia.delivery.exception;

public class ImportException extends RuntimeException 
{
    public ImportException(String typeObject) 
    {
        super(typeObject + " can't be import.");
    }
}
