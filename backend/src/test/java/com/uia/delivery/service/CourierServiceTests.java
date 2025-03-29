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

import com.uia.delivery.controller.filter.CourierFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.CourierRepository;

@ExtendWith(MockitoExtension.class)
class CourierServiceTests 
{
    @Mock
    private CourierRepository courierRepository;

    @Mock
    private TypeOrderService typeOrderService;

    @InjectMocks
    private CourierService courierService;

    private Courier courier;
    private Long testId = 3L;
    private String testFirstname = "testFirstname";

    @BeforeEach
    void setUp()
    {
        courier = new Courier();
        courier.setId(testId);
        courier.setFirstname(testFirstname);
    }

    @Test
    void createCourier_ReturnsCourier()
    {
        when(typeOrderService.safetySaveTypes(any())).thenReturn(courier.getSupportedTypeOrders());
        when(courierRepository.save(courier)).thenReturn(courier);

        Courier result = courierService.createCourier(courier);

        assertNotNull(result);
        assertEquals(courier.getFirstname(), result.getFirstname());
        verify(typeOrderService).safetySaveTypes(any());
    }

    @Test
    void getCourierById_ReturnsCourier()
    {
        when(courierRepository.findById(testId)).thenReturn(Optional.of(courier));

        Courier result = courierService.getCourierById(testId);

        assertNotNull(result);
        assertEquals(courier.getId(), result.getId());
        assertEquals(courier.getFirstname(), result.getFirstname());
    }

    @Test
    void getCourierById_ThrowsNotFoundException()
    {
        when(courierRepository.findById(testId)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> courierService.getCourierById(testId));
    }

    @Test
    void getCouriersByParams_ReturnsCouriersPage()
    {
        SortParams sortParams = SortParams.builder()
                .pageNumber(1)
                .pageSize(10)
                .sortBy("redactedAt")
                .sortOrder("desc")
            .build();
        CourierFilter courierFilter = CourierFilter.builder().firstname(testFirstname).build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("redactedAt").descending());
        List<Courier> list = List.of(new Courier(), new Courier());
        Page<Courier> page = new PageImpl<>(list, pageable, list.size());

        when(courierRepository.findAll(ArgumentMatchers.<Specification<Courier>>any(), eq(pageable))).thenReturn(page);

        Page<Courier> result = courierService.getCouriersByParams(sortParams, courierFilter);

        assertEquals(2, result.getTotalElements());
        verify(courierRepository).findAll(ArgumentMatchers.<Specification<Courier>>any(), eq(pageable));
    }

    @Test
    void getCouriersByParams_ReturnsEmptyPage()
    {
        SortParams sortParams = SortParams.builder()
                .pageNumber(1)
                .pageSize(10)
                .sortBy("redactedAt")
                .sortOrder("desc")
            .build();
        CourierFilter courierFilter = CourierFilter.builder().firstname(testFirstname).build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("redactedAt").descending());
        List<Courier> list = List.of();
        Page<Courier> page = new PageImpl<>(list, pageable, list.size());

        when(courierRepository.findAll(ArgumentMatchers.<Specification<Courier>>any(), eq(pageable))).thenReturn(page);

        Page<Courier> result = courierService.getCouriersByParams(sortParams, courierFilter);

        assertEquals(0, result.getTotalElements());
        verify(courierRepository).findAll(ArgumentMatchers.<Specification<Courier>>any(), eq(pageable));
    }

    @Test
    void updateCourier_ReturnsCourier()
    {
        Courier formCourier = courier;
        formCourier.setFirstname(testFirstname + "test");

        when(courierRepository.findById(testId)).thenReturn(Optional.of(courier));
        when(typeOrderService.safetySaveTypes(any())).thenReturn(courier.getSupportedTypeOrders());
        when(courierRepository.save(any(Courier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Courier result = courierService.updateCourier(testId, formCourier);

        assertNotNull(result);
        assertEquals(formCourier.getId(), result.getId());
        assertEquals(formCourier.getFirstname(), result.getFirstname());
        verify(courierRepository).save(courier);
    }

    @Test
    void updateCourier_ThrowsNotFoundException()
    {
        when(courierRepository.findById(testId)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> courierService.updateCourier(testId, null));
    }

    @Test
    void testDeleteCourier_Success() {
        courierService.deleteCourier(testId);
        verify(courierRepository).deleteById(testId);
    }
}
