package com.uia.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.uia.delivery.entity.Schedule;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.service.ScheduleService;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTests 
{
    private MockMvc mockMvc;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleController scheduleController;

    private Schedule schedule;
    private Long testId = 3L;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders
                .standaloneSetup(scheduleController)
                .setControllerAdvice(new AdviceExceptionHandler())
            .build();

        schedule = Schedule.builder().id(testId).build();

    }

    @Test
    void getScheduleById_ReturnsSchedule() throws Exception
    {
        when(scheduleService.getScheduleById(testId)).thenReturn(schedule);

        mockMvc.perform(get("/api/schedule/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId));
    }

    @Test
    void getScheduleById_ThrowsNotFoundException() throws Exception
    {
        when(scheduleService.getScheduleById(testId + 1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/api/schedule/4"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSchedulesByParams_ReturnsPageSchedules() throws Exception
    {
        List<Schedule> schedules = List.of(
            Schedule.builder().id(1L).index(6L).periodOperation(16L).build(),
            Schedule.builder().id(2L).index(7L).periodOperation(17L).build()
        );
        Page<Schedule> schedulePage = new PageImpl<>(
            new ArrayList<>(schedules), 
            PageRequest.of(0, 10), 
            schedules.size()
        );

        when(scheduleService.getSchedulesByParams(any(), any())).thenReturn(schedulePage);

        mockMvc.perform(get("/api/schedule")
                .param("minIndex", "5")
                .param("maxIndex", "20")
                .param("minPeriodOperation", "15")
                .param("maxPeriodOperation", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].index").value(6))
                .andExpect(jsonPath("$.content[0].periodOperation").value(16))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].index").value(7))
                .andExpect(jsonPath("$.content[1].periodOperation").value(17));
    }

    @Test
    void getSchedulesByParams_ReturnsEmptyPage() throws Exception
    {
        Page<Schedule> schedulePage = new PageImpl<>(
            new ArrayList<>(), 
            PageRequest.of(0, 10), 
            0
        );

        when(scheduleService.getSchedulesByParams(any(), any())).thenReturn(schedulePage);

        mockMvc.perform(get("/api/schedule")
                .param("minIndex", "5")
                .param("maxIndex", "20")
                .param("minPeriodOperation", "15")
                .param("maxPeriodOperation", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0));
    }
}
