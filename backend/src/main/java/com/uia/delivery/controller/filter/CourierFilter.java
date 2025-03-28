package com.uia.delivery.controller.filter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourierFilter 
{
    private String firstname;
    private String surname;
    private String lastname;
    private Double minCost;
    private Double maxCost;
    private Double minSpeed;
    private Double maxSpeed;
    private Integer minLoadCapacity;
    private Integer maxLoadCapacity;
    private String supportedTypesOrder;
}
