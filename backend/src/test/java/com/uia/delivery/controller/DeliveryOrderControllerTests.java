package com.uia.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.exception.NullFieldException;
import com.uia.delivery.service.DeliveryOrderService;

@ExtendWith(MockitoExtension.class)
class DeliveryOrderControllerTests
{
    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DeliveryOrderService deliveryOrderService;

    @InjectMocks
    private DeliveryOrderController deliveryOrderController;

    private DeliveryOrder order;
    private Long testId = 3L;
    private String testName = "testName";

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders
                .standaloneSetup(deliveryOrderController)
                .setControllerAdvice(new AdviceExceptionHandler())
            .build();

        order = new DeliveryOrder();
        order.setId(testId);
        order.setName(testName);
    }

    @Test
    void createOrder_ReturnsOrder() throws Exception
    {
        when(deliveryOrderService.createOrder(order)).thenReturn(order);

        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(testName));
    }

    @Test
    void createOrder_ThrowsNullFieldException() throws Exception
    {   
        when(deliveryOrderService.createOrder(order)).thenThrow(NullFieldException.class);

        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderById_ReturnsOrder() throws Exception
    {
        when(deliveryOrderService.getOrderById(testId)).thenReturn(order);

        mockMvc.perform(get("/api/order/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId))
                .andExpect(jsonPath("$.name").value(testName));
    }

    @Test
    void getOrderById_ThrowsNotFoundException() throws Exception
    {
        when(deliveryOrderService.getOrderById(testId + 1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/api/order/4"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByParams_ReturnsPageOrders() throws Exception
    {
        List<DeliveryOrder> orders = List.of(
            new DeliveryOrder(1L, testName + "1", 6, 16.0, null, null, null, null, null, null),
            new DeliveryOrder(2L, testName + "2", 7, 17.0, null, null, null, null, null, null)
        );
        Page<DeliveryOrder> orderPage = new PageImpl<>(
            new ArrayList<>(orders), 
            PageRequest.of(0, 10), 
            orders.size()
        );

        when(deliveryOrderService.getOrdersByParams(any(), any())).thenReturn(orderPage);

        mockMvc.perform(get("/api/order")
                .param("minWeight", "5")
                .param("maxWeight", "20")
                .param("minCost", "15")
                .param("maxCost", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value(testName + "1"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].name").value(testName + "2"));
    }

    @Test
    void getOrdersByParams_ReturnsEmptyPage() throws Exception
    {
        Page<DeliveryOrder> orderPage = new PageImpl<>(
            new ArrayList<>(), 
            PageRequest.of(0, 10), 
            0
        );

        when(deliveryOrderService.getOrdersByParams(any(), any())).thenReturn(orderPage);

        mockMvc.perform(get("/api/order")
                .param("minWeight", "5")
                .param("maxWeight", "20")
                .param("minCost", "15")
                .param("maxCost", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0));
    }

    @Test
    void updateOrderById_ReturnsOrder() throws Exception
    {
        when(deliveryOrderService.updateOrder(testId, order)).thenReturn(order);

        mockMvc.perform(put("/api/order/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId))
                .andExpect(jsonPath("$.name").value(testName));
    }

    @Test
    void updateOrderById_ThrowsNotFoundException() throws Exception
    {
        when(deliveryOrderService.updateOrder(testId + 1L, order)).thenThrow(NotFoundException.class);

        mockMvc.perform(put("/api/order/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateOrderById_ThrowsNullFieldException() throws Exception
    {
        when(deliveryOrderService.updateOrder(testId, order)).thenThrow(NullFieldException.class);

        mockMvc.perform(put("/api/order/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteOrderById_Success() throws Exception
    {
        doNothing().when(deliveryOrderService).deleteOrder(testId);

        mockMvc.perform(delete("/api/order/3"))
                .andExpect(status().isNoContent());
    }
}
