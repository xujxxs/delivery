package com.uia.delivery.akka.query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.uia.delivery.akka.message.CourierMessage;
import com.uia.delivery.akka.message.DispatcherMessage;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.entity.DeliveryOrder;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import scala.compat.java8.FutureConverters;

@Slf4j
public class CourierQuery 
{
    private static final Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    
    private CourierQuery() {
        throw new IllegalStateException("Utility class");
    }

    public static CompletableFuture<Courier> fetchCourier(ActorRef courierRef) 
    {
        log.trace("Sending 'CourierMessage.GetCourier' to courierRef: {}", courierRef.path().name());
        return FutureConverters.toJava(
                Patterns.ask(courierRef, new CourierMessage.GetCourier(), timeout)
            ).thenApply(response -> {
                log.debug("Message 'CourierMessage.GetCourier' from courierRef: {}, response: {}",
                    courierRef.path().name(), response);
                return (Courier) response;
            })
            .exceptionally(ex -> {
                log.error("Error receiving 'CourierMessage.GetCourier' from courierRef: {}. By: {}",
                    courierRef.path().name(), ex.getMessage());
                return null;
            })
            .toCompletableFuture();
    }

    public static CompletableFuture<List<Long>> fetchAssignedOrderIds(ActorRef courierRef) 
    {
        log.trace("Sending 'CourierMessage.GetAssignedOrderIds' to courierRef: {}", courierRef.path().name());
        return FutureConverters.toJava(
                Patterns.ask(courierRef, new CourierMessage.GetAssignedOrderIds(), timeout)
            ).thenApply(response -> {
                log.trace("Message 'CourierMessage.GetAssignedOrderIds' from courierRef: {}, response: {}",
                    courierRef.path().name(), response);

                List<Long> ansList = new ArrayList<>();
                if (response instanceof Iterable<?> iterable) {
                    for (Object obj : iterable) {
                        if (obj instanceof Long orderId)
                            ansList.add(orderId);
                    }
                }

                log.debug("Convert response: {}, to List<Long>: {}", 
                    response, ansList);
                return ansList;
            })
            .exceptionally(ex -> {
                log.error("Error receiving 'CourierMessage.GetAssignedOrderIds' from courierRef: {}. By: {}",
                    courierRef.path().name(), ex.getMessage());
                return null;
            })
            .toCompletableFuture();
    }

    public static CompletableFuture<Double> computeMaxProfitForOrder(ActorRef courierRef, DeliveryOrder msg) 
    {
        log.trace("Sending 'CourierMessage.ComputeMaxProfitWithOrder': {}, to courierRef: {}", 
            msg, courierRef.path().name());
        return FutureConverters.toJava(
                Patterns.ask(courierRef, new CourierMessage.ComputeMaxProfitWithOrder(msg), timeout)
            ).thenApply(response -> {
                log.debug("Message 'CourierMessage.ComputeMaxProfitWithOrder' from courierRef: {}, response: {}",
                    courierRef.path().name(), response);
                return (Double) response;
            })
            .exceptionally(ex -> {
                log.error("Error receiving 'CourierMessage.ComputeMaxProfitWithOrder' from courierRef: {}. By: {}",
                    courierRef.path().name(), ex.getMessage());
                return -1.0;
            })
            .toCompletableFuture();
    }

    public static CompletableFuture<Boolean> tryAddOrderToCourier(
            ActorRef courierRef, 
            DispatcherMessage.BindRequest msg)
    {
        log.trace("Sending 'DispatcherMessage.BindRequest': {}, to courierRef: {}", 
            msg, courierRef.path().name());
        return FutureConverters.toJava(
                Patterns.ask(courierRef, msg, timeout)
            ).thenApply(response -> {
                log.debug("Message 'DispatcherMessage.BindRequest' from courierRef: {}, response: {}",
                    courierRef.path().name(), response);
                return (boolean) response;
            })
            .exceptionally(ex -> {
                log.error("Error receiving 'DispatcherMessage.BindRequest' from courierRef: {}. By: {}",
                    courierRef.path().name(), ex.getMessage());
                return false;
            })
            .toCompletableFuture();
    }
}
