package com.uia.delivery.akka.message;

import com.uia.delivery.entity.DeliveryOrder;

import lombok.AllArgsConstructor;
import lombok.Data;

public class CourierMessage 
{
    private CourierMessage()
    {
        throw new IllegalStateException("Utility class");
    }
    
    public static class GetCourier { }
    public static class GetAssignedOrderIds { }
    
    @Data
    @AllArgsConstructor
    public static class ComputeMaxProfitWithOrder {
        private DeliveryOrder order;
    }
}
