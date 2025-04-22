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
import com.uia.delivery.controller.filter.CourierFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.dto.CouriersJsonResponse;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.exception.ExportException;
import com.uia.delivery.exception.ImportException;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.CourierRepository;
import com.uia.delivery.repository.specification.CourierSpecification;

import akka.actor.ActorRef;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CourierService 
{
    private final ActorRef dispatcherManager;
    private final CourierRepository courierRepository;
    private final TypeOrderService typeOrderService;
    private final ObjectMapper objectMapper;

    public CourierService(
            ActorRef dispatcherManager,
            CourierRepository courierRepository,
            TypeOrderService typeOrderService,
            ObjectMapper objectMapper
    ) {
        this.dispatcherManager = dispatcherManager;
        this.courierRepository = courierRepository;
        this.typeOrderService = typeOrderService;
        this.objectMapper = objectMapper;
    }

    public Courier createCourier(Courier createForm)
    {
        log.debug("Creating courier with data: {}", createForm);
        createForm.setSupportedTypeOrders(typeOrderService.safetySaveTypes(createForm.getSupportedTypeOrders()));
        Courier savedCourier = courierRepository.save(createForm);
        log.info("Courier: {}, created in database", savedCourier.getId());

        dispatcherManager.tell(new DispatcherMessage.CreateCourier(savedCourier), ActorRef.noSender());
        return savedCourier;
    }

    public Courier getCourierById(Long id)
    {
        log.debug("Fetching courier with id: {}", id);
        return courierRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Courier"));
    }

    public Page<Courier> getCouriersByParams(SortParams sortParams, CourierFilter courierFilter)
    {
        log.debug("Fetching couriers with filter and sortParams");
        Pageable page = PageRequest.of(
            sortParams.getPageNumber() - 1, 
            sortParams.getPageSize(),
            sortParams.getSortOrder().equalsIgnoreCase("asc")
                ? Sort.by(sortParams.getSortBy()).ascending()
                : Sort.by(sortParams.getSortBy()).descending());

        Specification<Courier> specification = CourierSpecification.dynamicFilter(courierFilter);
        Page<Courier> ans = courierRepository.findAll(specification, page);

        log.info("Fetched {} couriers by params", ans.getTotalElements());
        return ans;
    }

    public Courier updateCourier(Long id, Courier updateForm)
    {
        log.debug("Updating courier with id: {}", id);
        Courier updateCourier = getCourierById(id);
        
        updateCourier.setFirstname(updateForm.getFirstname());
        updateCourier.setSurname(updateForm.getSurname());
        updateCourier.setLastname(updateForm.getLastname());
        updateCourier.setCost(updateForm.getCost());
        updateCourier.setSpeed(updateForm.getSpeed());
        updateCourier.setLoadCapacity(updateForm.getLoadCapacity());
        updateCourier.setSupportedTypeOrders(typeOrderService.safetySaveTypes(updateForm.getSupportedTypeOrders()));
        updateCourier.setPosition(updateCourier.getPosition());

        Courier savedCourier = courierRepository.save(updateCourier);
        log.info("Courier: {}, updated in database", savedCourier.getId());
        dispatcherManager.tell(new DispatcherMessage.UpdateCourier(savedCourier), ActorRef.noSender());

        return savedCourier;
    }

    public void deleteCourier(Long id)
    {
        log.debug("Deleting courier with id: {}", id);
        dispatcherManager.tell(new DispatcherMessage.DeleteCourier(id), ActorRef.noSender());
        log.info("Request was sent to DispatcherManager to delete courier: {}", id);
    }

    public byte[] exportAllCouriers()
    {
        log.debug("Exporting all couriers");
        List<Courier> allCouriers = courierRepository.findAll();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            objectMapper.writeValue(outputStream, new CouriersJsonResponse(allCouriers));
        } catch (IOException e) {
            log.error("Error export file. By: {}", e.getMessage());
            throw new ExportException("Couriers");
        }
        log.info("Exported {} couriers", allCouriers.size());
        return outputStream.toByteArray();
    }

    public void importCouriers(MultipartFile file)
    {
        log.debug("Importing couriers from file: {}", file.getOriginalFilename());
        try {
            List<Courier> couriers = objectMapper.readValue(file.getInputStream(), CouriersJsonResponse.class).getCouriers();
            log.debug("Couriers to import: {}", couriers);
    
            couriers.forEach(courier -> {
                courier.setId(null);
                createCourier(courier);
            });
            log.info("Successfully imported {} couriers", couriers.size());
        } catch (IOException e) {
            log.warn("Error import file. By: ", e.getCause());
            throw new ImportException("Couriers");
        }
    }
}
