package com.uia.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.uia.delivery.controller.filter.OrderFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.DeliveryOrderRepository;

@ExtendWith(MockitoExtension.class)
class DeliveryOrderServiceTests 
{
    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    @Mock
    private TypeOrderService typeOrderService;
    
    @InjectMocks
    private DeliveryOrderService deliveryOrderService;

    private DeliveryOrder order;
    private Long testId = 3L;
    private String testName = "testName";

    @BeforeEach
    void setUp()
    {
        order = new DeliveryOrder();
        order.setId(testId);
        order.setName(testName);
    }

    @Test
    void createOrder_ReturnsDeliveryOrder()
    {
        when(typeOrderService.safetySaveType(any())).thenReturn(order.getTypeOrder());
        when(deliveryOrderRepository.save(order)).thenReturn(order);

        DeliveryOrder result = deliveryOrderService.createOrder(order);

        assertNotNull(result);
        assertEquals(order.getName(), result.getName());
        verify(typeOrderService).safetySaveType(any());
    }

    @Test
    void getOrderById_ReturnsDeliveryOrder()
    {
        when(deliveryOrderRepository.findById(testId)).thenReturn(Optional.of(order));

        DeliveryOrder result = deliveryOrderService.getOrderById(testId);

        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
        assertEquals(order.getName(), result.getName());
    }

    @Test
    void getOrderById_ThrowsNotFoundException()
    {
        when(deliveryOrderRepository.findById(testId)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> deliveryOrderService.getOrderById(testId));
    }

    @Test
    void getOrdersByParams_ReturnsDeliveryOrdersPage()
    {
        SortParams sortParams = SortParams.builder()
                .pageNumber(1)
                .pageSize(10)
                .sortBy("redactedAt")
                .sortOrder("desc")
            .build();
        OrderFilter orderFilter = OrderFilter.builder().name(testName).build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("redactedAt").descending());
        List<DeliveryOrder> list = List.of(new DeliveryOrder(), new DeliveryOrder());
        Page<DeliveryOrder> page = new PageImpl<>(list, pageable, list.size());

        when(deliveryOrderRepository.findAll(ArgumentMatchers.<Specification<DeliveryOrder>>any(), eq(pageable))).thenReturn(page);

        Page<DeliveryOrder> result = deliveryOrderService.getOrdersByParams(sortParams, orderFilter);

        assertEquals(2, result.getTotalElements());
        verify(deliveryOrderRepository).findAll(ArgumentMatchers.<Specification<DeliveryOrder>>any(), eq(pageable));
    }

    @Test
    void getOrdersByParams_ReturnsEmptyPage()
    {
        SortParams sortParams = SortParams.builder()
                .pageNumber(1)
                .pageSize(10)
                .sortBy("redactedAt")
                .sortOrder("desc")
            .build();
        OrderFilter orderFilter = OrderFilter.builder().name(testName).build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("redactedAt").descending());
        List<DeliveryOrder> list = List.of();
        Page<DeliveryOrder> page = new PageImpl<>(list, pageable, list.size());

        when(deliveryOrderRepository.findAll(ArgumentMatchers.<Specification<DeliveryOrder>>any(), eq(pageable))).thenReturn(page);

        Page<DeliveryOrder> result = deliveryOrderService.getOrdersByParams(sortParams, orderFilter);

        assertEquals(0, result.getTotalElements());
        verify(deliveryOrderRepository).findAll(ArgumentMatchers.<Specification<DeliveryOrder>>any(), eq(pageable));
    }

    @Test
    void updateOrder_ReturnsDeliveryOrder()
    {
        DeliveryOrder formOrder = order;
        formOrder.setName(testName + "test");

        when(deliveryOrderRepository.findById(testId)).thenReturn(Optional.of(order));
        when(typeOrderService.safetySaveType(any())).thenReturn(order.getTypeOrder());
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryOrder result = deliveryOrderService.updateOrder(testId, formOrder);

        assertNotNull(result);
        assertEquals(formOrder.getId(), result.getId());
        assertEquals(formOrder.getName(), result.getName());
        verify(deliveryOrderRepository).save(order);
    }

    @Test
    void updateOrder_ThrowsNotFoundException()
    {
        when(deliveryOrderRepository.findById(testId)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> deliveryOrderService.updateOrder(testId, null));
    }

    @Test
    void deleteOrder_Success() {
        deliveryOrderService.deleteOrder(testId);
        verify(deliveryOrderRepository).deleteById(testId);
    }
}
