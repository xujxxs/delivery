package com.uia.delivery.akka;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.uia.delivery.akka.message.DispatcherMessage;
import com.uia.delivery.akka.query.CourierQuery;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.repository.CourierRepository;
import com.uia.delivery.repository.DeliveryOrderRepository;
import com.uia.delivery.service.ScheduleService;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispatcherManager extends AbstractActor
{
    private final Map<Long, ActorRef> couriersRef = new HashMap<>();
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final ScheduleService scheduleService;

    public DispatcherManager(
            CourierRepository courierRepository,
            DeliveryOrderRepository deliveryOrderRepository,
            ScheduleService scheduleService
    ) {
        this.deliveryOrderRepository = deliveryOrderRepository;
        this.scheduleService = scheduleService;

        courierRepository.findAll().forEach(courier -> {
            log.info("Init aktor courier: {}", courier.getId());
            couriersRef.put(
                courier.getId(),
                getContext().actorOf(
                    CourierActor.props(courier, scheduleService),
                    String.format("courier-%d", courier.getId()))
            );
        });
    }

    public static Props props(
        CourierRepository courierRepository,
        DeliveryOrderRepository deliveryOrderRepository,
        ScheduleService scheduleService
    ) {
        return Props.create(DispatcherManager.class, () -> new DispatcherManager(courierRepository, deliveryOrderRepository, scheduleService));
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(DispatcherMessage.CreateCourier.class, this::handleCreateCourier)
                .match(DispatcherMessage.UpdateCourier.class, this::handleUpdateCourier)
                .match(DispatcherMessage.DeleteCourier.class, this::handleDeleteCourier)
                .match(DispatcherMessage.CreateOrder.class, this::handleAddOrder)
                .match(DispatcherMessage.UpdateOrder.class, this::handleUpdateOrder)
                .match(DispatcherMessage.RefindOrder.class, this::handleRefindOrders)
                .match(DispatcherMessage.DeleteOrder.class, this::handleDeleteOrder)
            .build();
    }
    
    private void handleCreateCourier(DispatcherMessage.CreateCourier msg)
    {
        long courierId = msg.getCourier().getId();
        log.debug("Create actor courier: {}", courierId);
        if(couriersRef.containsKey(courierId))
        {
            log.warn("Actor courier: {}, not created. By: actor already exist in Map<ActorRef> couriersRef.", courierId);
            return;
        } 
        
        log.debug("Courier: {}, not exist in Map<ActorRef> couriersRef. Сreating courier actor", courierId);
        ActorRef courierRef = getContext().actorOf(
            CourierActor.props(msg.getCourier(), scheduleService),
            String.format("courier-%d", courierId));
        couriersRef.put(courierId, courierRef);

        log.info("Aktor courier: {} created.", courierId);
        searchOrders(courierRef);
    }

    private void handleUpdateCourier(DispatcherMessage.UpdateCourier msg)
    {
        long courierId = msg.getCourier().getId();
        log.debug("Update actor courier: {}", courierId);
        if(!couriersRef.containsKey(courierId))
        {
            log.warn("Courier: {}, not updated. By: not found in Map<ActorRef> couriersRef.", courierId);
            return;
        }
        couriersRef.get(courierId).tell(msg, getSender());
    }

    private void handleDeleteCourier(DispatcherMessage.DeleteCourier msg)
    {
        long courierId = msg.getCourierId();
        log.debug("Delete actor courier: {}", courierId);
        ActorRef courierRef = couriersRef.remove(courierId);

        if(courierRef == null)
        {
            log.warn("Actor courier: {}, not deleted. By: not found in Map<ActorRef> couriersRef", courierId);
            return;
        } 
        
        log.debug("Actor courier: {}, removed from Map<ActorRef> couriersRef", courierId);
        List<DeliveryOrder> assignedOrders = CourierQuery.fetchAssignedOrderIds(courierRef).join().stream()
                .map(orderId -> unbindAndSaveOrderById(orderId, courierId))
                .filter(Optional::isPresent)
                .map(Optional::get)
            .toList();
        
        if(assignedOrders.isEmpty())
            log.debug("Courier: {} has no assigned orders", courierId);

        getContext().stop(courierRef);
        log.info("Aktor courier: {} deleted", courierId);

        assignedOrders.forEach(this::searchCourier);
    }

    private void handleAddOrder(DispatcherMessage.CreateOrder msg) {
        searchCourier(msg.getOrder());
    }

    private void handleUpdateOrder(DispatcherMessage.UpdateOrder msg)
    {
        DeliveryOrder order = msg.getOrder();
        log.debug("Update order: {}", order.getId());
        if(msg.getOrder().getAssignedCourier() == null)
        {
            log.debug("Order: {}, not binded with courier. Update ended", order.getId());
            return;
        }
        long assignedCourierId = order.getAssignedCourier().getId();
        if(!couriersRef.containsKey(assignedCourierId))
        {
            log.error("Courier: {}, binded with order: {}. By: not found in Map<ActorRef> couriersRef",
                assignedCourierId, order.getId());
            return;
        }
        couriersRef.get(assignedCourierId).tell(msg, getSelf());
    }

    private void handleRefindOrders(DispatcherMessage.RefindOrder msg)
    {
        for(DeliveryOrder order : msg.getOrders()) 
        {
            unbindAndSaveOrderById(order.getId(), msg.getCourierId())
                .ifPresent(this::searchCourier);
        }
    }

    private void handleDeleteOrder(DispatcherMessage.DeleteOrder msg)
    {

        log.debug("Delete order: {}", msg.getOrderId());
        Optional<DeliveryOrder> opOrder = deliveryOrderRepository.findById(msg.getOrderId());

        if(!opOrder.isPresent()) 
        {
            log.warn("Order: {}, not deleted. By: not found in database", msg.getOrderId());
            return;
        }
        DeliveryOrder order = opOrder.get();
        deliveryOrderRepository.delete(order);
        log.debug("Order: {}, removed from database", msg.getOrderId());

        if(order.getAssignedCourier() != null) 
        {
            ActorRef assignedCourierRef = couriersRef.get(order.getAssignedCourier().getId());
            log.debug("Order: {}, binded with courier: {}", 
                msg.getOrderId(), order.getAssignedCourier().getId());
            assignedCourierRef.tell(msg, getSelf());
            searchOrders(assignedCourierRef);
        }
        else {
            log.debug("Order: {}, not binded with courier", msg.getOrderId());
        }
        log.info("Order: {} deleted", msg.getOrderId());
    }

    private Optional<DeliveryOrder> unbindAndSaveOrderById(Long orderId, Long courierId) {
        Optional<DeliveryOrder> opOrder = deliveryOrderRepository.findById(orderId);
        if (!opOrder.isPresent()) {
            log.warn("Order id: {}, getted from courier: {}, not found", orderId, courierId);
            return Optional.empty();
        }
        DeliveryOrder order = opOrder.get();
        order.setAssignedCourier(null);
        return Optional.of(deliveryOrderRepository.save(order));
    }

    private void searchCourier(DeliveryOrder order)
    {
        log.debug("Searching couriers with positive profit for order: {}", order.getId());

        List<ActorRef> bestCouriers = couriersRef.values()
            .parallelStream()
                .map(courierRef -> new AbstractMap.SimpleEntry<>(courierRef, CourierQuery.computeMaxProfitForOrder(courierRef, order).join()))
                .filter(entity -> entity.getValue() >= 0)
                .sorted(Comparator.comparingDouble(entity -> -entity.getValue()))
                .map(Map.Entry::getKey)
            .toList();
        
        if(bestCouriers.isEmpty())
        {
            log.info("Not found couriers with positive profit for order: {}", order.getId());
            return;
        }

        for(ActorRef bestCourier : bestCouriers) {
            if(tryBind(bestCourier, order))
                return;
        }
        log.warn("Order: {}, not binded with no one couriers. Proposed: {}, couriers", 
            order.getId(), bestCouriers.size());
    }

    private void searchOrders(ActorRef courierRef)
    {
        Long courierId = CourierQuery.fetchCourier(courierRef).join().getId();
        log.debug("Searching orders with positive profit for courier: {}", courierId);
        List<DeliveryOrder> freeOrders = deliveryOrderRepository.findByAssignedCourierIsNull();
        DeliveryOrder bindedOrder = null;

        do {
            bindedOrder = addBestOrder(
                freeOrders.parallelStream()
                        .map(order -> new AbstractMap.SimpleEntry<>(order, CourierQuery.computeMaxProfitForOrder(courierRef, order).join()))
                        .filter(entity -> entity.getValue() >= 0)
                        .sorted(Comparator.comparingDouble(entity -> -entity.getValue()))
                        .map(Map.Entry::getKey)
                    .toList(),
                courierRef,
                courierId);
                
            if(bindedOrder != null)
                freeOrders.remove(bindedOrder);

        } while (bindedOrder != null);
    }

    private DeliveryOrder addBestOrder(List<DeliveryOrder> listBestOrders, ActorRef courierRef, Long courierId)
    {
        if(listBestOrders.isEmpty())
            return null;

        for(DeliveryOrder bestOrder : listBestOrders){
            if(tryBind(courierRef, bestOrder))
                return bestOrder;
        }
        log.warn("Courier: {}, not binded with no one orders. Proposed: {}, orders", 
            courierId, listBestOrders.size());
        return null;
    }

    private boolean tryBind(ActorRef courierRef, DeliveryOrder order)
    {
        DispatcherMessage.BindRequest bind = new DispatcherMessage.BindRequest(courierRef, order);
        if(Boolean.TRUE.equals(CourierQuery.tryAddOrderToCourier(courierRef, bind).join()))
        {
            order.setAssignedCourier(CourierQuery.fetchCourier(courierRef).join());
            deliveryOrderRepository.save(order);
            return true;
        }
        return false;
    }
}
