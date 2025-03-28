package com.uia.delivery.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.uia.delivery.controller.filter.CourierFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.CourierRepository;
import com.uia.delivery.repository.specification.CourierSpecification;

@Service
public class CourierService 
{
    private final CourierRepository courierRepository;
    private final TypeOrderService typeOrderService;

    public CourierService(
            CourierRepository courierRepository,
            TypeOrderService typeOrderService
    ) {
        this.courierRepository = courierRepository;
        this.typeOrderService = typeOrderService;
    }

    public Courier createCourier(Courier createForm)
    {
        createForm.setSupportedTypeOrders(typeOrderService.safetySaveTypes(createForm.getSupportedTypeOrders()));
        return courierRepository.save(createForm);
    }

    public Courier getCourierById(Long id)
    {
        return courierRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Courier"));
    }

    public Page<Courier> getCouriersByParams(SortParams sortParams, CourierFilter courierFilter)
    {
        Pageable page = PageRequest.of(
            sortParams.getPageNumber() - 1, 
            sortParams.getPageSize(),
            sortParams.getSortOrder().equalsIgnoreCase("asc")
                ? Sort.by(sortParams.getSortBy()).ascending()
                : Sort.by(sortParams.getSortBy()).descending());

        Specification<Courier> specification = CourierSpecification.dynamicFilter(courierFilter);
        return courierRepository.findAll(specification, page);
    }

    public Courier updateCourier(Long id, Courier updateForm)
    {
        Courier updateCourier = getCourierById(id);
        
        updateCourier.setFirstname(updateForm.getFirstname());
        updateCourier.setSurname(updateForm.getSurname());
        updateCourier.setLastname(updateForm.getLastname());
        updateCourier.setCost(updateForm.getCost());
        updateCourier.setSpeed(updateForm.getSpeed());
        updateCourier.setLoadCapacity(updateForm.getLoadCapacity());
        updateCourier.setSupportedTypeOrders(typeOrderService.safetySaveTypes(updateForm.getSupportedTypeOrders()));
        updateCourier.setPosition(updateCourier.getPosition());

        return courierRepository.save(updateCourier);
    }

    public void deleteCourier(Long id)
    {
        courierRepository.deleteById(id);
    }
}
