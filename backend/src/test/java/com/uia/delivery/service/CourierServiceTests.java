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
import com.uia.delivery.controller.filter.CourierFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.dto.CouriersJsonResponse;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.exception.ExportException;
import com.uia.delivery.exception.ImportException;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.CourierRepository;

import akka.actor.ActorRef;

@ExtendWith(MockitoExtension.class)
class CourierServiceTests 
{
    @Mock
    private ActorRef dispatcherManager;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private TypeOrderService typeOrderService;

    @Mock
    private ObjectMapper objectMapper;

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

    private List<Courier> createListCouriers()
    {
        return List.of(
            new Courier(1L, testFirstname + "1", null, null, 10.0, 20.0, 40000, null, null, null, null),
            new Courier(2L, testFirstname + "2", null, null, 13.0, 25.0, 40000, null, null, null, null)
        );
    }

    @Test
    void createCourier_ReturnsCourier()
    {
        doNothing().when(dispatcherManager).tell(any(), any());
        when(typeOrderService.safetySaveTypes(any())).thenReturn(courier.getSupportedTypeOrders());
        when(courierRepository.save(courier)).thenReturn(courier);

        Courier result = courierService.createCourier(courier);

        assertNotNull(result);
        assertEquals(courier.getFirstname(), result.getFirstname());
        verify(dispatcherManager).tell(new DispatcherMessage.CreateCourier(result), ActorRef.noSender());
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
        List<Courier> list = createListCouriers();
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

        doNothing().when(dispatcherManager).tell(any(), any());
        when(courierRepository.findById(testId)).thenReturn(Optional.of(courier));
        when(typeOrderService.safetySaveTypes(any())).thenReturn(courier.getSupportedTypeOrders());
        when(courierRepository.save(any(Courier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Courier result = courierService.updateCourier(testId, formCourier);

        assertNotNull(result);
        assertEquals(formCourier.getId(), result.getId());
        assertEquals(formCourier.getFirstname(), result.getFirstname());
        verify(dispatcherManager).tell(new DispatcherMessage.UpdateCourier(result), ActorRef.noSender());
        verify(courierRepository).save(courier);
    }

    @Test
    void updateCourier_ThrowsNotFoundException()
    {
        when(courierRepository.findById(testId)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> courierService.updateCourier(testId, null));
    }

    @Test
    void testDeleteCourier_Success() 
    {
        doNothing().when(dispatcherManager).tell(any(), any());
        
        courierService.deleteCourier(testId);
        verify(dispatcherManager).tell(new DispatcherMessage.DeleteCourier(testId), ActorRef.noSender());
        verify(courierRepository).deleteById(testId);
    }

    @Test
    void exportJSON_ReturnsByteArray() throws Exception
    {
        List<Courier> couriers = createListCouriers();
        when(courierRepository.findAll()).thenReturn(couriers);

        doAnswer(invocation -> {
            ByteArrayOutputStream os = invocation.getArgument(0);
            new ObjectMapper().writeValue(os, new CouriersJsonResponse(couriers));
            return null;
        }).when(objectMapper).writeValue(any(ByteArrayOutputStream.class), any(CouriersJsonResponse.class));

        byte[] result = courierService.exportAllCouriers();

        assertNotNull(result);
        String json = new String(result);
        assertTrue(json.contains(testFirstname + "1"));
        assertTrue(json.contains(testFirstname + "2"));
    }

    @Test
    void exportJSON_ThrowsExportException() throws Exception
    {
        List<Courier> couriers = createListCouriers();
        when(courierRepository.findAll()).thenReturn(couriers);
        doThrow(new IOException("testException"))
                .when(objectMapper).writeValue(any(ByteArrayOutputStream.class), any(CouriersJsonResponse.class));

        assertThrows(ExportException.class, () -> courierService.exportAllCouriers());
    }

    @Test
    void importJSON_Success() throws Exception
    {
        List<Courier> couriers = createListCouriers();
        CouriersJsonResponse response = new CouriersJsonResponse(couriers);
        byte[] jsonData = new ObjectMapper().writeValueAsBytes(response);
        MultipartFile file = new MockMultipartFile("file", "couriers.json", "application/json", jsonData);

        doNothing().when(dispatcherManager).tell(any(), any());
        when(typeOrderService.safetySaveTypes(any())).thenReturn(null);
        when(courierRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(objectMapper.readValue(any(InputStream.class), eq(CouriersJsonResponse.class))).thenReturn(response);

        courierService.importCouriers(file);

        verify(dispatcherManager, times(couriers.size())).tell(any(DispatcherMessage.CreateCourier.class), eq(ActorRef.noSender()));
        verify(courierRepository, times(couriers.size())).save(any(Courier.class));
    }

    @Test
    void importJSON_ThrowsImportException() throws Exception
    {
        MultipartFile file = new MockMultipartFile("file", "couriers.json", "application/json", new byte[]{});

        doThrow(new IOException("testException"))
            .when(objectMapper).readValue(any(InputStream.class), eq(CouriersJsonResponse.class));

        assertThrows(ImportException.class, () -> courierService.importCouriers(file));
    }
}
