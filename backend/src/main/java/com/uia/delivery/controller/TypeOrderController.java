package com.uia.delivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uia.delivery.entity.subsidiary.TypeOrder;
import com.uia.delivery.service.TypeOrderService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@Slf4j
@RestController
@RequestMapping("/api/type-order")
public class TypeOrderController 
{
    private final TypeOrderService typeOrderService;

    public TypeOrderController(TypeOrderService typeOrderService)
    {
        this.typeOrderService = typeOrderService;
    }

    @GetMapping
    public ResponseEntity<List<TypeOrder>> getTypesOrder() 
    {
        log.info("GET '/api/type-order'");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(typeOrderService.getTypesOrder());
    }
    
}
