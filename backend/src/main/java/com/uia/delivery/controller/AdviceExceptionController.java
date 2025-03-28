package com.uia.delivery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.exception.NullFieldException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class AdviceExceptionController 
{
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFoundException(NotFoundException ex)
    {
        log.warn("Resource not found: {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(NullFieldException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String nullFieldException(NullFieldException ex)
    {
        log.warn("Field was null: {}", ex.getMessage());
        return ex.getMessage();
    }
}
