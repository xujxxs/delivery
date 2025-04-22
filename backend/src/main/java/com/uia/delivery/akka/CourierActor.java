package com.uia.delivery.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import com.uia.delivery.akka.message.CourierMessage;
import com.uia.delivery.akka.message.DispatcherMessage;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.entity.Schedule;
import com.uia.delivery.entity.subsidiary.TypeOperation;
import com.uia.delivery.service.ScheduleService;
import com.uia.delivery.service.algorithm.SchedulingAlgorithm.ResultFindMaxProfit;
import com.uia.delivery.service.algorithm.SchedulingAlgorithm.ResultRebuildSchedule;

@Slf4j
public class CourierActor extends AbstractActor
{
    private Courier courier;
    private final ScheduleService scheduleService;

    public CourierActor(
            Courier courier, 
            ScheduleService scheduleService
    ) {
        this.courier = courier;
        this.scheduleService = scheduleService;
    }

    public static Props props(
            Courier courier, 
            ScheduleService scheduleService
    ) {
        return Props.create(CourierActor.class, () -> new CourierActor(courier, scheduleService));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CourierMessage.GetCourier.class, msg -> {
                    log.trace("Courier: {}, received request: 'CourierMessage.GetCourier'", courier.getId());
                    log.trace("Courier: {}, answer on request: 'CourierMessage.GetAssignedOrderIds'. Response object: {}", 
                        courier.getId(), courier);

                    getSender().tell(courier, getSelf());
                })
                .match(CourierMessage.GetAssignedOrderIds.class, msg -> {
                    log.trace("Courier: {}, received request: 'CourierMessage.GetAssignedOrderIds'", courier.getId());
                    List<Long> ans = scheduleService.getSchedulesByCourierId(courier.getId()).stream()
                            .filter(write -> write.getTypeOperation().equals(TypeOperation.PICKUP))
                            .map(write -> write.getDeliveryOrder().getId())
                        .toList();
                    
                    log.trace("Courier: {}, answer on request: 'CourierMessage.GetAssignedOrderIds'. Response object: {}", 
                        courier.getId(), ans);
                    getSender().tell(ans, getSelf());
                })
                .match(CourierMessage.ComputeMaxProfitWithOrder.class, msg -> {
                    log.trace("Courier: {}, received request: 'CourierMessage.ComputeMaxProfitWithOrder'", courier.getId());
                    Double ans = scheduleService.findMaxProfit(
                            courier, 
                            scheduleService.getSchedulesByCourierId(courier.getId()), 
                            msg.getOrder()
                        ).getMaxProfit();

                    log.trace("Courier: {}, answer on request: 'CourierMessage.ComputeMaxProfitWithOrder'. Response object: {}", 
                        courier.getId(), ans);
                    getSender().tell(ans, getSelf());
                })
                .match(DispatcherMessage.BindRequest.class, msg -> getSender().tell(handleAddOrder(msg), getSelf()))
                .match(DispatcherMessage.UpdateCourier.class, this::handleUpdateCourier)
                .match(DispatcherMessage.UpdateOrder.class, this::handleUpdateOrder)
                .match(DispatcherMessage.DeleteOrder.class, this::handleDeleteOrder)
            .build();
    }

    private boolean handleAddOrder(DispatcherMessage.BindRequest msg)
    {
        List<Schedule> courierSchedule = scheduleService.getSchedulesByCourierId(courier.getId());
        DeliveryOrder order = msg.getOrder();
        log.debug("Bind courier: {}, with order: {}", courier.getId(), order.getId());
        ResultFindMaxProfit result = scheduleService.findMaxProfit(courier, courierSchedule, order);
        
        if(result.getMaxProfit() < 0)
        {
            log.warn("Order: {}, not binded with courier: {}. By: profit: {}, < 0 on pickupIndex: {}, deliveryIndex: {}",
                order.getId(), courier.getId(), result.getMaxProfit(), result.getPickupIndex(), result.getDeliveryIndex());
            return false;
        }
        
        courierSchedule = scheduleService.buildSchedule(courier, order, courierSchedule, result.getPickupIndex(), result.getDeliveryIndex());
        List<Schedule> savedSchedule = scheduleService.saveShedules(courierSchedule);
        log.info("Order: {}, added to schedule courier: {} on (pickupIndex: {}, deliveryIndex: {})",
            order.getId(), courier.getId(), result.getPickupIndex(), result.getDeliveryIndex());
        
        log.debug("New schedule courier: {}, List<Schedule>: {}", 
            courier.getId(), savedSchedule);
        scheduleService.receiveUpdatedSchedule(courier.getId());
        return true;
    }

    private void handleUpdateCourier(DispatcherMessage.UpdateCourier msg)
    {
        this.courier = msg.getCourier();
        processScheduleUpdate(courier, "update courier");
    }

    private void handleUpdateOrder(DispatcherMessage.UpdateOrder msg)
    {
        processScheduleUpdate(courier, "update order: " + msg.getOrder().getId());
    }

    private void handleDeleteOrder(DispatcherMessage.DeleteOrder msg)
    {
        processScheduleUpdate(courier, "delete order: " + msg.getOrderId());
    }

    private void processScheduleUpdate(Courier courier, String processDescription)
    {
        log.debug("Check on valid schedule courier: {}", courier.getId());
        List<Schedule> courierSchedule = scheduleService.getSchedulesByCourierId(courier.getId());
        if(scheduleService.computeScheduleProfit(courier, courierSchedule) >= 0)
        {
            log.info("Schedule courier: {}, is valid after {}", 
                courier.getId(), processDescription);
            scheduleService.receiveUpdatedSchedule(courier.getId());
            return;
        }
        log.debug("Schedule courier: {}, is not valid after {}", 
            courier.getId(), processDescription);
        
        ResultRebuildSchedule resultRebuild = scheduleService.rebuildSchedule(courier, courierSchedule);
        
        if(!resultRebuild.getRefusedOrders().isEmpty()) {
            log.debug("Failed to save: {}, orders from old schedule. Try search new courier for orders", resultRebuild.getRefusedOrders().size());
            getSender().tell(new DispatcherMessage.RefindOrder(resultRebuild.getRefusedOrders(), courier.getId()), getSelf());
        } else {
            log.debug("All orders were saved from the old schedule");
        }
        scheduleService.saveShedules(resultRebuild.getNewSchedule());
        scheduleService.receiveUpdatedSchedule(courier.getId());
    }
}
