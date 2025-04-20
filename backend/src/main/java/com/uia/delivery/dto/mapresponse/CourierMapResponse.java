package com.uia.delivery.dto.mapresponse;

import com.uia.delivery.entity.Courier;
import com.uia.delivery.entity.subsidiary.Coordinates;

import lombok.Data;

@Data
public class CourierMapResponse 
{
    private Long id;
    private Coordinates point;

    public CourierMapResponse(Courier courier)
    {
        this.id = courier.getId();
        this.point = courier.getPosition();
    }
}
