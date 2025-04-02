package com.uia.delivery.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uia.delivery.akka.message.DispatcherMessage;
import com.uia.delivery.controller.filter.OrderFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.dto.OrdersJsonResponse;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.exception.ExportException;
import com.uia.delivery.exception.ImportException;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.DeliveryOrderRepository;
import com.uia.delivery.repository.specification.DeliveryOrderSpecification;

import akka.actor.ActorRef;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeliveryOrderService 
{
    private final ActorRef dispatcherManager;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final TypeOrderService typeOrderService;
    private final ObjectMapper objectMapper;

    public DeliveryOrderService(
            ActorRef dispatcherManager,
            DeliveryOrderRepository deliveryOrderRepository,
            TypeOrderService typeOrderService,
            ObjectMapper objectMapper
    ) {
        this.dispatcherManager = dispatcherManager;
        this.deliveryOrderRepository = deliveryOrderRepository;
        this.typeOrderService = typeOrderService;
        this.objectMapper = objectMapper;
    }

    public DeliveryOrder createOrder(DeliveryOrder createForm)
    {
        log.debug("Creating order with data: {}", createForm);
        createForm.setTypeOrder(typeOrderService.safetySaveType(createForm.getTypeOrder()));
        createForm.setAssignedCourier(null);
        DeliveryOrder savedOrder = deliveryOrderRepository.save(createForm);
        log.info("Order: {}, created in database", savedOrder.getId());

        dispatcherManager.tell(new DispatcherMessage.CreateOrder(savedOrder), ActorRef.noSender());
        return savedOrder;
    }

    public DeliveryOrder getOrderById(Long id)
    {
        log.debug("Fetching order with id: {}", id);
        return deliveryOrderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Order"));
    }

    public Page<DeliveryOrder> getOrdersByParams(SortParams sortParams, OrderFilter orderFilter)
    {
        log.debug("Fetching orders with filter and sortParams");
        Pageable page = PageRequest.of(
            sortParams.getPageNumber() - 1, 
            sortParams.getPageSize(),
            sortParams.getSortOrder().equalsIgnoreCase("asc")
                ? Sort.by(sortParams.getSortBy()).ascending()
                : Sort.by(sortParams.getSortBy()).descending());

        Specification<DeliveryOrder> specification = DeliveryOrderSpecification.dynamicFilter(orderFilter);
        Page<DeliveryOrder> ans = deliveryOrderRepository.findAll(specification, page);

        log.info("Fetched {} orders by params", ans.getTotalElements());
        return ans;
    }

    public DeliveryOrder updateOrder(Long id, DeliveryOrder updateForm)
    {
        log.debug("Updating order with id: {}", id);
        DeliveryOrder updateOrder = getOrderById(id);

        updateOrder.setName(updateForm.getName());
        updateOrder.setWeight(updateForm.getWeight());
        updateOrder.setCost(updateForm.getCost());
        updateOrder.setDeliveryPeriod(updateForm.getDeliveryPeriod());
        updateOrder.setPositionPickUp(updateForm.getPositionPickUp());
        updateOrder.setPositionDelivery(updateForm.getPositionDelivery());

        updateOrder.setTypeOrder(typeOrderService.safetySaveType(updateForm.getTypeOrder()));

        DeliveryOrder savedOrder = deliveryOrderRepository.save(updateOrder);
        log.info("Order: {}, updated in database", savedOrder.getId());
        dispatcherManager.tell(new DispatcherMessage.UpdateOrder(savedOrder), ActorRef.noSender());

        return savedOrder;
    }

    public void deleteOrder(Long id)
    {
        log.debug("Deleting courier with id: {}", id);
        dispatcherManager.tell(new DispatcherMessage.DeleteOrder(id), ActorRef.noSender());
    }

    public byte[] exportAllOrders()
    {
        log.debug("Exporting all orders");
        List<DeliveryOrder> allOrders = deliveryOrderRepository.findAll();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            objectMapper.writeValue(outputStream, new OrdersJsonResponse(allOrders));
        } catch (IOException e) {
            log.error("Error export file. By: {}", e.getMessage());
            throw new ExportException("Orders");
        }
        log.info("Exported {} orders", allOrders.size());
        return outputStream.toByteArray();
    }

    public void importOrders(MultipartFile file)
    {
        log.debug("Importing orders from file: {}", file.getOriginalFilename());
        try {
            List<DeliveryOrder> orders = objectMapper.readValue(file.getInputStream(), OrdersJsonResponse.class).getOrders();
            log.debug("Orders to import: {}", orders);
    
            orders.forEach(order -> {
                order.setId(null);
                createOrder(order);
            });
            log.info("Successfully imported {} orders", orders.size());
        } catch (IOException e) {
            log.warn("Error import file. By: ", e.getMessage());
            throw new ImportException("Orders");
        }
    }
}
