package com.uia.delivery.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uia.delivery.controller.filter.OrderFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.service.DeliveryOrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/order")
public class DeliveryOrderController 
{
    private final DeliveryOrderService deliveryOrderService;

    public DeliveryOrderController(DeliveryOrderService deliveryOrderService)
    {
        this.deliveryOrderService = deliveryOrderService;
    }
    
    @PostMapping
    public ResponseEntity<DeliveryOrder> createOrder(@RequestBody DeliveryOrder createForm)
    {
        log.info("POST '/api/order' | RequestBody: {}", createForm);
        DeliveryOrder createdOrder = deliveryOrderService.createOrder(createForm);
        log.debug("Created order: {}", createdOrder);

        return ResponseEntity
                .created(URI.create("/api/order/" + createdOrder.getId()))
                .body(createdOrder);
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<DeliveryOrder> getOrderById(@PathVariable Long orderId)
    {
        log.info("GET '/api/order/{}'", orderId);
        DeliveryOrder gettedOrder = deliveryOrderService.getOrderById(orderId);
        log.debug("Getted order: {}", gettedOrder);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gettedOrder);
    }
    
    @GetMapping
    public ResponseEntity<Page<DeliveryOrder>> getOrdersByParams(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "redactedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minWeight,
            @RequestParam(required = false) Integer maxWeight,
            @RequestParam(required = false) Double minCost,
            @RequestParam(required = false) Double maxCost,
            @RequestParam(required = false) Long minDeliveryPeriod,
            @RequestParam(required = false) Long maxDeliveryPeriod,
            @RequestParam(required = false) String typesOrder
    ) {
        SortParams sortParams = SortParams.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
            .build();
        
        OrderFilter orderFilter = OrderFilter.builder()
                .name(name)
                .minWeight(minWeight)
                .maxWeight(maxWeight)
                .minCost(minCost)
                .maxCost(maxCost)
                .minDeliveryPeriod(minDeliveryPeriod)
                .maxDeliveryPeriod(maxDeliveryPeriod)
                .typesOrder(typesOrder)
            .build();

        log.info("GET '/api/order' | Sort params: {}, Courier filter: {}",
            sortParams, orderFilter);
        Page<DeliveryOrder> pageOrders = deliveryOrderService.getOrdersByParams(sortParams, orderFilter);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pageOrders);
    }
    
    @PutMapping("/{orderId}")
    public ResponseEntity<DeliveryOrder> updateOrder(
            @PathVariable Long orderId,
            @RequestBody DeliveryOrder updateForm)
    {
        log.info("PUT '/api/order/{}' | RequestBody: {}", 
            orderId, updateForm);
        DeliveryOrder updatedOrder = deliveryOrderService.updateOrder(orderId, updateForm);
        log.debug("Updated order: {}", updatedOrder);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedOrder);
    }
    
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId)
    {
        log.info("DELETE '/api/order/{}'", orderId);
        deliveryOrderService.deleteOrder(orderId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportJSON()
    {
        log.info("GET '/api/order/export'");
        byte[] exportFile = deliveryOrderService.exportAllOrders();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.json")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(exportFile);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importJSON(@RequestParam("file") MultipartFile file) 
    {
        log.info("POST '/api/order/import'");
        deliveryOrderService.importOrders(file);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("File successfully imported.");
    }
}
