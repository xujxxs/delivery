package com.uia.delivery.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.uia.delivery.controller.filter.OrderFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.DeliveryOrderRepository;
import com.uia.delivery.repository.specification.DeliveryOrderSpecification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeliveryOrderService 
{
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final TypeOrderService typeOrderService;

    public DeliveryOrderService(
            DeliveryOrderRepository deliveryOrderRepository,
            TypeOrderService typeOrderService
    ) {
        this.deliveryOrderRepository = deliveryOrderRepository;
        this.typeOrderService = typeOrderService;
    }

    public DeliveryOrder createOrder(DeliveryOrder createForm)
    {
        createForm.setTypeOrder(typeOrderService.safetySaveType(createForm.getTypeOrder()));
        return deliveryOrderRepository.save(createForm);
    }

    public DeliveryOrder getOrderById(Long id)
    {
        return deliveryOrderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Order"));
    }

    public Page<DeliveryOrder> getOrdersByParams(SortParams sortParams, OrderFilter orderFilter)
    {
        Pageable page = PageRequest.of(
            sortParams.getPageNumber() - 1, 
            sortParams.getPageSize(),
            sortParams.getSortOrder().equalsIgnoreCase("asc")
                ? Sort.by(sortParams.getSortBy()).ascending()
                : Sort.by(sortParams.getSortBy()).descending());

        Specification<DeliveryOrder> specification = DeliveryOrderSpecification.dynamicFilter(orderFilter);
        return deliveryOrderRepository.findAll(specification, page);
    }

    public DeliveryOrder updateOrder(Long id, DeliveryOrder updateForm)
    {
        DeliveryOrder updateOrder = getOrderById(id);

        updateOrder.setName(updateForm.getName());
        updateOrder.setWeight(updateForm.getWeight());
        updateOrder.setCost(updateForm.getCost());
        updateOrder.setDeliveryPeriod(updateForm.getDeliveryPeriod());
        updateOrder.setPositionPickUp(updateForm.getPositionPickUp());
        updateOrder.setPositionDelivery(updateForm.getPositionDelivery());

        updateOrder.setTypeOrder(typeOrderService.safetySaveType(updateForm.getTypeOrder()));

        return deliveryOrderRepository.save(updateOrder);
    }

    public void deleteOrder(Long id)
    {
        deliveryOrderRepository.deleteById(id);
    }
}
