package com.uia.delivery.exception;

public class ExportException extends RuntimeException 
{
    public ExportException(String typeObject) 
    {
        super(typeObject + " can't be export.");
    }
}
