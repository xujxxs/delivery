package com.uia.delivery.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uia.delivery.controller.filter.CourierFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.service.CourierService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/api/courier")
public class CourierController 
{
    private final CourierService courierService;

    public CourierController(CourierService courierService)
    {
        this.courierService = courierService;
    }
    
    @PostMapping
    public ResponseEntity<Courier> createCourier(@RequestBody Courier createForm)
    {
        log.info("POST '/api/courier' | RequestBody: {}", createForm);
        Courier createdCourier = courierService.createCourier(createForm);
        log.debug("Created courier: {}", createdCourier);

        return ResponseEntity
                .created(URI.create("/api/courier/" + createdCourier.getId()))
                .body(createdCourier);
    }
    
    @GetMapping("/{courierId}")
    public ResponseEntity<Courier> getCourierById(@PathVariable Long courierId)
    {
        log.info("GET '/api/courier/{}'", courierId);
        Courier gettedCourier = courierService.getCourierById(courierId);
        log.debug("Getted courier: {}", gettedCourier);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gettedCourier);
    }
    
    @GetMapping
    public ResponseEntity<Page<Courier>> getCouriersByParams(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "redactedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String firstname,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String lastname,
            @RequestParam(required = false) Double minCost,
            @RequestParam(required = false) Double maxCost,
            @RequestParam(required = false) Double minSpeed,
            @RequestParam(required = false) Double maxSpeed,
            @RequestParam(required = false) Integer minLoadCapacity,
            @RequestParam(required = false) Integer maxLoadCapacity,
            @RequestParam(required = false) String supportedTypesOrder
    ) {
        SortParams sortParams = SortParams.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
            .build();
        
        CourierFilter courierFilter = CourierFilter.builder()
                .firstname(firstname)
                .surname(surname)
                .lastname(lastname)
                .minCost(minCost)
                .maxCost(maxCost)
                .minSpeed(minSpeed)
                .maxSpeed(maxSpeed)
                .minLoadCapacity(minLoadCapacity)
                .maxLoadCapacity(maxLoadCapacity)
                .supportedTypesOrder(supportedTypesOrder)
            .build();

        log.info("GET '/api/courier' | Sort params: {}, Courier filter: {}",
            sortParams, courierFilter);
        Page<Courier> pageCouriers = courierService.getCouriersByParams(sortParams, courierFilter);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pageCouriers);
    }
    
    @PutMapping("/{courierId}")
    public ResponseEntity<Courier> updateCourier(
            @PathVariable Long courierId,
            @RequestBody Courier updateForm
    ) {
        log.info("PUT '/api/courier/{}' | RequestBody: {}", 
            courierId, updateForm);
        Courier updatedCourier = courierService.updateCourier(courierId, updateForm);
        log.debug("Updated courier: {}", updatedCourier);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedCourier);
    }
    
    @DeleteMapping("/{courierId}")
    public ResponseEntity<Void> deleteCourier(@PathVariable Long courierId)
    {
        log.info("DELETE '/api/courier/{}'", courierId);
        courierService.deleteCourier(courierId);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportJSON()
    {
        log.info("GET '/api/courier/export'");
        byte[] exportFile = courierService.exportAllCouriers();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=couriers.json")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(exportFile);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importJSON(@RequestParam("file") MultipartFile file) 
    {
        log.info("POST '/api/courier/import'");
        courierService.importCouriers(file);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("File successfully imported.");
    }
}
