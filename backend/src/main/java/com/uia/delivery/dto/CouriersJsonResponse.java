package com.uia.delivery.dto;

import java.util.List;

import com.uia.delivery.entity.Courier;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CouriersJsonResponse 
{
    private List<Courier> couriers;
}
