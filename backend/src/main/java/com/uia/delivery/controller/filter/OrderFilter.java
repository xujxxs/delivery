package com.uia.delivery.controller.filter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderFilter 
{
    private String name;
    private Integer minWeight;
    private Integer maxWeight;
    private Double minCost;
    private Double maxCost;
    private Long minDeliveryPeriod;
    private Long maxDeliveryPeriod;
    private String typesOrder;
}
