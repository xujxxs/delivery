package com.uia.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.uia.delivery.controller.filter.ScheduleFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.dto.ScheduleResponse;
import com.uia.delivery.entity.Schedule;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.ScheduleRepository;
import com.uia.delivery.service.algorithm.SchedulingAlgorithm;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTests 
{
    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SchedulingAlgorithm schedulingAlgorithm;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ScheduleService scheduleService;

    private Schedule schedule;
    private Long testId = 3L;

    @BeforeEach
    void setUp()
    {
        schedule = Schedule.builder().id(testId).build();
    }

    private List<Schedule> createSchedules()
    {
        return List.of(
            new Schedule(1L, null, null, null, null, null, null, null, null, null),
            new Schedule(2L, null, null, null, null, null, null, null, null, null)
        );
    }

    @Test
    void getScheduleById_ReturnsSchedule()
    {
        when(scheduleRepository.findById(testId)).thenReturn(Optional.of(schedule));

        Schedule result = scheduleService.getScheduleById(testId);

        assertNotNull(result);
        assertEquals(schedule.getId(), result.getId());
    }

    @Test
    void getScheduleById_ThrowsNotFoundException()
    {
        when(scheduleRepository.findById(testId)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> scheduleService.getScheduleById(testId));
    }

    @Test
    void getSchedulesByCourierId_ReturnsSchedules()
    {
        List<Schedule> schedules = createSchedules();
        when(scheduleRepository.findByCourierId(testId)).thenReturn(schedules);

        List<Schedule> result = scheduleService.getSchedulesByCourierId(testId);

        assertNotNull(result);
        assertEquals(schedules.size(), result.size());
    }

    @Test
    void saveShedules_ReturnsSchedules()
    {
        List<Schedule> schedules = createSchedules();
        when(scheduleRepository.saveAll(schedules)).thenAnswer(invocation -> invocation.getArgument(0));

        List<Schedule> result = scheduleService.saveShedules(schedules);

        assertNotNull(result);
        assertEquals(schedules.size(), result.size());
        assertEquals(0, result.get(0).getIndex());
        assertEquals(1, result.get(1).getIndex());
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
        ScheduleFilter scheduleFilter = ScheduleFilter.builder().build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("redactedAt").descending());
        List<Schedule> list = List.of(Schedule.builder().build(), Schedule.builder().build());
        Page<Schedule> page = new PageImpl<>(list, pageable, list.size());

        when(scheduleRepository.findAll(ArgumentMatchers.<Specification<Schedule>>any(), eq(pageable))).thenReturn(page);

        Page<Schedule> result = scheduleService.getSchedulesByParams(sortParams, scheduleFilter);

        assertEquals(2, result.getTotalElements());
        verify(scheduleRepository).findAll(ArgumentMatchers.<Specification<Schedule>>any(), eq(pageable));
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
        ScheduleFilter scheduleFilter = ScheduleFilter.builder().build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("redactedAt").descending());
        List<Schedule> list = List.of();
        Page<Schedule> page = new PageImpl<>(list, pageable, list.size());

        when(scheduleRepository.findAll(ArgumentMatchers.<Specification<Schedule>>any(), eq(pageable))).thenReturn(page);

        Page<Schedule> result = scheduleService.getSchedulesByParams(sortParams, scheduleFilter);

        assertEquals(0, result.getTotalElements());
        verify(scheduleRepository).findAll(ArgumentMatchers.<Specification<Schedule>>any(), eq(pageable));
    }

    @Test
    void receiveUpdatedSchedule_Success()
    {
        List<Schedule> schedules = createSchedules();
        when(scheduleRepository.findByCourierId(testId)).thenReturn(schedules);

        
        scheduleService.receiveUpdatedSchedule(testId);

        verify(messagingTemplate, times(1))
            .convertAndSend(eq("/topic/schedules"), eq(new ScheduleResponse(testId, schedules)));
    }
}
