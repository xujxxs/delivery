package com.uia.delivery.service.algorithm;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.uia.delivery.entity.Courier;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.entity.Schedule;
import com.uia.delivery.entity.subsidiary.Coordinates;
import com.uia.delivery.entity.subsidiary.TypeOperation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SchedulingAlgorithm
{
    public List<Schedule> buildSchedule(  // TODO: add unit test
            Courier courier,
            DeliveryOrder order, 
            List<Schedule> schedule, 
            int pickupIndex, 
            int deliveryIndex
    ) {
        log.trace("Adding order: {} to courier: {} schedule (pickupIndex: {}, deliveryIndex: {})",
                order.getId(), courier.getId(), pickupIndex, deliveryIndex);

        Coordinates receiveOrderPos;
        if(pickupIndex == 0) {
            receiveOrderPos = courier.getPosition();
        } else {
            receiveOrderPos = schedule.get(pickupIndex - 1).getPositionEnd();
        }

        Schedule pickupOrder = Schedule.builder()
                .courier(courier)
                .deliveryOrder(order)
                .positionStart(receiveOrderPos)
                .positionEnd(order.getPositionPickUp())
                .typeOperation(TypeOperation.PICKUP)
            .build();
        pickupOrder.calculateOperationTime();

        List<Schedule> newSchedule = new ArrayList<>(schedule);
        newSchedule.add(pickupIndex, pickupOrder);

        Schedule deliveryOrder = Schedule.builder()
                .courier(courier)
                .deliveryOrder(order)
                .positionStart(newSchedule.get(deliveryIndex - 1).getPositionEnd())
                .positionEnd(order.getPositionDelivery())
                .typeOperation(TypeOperation.DELIVERY)
            .build();
        deliveryOrder.calculateOperationTime();

        if (deliveryIndex <= newSchedule.size()) {
            newSchedule.add(deliveryIndex, deliveryOrder);
        } else {
            newSchedule.add(deliveryOrder);
        }

        log.trace("Schedule build successfully. New schedule size: {}", newSchedule.size());
        return newSchedule;
    }

    public ResultRebuildSchedule rebuildSchedule(  // TODO: add unit test
            Courier courier, 
            List<Schedule> courierSchedule)
    {
        log.debug("Rebuilding schedule for courier: {}", courier.getId());
        List<Schedule> newCourierSchedule = new ArrayList<>();
        List<DeliveryOrder> wasAssignedOrders = courierSchedule.stream()
                .filter(write -> write.getTypeOperation().equals(TypeOperation.PICKUP))
                .map(write -> new AbstractMap.SimpleEntry<>(
                    write.getDeliveryOrder(), 
                    findMaxProfit(courier, new ArrayList<>(), write.getDeliveryOrder()).getMaxProfit()))
                .sorted(Comparator.comparingDouble(entity -> -entity.getValue()))
                .map(Map.Entry::getKey)
            .toList();

        List<DeliveryOrder> refusedOrders = new ArrayList<>();
        for(DeliveryOrder wasAssignedOrder : wasAssignedOrders)
        {
            ResultFindMaxProfit result = findMaxProfit(courier, newCourierSchedule, wasAssignedOrder);
            if(result.getMaxProfit() < 0) {
                log.debug("Order {} cannot be added to courier {} schedule", wasAssignedOrder.getId(), courier.getId());
                refusedOrders.add(wasAssignedOrder);
                continue;
            }
            
            newCourierSchedule = buildSchedule(
                courier,
                wasAssignedOrder, 
                newCourierSchedule, 
                result.getPickupIndex(), 
                result.getDeliveryIndex()
            );
        }
        log.debug("Schedule rebuild completed. New schedule size: {}. Orders refused: {}",
            newCourierSchedule.size(), refusedOrders.size());
        return new ResultRebuildSchedule(newCourierSchedule, refusedOrders);
    }

    public ResultFindMaxProfit findMaxProfit(Courier courier, List<Schedule> courierSchedule, DeliveryOrder addedOrder)  // TODO: add unit test
    {
        log.debug("Calculate max profit courier: {}, with order: {}",
            courier.getId(), addedOrder.getId());
        ResultFindMaxProfit ans = new ResultFindMaxProfit(-1, 0, 0);
        if(!courier.getSupportedTypeOrders().contains(addedOrder.getTypeOrder()))
        {
            log.debug("Order: {}, type not supported by courier: {}",
                addedOrder.getId(), courier.getId());
            return ans;
        }

        double currentMaxProfit = sumDeliveryCost(courierSchedule) - computeTotalOperationTime(courier, courierSchedule) * courier.getCost();
        int n = courierSchedule.size();
        log.debug("Max profit: {}, courier: {}, before adding order: {}", 
            currentMaxProfit, courier.getId(), addedOrder.getId());

        for(int pickupIndex = 0; pickupIndex <= n; ++pickupIndex) {
            for(int deliveryIndex = pickupIndex + 1; deliveryIndex <= n + 1; ++deliveryIndex)
            {
                double profitWithOrder = computeScheduleProfit(courier, buildSchedule(courier, addedOrder, courierSchedule, pickupIndex, deliveryIndex));
                log.debug("Max profit: {}, courier: {}, after add order: {} (pickupIndex: {}, deliveryIndex: {})", 
                    profitWithOrder, courier.getId(), addedOrder.getId(), pickupIndex, deliveryIndex);

                if(currentMaxProfit <= profitWithOrder)
                {
                    ans.setMaxProfit(profitWithOrder);
                    ans.setPickupIndex(pickupIndex);
                    ans.setDeliveryIndex(deliveryIndex);
                    currentMaxProfit = profitWithOrder;
                }
            }
        }
        log.debug("Max profit: {}, for couirer: {}, with added order: {}",
            ans.getMaxProfit(), courier.getId(), addedOrder.getId());
        return ans;
    }

    public long computeTotalOperationTime(Courier courier, List<Schedule> schedule)
    {
        long ans = 0;
        for(Schedule write : schedule)
        {
            ans += write.getPeriodOperation();
            if(write.getTypeOperation().equals(TypeOperation.DELIVERY)
                    && write.getDeliveryOrder().getDeliveryPeriod() < ans) 
            {
                log.debug("Courier: {} doesn't have time with schedule", courier.getId());
                return -1;
            }
        }
        log.debug("Courier: {} have time with schedule", courier.getId());
        return ans;
    }

    public boolean isLoadFeasible(Courier courier, List<Schedule> schedule)
    {
        int currentLoad = 0;
        for(Schedule write : schedule)
        {
            if(write.getTypeOperation().equals(TypeOperation.PICKUP)) {
                currentLoad += write.getDeliveryOrder().getWeight();
            } else if(write.getTypeOperation().equals(TypeOperation.DELIVERY)) {
                currentLoad -= write.getDeliveryOrder().getWeight();
            }

            if(currentLoad > courier.getLoadCapacity())
            {
                log.debug("Courier: {} overloaded with schedule", courier.getId());
                return false;
            }
        }
        log.debug("Courier: {} not overloaded with schedule", courier.getId());
        return true;
    }
    
    public double computeScheduleProfit(Courier courier, List<Schedule> schedule)
    {
        long allTimeToOrders = computeTotalOperationTime(courier, schedule);
        if(!isLoadFeasible(courier, schedule) || allTimeToOrders < 0)
            return -1;
        
        return sumDeliveryCost(schedule) - allTimeToOrders * courier.getCost();
    }

    public double sumDeliveryCost(List<Schedule> schedule)
    {
        double totalCost = 0;
        for(Schedule write : schedule)
        {
            if(write.getTypeOperation().equals(TypeOperation.DELIVERY))
                totalCost += write.getDeliveryOrder().getCost();
        }
        log.trace("Total delivery cost computed: {}", totalCost);
        return totalCost;
    }

    @Data
    @AllArgsConstructor
    public static class ResultFindMaxProfit {
        private double maxProfit;
        private int pickupIndex;
        private int deliveryIndex;
    }

    @Data
    @AllArgsConstructor
    public static class ResultRebuildSchedule {
        private List<Schedule> newSchedule;
        private List<DeliveryOrder> refusedOrders;
    }
}
