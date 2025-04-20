package com.uia.delivery.dto.mapresponse;

import java.util.List;

import lombok.Data;

@Data
public class MapResponse 
{
    private List<CourierMapResponse> couriers;
    private List<OrderMapResponse> orders;
    private List<ScheduleMapResponse> schedules;
}
