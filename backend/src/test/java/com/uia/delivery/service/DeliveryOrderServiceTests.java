package com.uia.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.mock.web.MockMultipartFile;
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

import akka.actor.ActorRef;

@ExtendWith(MockitoExtension.class)
class DeliveryOrderServiceTests 
{
    @Mock
    private ActorRef dispatcherManager;

    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    @Mock
    private TypeOrderService typeOrderService;

    @Mock
    private ObjectMapper objectMapper;
    
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

    private List<DeliveryOrder> createDeliveryOrders()
    {
        return List.of(
            new DeliveryOrder(1L, testName + "1", null, null, null, null, null, null, null, null, null),
            new DeliveryOrder(2L, testName + "2", null, null, null, null, null, null, null, null, null)
        );
    }

    @Test
    void createOrder_ReturnsDeliveryOrder()
    {
        doNothing().when(dispatcherManager).tell(any(), any());
        when(typeOrderService.safetySaveType(any())).thenReturn(order.getTypeOrder());
        when(deliveryOrderRepository.save(order)).thenReturn(order);

        DeliveryOrder result = deliveryOrderService.createOrder(order);

        assertNotNull(result);
        assertEquals(order.getName(), result.getName());
        verify(dispatcherManager).tell(new DispatcherMessage.CreateOrder(result), ActorRef.noSender());
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
        List<DeliveryOrder> list = createDeliveryOrders();
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

        doNothing().when(dispatcherManager).tell(any(), any());
        when(deliveryOrderRepository.findById(testId)).thenReturn(Optional.of(order));
        when(typeOrderService.safetySaveType(any())).thenReturn(order.getTypeOrder());
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryOrder result = deliveryOrderService.updateOrder(testId, formOrder);

        assertNotNull(result);
        assertEquals(formOrder.getId(), result.getId());
        assertEquals(formOrder.getName(), result.getName());
        verify(dispatcherManager).tell(new DispatcherMessage.UpdateOrder(result), ActorRef.noSender());
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
        doNothing().when(dispatcherManager).tell(any(), any());

        deliveryOrderService.deleteOrder(testId);
        verify(dispatcherManager).tell(new DispatcherMessage.DeleteOrder(testId), ActorRef.noSender());
    }

    @Test
    void exportJSON_ReturnsByteArray() throws Exception
    {
        List<DeliveryOrder> orders = createDeliveryOrders();
        when(deliveryOrderRepository.findAll()).thenReturn(orders);

        doAnswer(invocation -> {
            ByteArrayOutputStream os = invocation.getArgument(0);
            new ObjectMapper().writeValue(os, new OrdersJsonResponse(orders));
            return null;
        }).when(objectMapper).writeValue(any(ByteArrayOutputStream.class), any(OrdersJsonResponse.class));

        byte[] result = deliveryOrderService.exportAllOrders();

        assertNotNull(result);
        String json = new String(result);
        assertTrue(json.contains(testName + "1"));
        assertTrue(json.contains(testName + "2"));
    }

    @Test
    void exportJSON_ThrowsExportException() throws Exception
    {
        List<DeliveryOrder> orders = createDeliveryOrders();
        when(deliveryOrderRepository.findAll()).thenReturn(orders);
        doThrow(new IOException("testException"))
                .when(objectMapper).writeValue(any(ByteArrayOutputStream.class), any(OrdersJsonResponse.class));

        assertThrows(ExportException.class, () -> deliveryOrderService.exportAllOrders());
    }

    @Test
    void importJSON_Success() throws Exception
    {
        List<DeliveryOrder> orders = createDeliveryOrders();
        OrdersJsonResponse response = new OrdersJsonResponse(orders);
        byte[] jsonData = new ObjectMapper().writeValueAsBytes(response);
        MultipartFile file = new MockMultipartFile("file", "orders.json", "application/json", jsonData);

        doNothing().when(dispatcherManager).tell(any(), any());
        when(typeOrderService.safetySaveType(any())).thenReturn(null);
        when(deliveryOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(objectMapper.readValue(any(InputStream.class), eq(OrdersJsonResponse.class))).thenReturn(response);

        deliveryOrderService.importOrders(file);

        verify(dispatcherManager, times(orders.size())).tell(any(DispatcherMessage.CreateOrder.class), eq(ActorRef.noSender()));
        verify(deliveryOrderRepository, times(orders.size())).save(any(DeliveryOrder.class));
    }

    @Test
    void importJSON_ThrowsImportException() throws Exception
    {
        MultipartFile file = new MockMultipartFile("file", "orders.json", "application/json", new byte[]{});

        doThrow(new IOException("testException"))
            .when(objectMapper).readValue(any(InputStream.class), eq(OrdersJsonResponse.class));

        assertThrows(ImportException.class, () -> deliveryOrderService.importOrders(file));
    }
}
