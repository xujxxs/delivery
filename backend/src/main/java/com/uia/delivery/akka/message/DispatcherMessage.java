package com.uia.delivery.akka.message;

import java.util.List;

import com.uia.delivery.entity.Courier;
import com.uia.delivery.entity.DeliveryOrder;

import akka.actor.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Data;

public class DispatcherMessage
{
    private DispatcherMessage()
    {
        throw new IllegalStateException("Utility class");
    }

    @Data
    @AllArgsConstructor
    public static class BindRequest {
        private ActorRef courierRef;
        private DeliveryOrder order;
    }

    @Data
    @AllArgsConstructor
    public static class CreateCourier {
        private Courier courier;
    } 

    @Data
    @AllArgsConstructor
    public static class UpdateCourier {
        private Courier courier;
    } 

    @Data
    @AllArgsConstructor
    public static class DeleteCourier {
        private Long courierId;
    } 

    @Data
    @AllArgsConstructor
    public static class CreateOrder {
        private DeliveryOrder order;
    } 

    @Data
    @AllArgsConstructor
    public static class UpdateOrder {
        private DeliveryOrder order;
    } 

    @Data
    @AllArgsConstructor
    public static class RefindOrder {
        private List<DeliveryOrder> orders;
        private Long courierId;
    } 

    @Data
    @AllArgsConstructor
    public static class DeleteOrder {
        private Long orderId;
    } 
}
