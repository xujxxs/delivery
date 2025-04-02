package com.uia.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.exception.ExportException;
import com.uia.delivery.exception.ImportException;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.exception.NullFieldException;
import com.uia.delivery.service.CourierService;

@ExtendWith(MockitoExtension.class)
class CourierControllerTests 
{
    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CourierService courierService;

    @InjectMocks
    private CourierController courierController;

    private Courier courier;
    private Long testId = 3L;
    private String testFirstname = "testFirstname";

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders
                .standaloneSetup(courierController)
                .setControllerAdvice(new AdviceExceptionHandler())
            .build();
        
        courier = new Courier();
        courier.setId(testId);
        courier.setFirstname(testFirstname);
    }

    @Test
    void createCourier_ReturnsCourier() throws Exception
    {
        when(courierService.createCourier(courier)).thenReturn(courier);

        mockMvc.perform(post("/api/courier")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courier)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstname").value(testFirstname));
    }

    @Test
    void createCourier_ThrowsNullFieldException() throws Exception
    {   
        when(courierService.createCourier(courier)).thenThrow(NullFieldException.class);

        mockMvc.perform(post("/api/courier")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courier)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCourierById_ReturnsCourier() throws Exception
    {
        when(courierService.getCourierById(testId)).thenReturn(courier);

        mockMvc.perform(get("/api/courier/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId))
                .andExpect(jsonPath("$.firstname").value(testFirstname));
    }

    @Test
    void getCourierById_ThrowsNotFoundException() throws Exception
    {
        when(courierService.getCourierById(testId + 1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/api/courier/4"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCouriersByParams_ReturnsPageCouriers() throws Exception
    {
        List<Courier> couriers = List.of(
            new Courier(1L, testFirstname + "1", null, null, 10.0, 20.0, 40000, null, null, null, null),
            new Courier(2L, testFirstname + "2", null, null, 13.0, 25.0, 40000, null, null, null, null)
        );
        Page<Courier> courierPage = new PageImpl<>(
            new ArrayList<>(couriers), 
            PageRequest.of(0, 10), 
            couriers.size()
        );

        when(courierService.getCouriersByParams(any(), any())).thenReturn(courierPage);

        mockMvc.perform(get("/api/courier")
                .param("minCost", "5")
                .param("maxCost", "20")
                .param("minSpeed", "15")
                .param("maxSpeed", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].firstname").value(testFirstname + "1"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].firstname").value(testFirstname + "2"));
    }

    @Test
    void getCouriersByParams_ReturnsEmptyPage() throws Exception
    {
        Page<Courier> courierPage = new PageImpl<>(
            new ArrayList<>(), 
            PageRequest.of(0, 10), 
            0
        );

        when(courierService.getCouriersByParams(any(), any())).thenReturn(courierPage);

        mockMvc.perform(get("/api/courier")
                .param("minCost", "5")
                .param("maxCost", "20")
                .param("minSpeed", "15")
                .param("maxSpeed", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0));
    }

    @Test
    void updateCourierById_ReturnsCourier() throws Exception
    {
        when(courierService.updateCourier(testId, courier)).thenReturn(courier);

        mockMvc.perform(put("/api/courier/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courier)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId))
                .andExpect(jsonPath("$.firstname").value(testFirstname));
    }

    @Test
    void updateCourierById_ThrowsNotFoundException() throws Exception
    {
        when(courierService.updateCourier(testId + 1L, courier)).thenThrow(NotFoundException.class);

        mockMvc.perform(put("/api/courier/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courier)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCourierById_ThrowsNullFieldException() throws Exception
    {
        when(courierService.updateCourier(testId, courier)).thenThrow(NullFieldException.class);

        mockMvc.perform(put("/api/courier/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courier)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCourierById_Success() throws Exception
    {
        doNothing().when(courierService).deleteCourier(testId);

        mockMvc.perform(delete("/api/courier/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void exportJSON_Success() throws Exception
    {
        byte[] testData = "testData".getBytes();
        when(courierService.exportAllCouriers()).thenReturn(testData);

        mockMvc.perform(get("/api/courier/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=couriers.json"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/json"))
                .andExpect(content().bytes(testData));

    }

    @Test
    void exportJSON_ThrowsExportException() throws Exception
    {
        when(courierService.exportAllCouriers()).thenThrow(new ExportException("Couriers"));

        mockMvc.perform(get("/api/courier/export"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void importJSON_Success() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile("file", "couriers.json", MediaType.APPLICATION_JSON_VALUE, "testData".getBytes());
        doNothing().when(courierService).importCouriers(any());

        mockMvc.perform(multipart("/api/courier/import").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("File successfully imported."));
    }

    @Test
    void importJSON_ThrowsImportException() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile("file", "couriers.json", MediaType.APPLICATION_JSON_VALUE, "testData".getBytes());
        doThrow(new ImportException("Couriers")).when(courierService).importCouriers(any());

        mockMvc.perform(multipart("/api/courier/import").file(file))
                .andExpect(status().isInternalServerError());
    }
}
