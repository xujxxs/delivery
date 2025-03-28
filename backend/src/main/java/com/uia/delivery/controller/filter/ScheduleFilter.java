package com.uia.delivery.controller.filter;

import com.uia.delivery.entity.subsidiary.TypeOperation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduleFilter 
{
    private Long minIndex;
    private Long maxIndex;
    private Long minCourierId;
    private Long maxCourierId;
    private Long minOrderId;
    private Long maxOrderId;
    private TypeOperation typeOperation;
    private Long minPeriodOperation;
    private Long maxPeriodOperation;
}
