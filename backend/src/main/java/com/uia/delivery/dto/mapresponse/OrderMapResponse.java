package com.uia.delivery.dto.mapresponse;

import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.entity.subsidiary.Coordinates;

import lombok.Data;

@Data
public class OrderMapResponse 
{
    private Long id;
    private Coordinates startPoint;
    private Coordinates endPoint;

    public OrderMapResponse(DeliveryOrder deliveryOrder)
    {
        this.id = deliveryOrder.getId();
        this.startPoint = deliveryOrder.getPositionPickUp();
        this.endPoint = deliveryOrder.getPositionDelivery();
    }
}
