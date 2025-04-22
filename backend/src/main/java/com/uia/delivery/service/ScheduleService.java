package com.uia.delivery.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uia.delivery.controller.filter.ScheduleFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.dto.ScheduleResponse;
import com.uia.delivery.dto.SchedulesJsonResponse;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.entity.Schedule;
import com.uia.delivery.exception.ExportException;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.ScheduleRepository;
import com.uia.delivery.repository.specification.ScheduleSpecification;
import com.uia.delivery.service.algorithm.SchedulingAlgorithm;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ScheduleService 
{
    private final ScheduleRepository scheduleRepository;
    private final SchedulingAlgorithm schedulingAlgorithm;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            SchedulingAlgorithm schedulingAlgorithm,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper
    ) {
        this.scheduleRepository = scheduleRepository;
        this.schedulingAlgorithm = schedulingAlgorithm;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public Schedule getScheduleById(Long id)
    {
        log.debug("Fetching schedule with id: {}", id);
        return scheduleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Schedule"));
    }

    public List<Schedule> getSchedulesByCourierId(Long courierId)
    {
        log.debug("Fetching schedules by courierId: {}", courierId);
        List<Schedule> ans = scheduleRepository.findByCourierId(courierId);
        log.debug("Fetched {} schedules for courier: {}", ans.size(), courierId);
        return ans;
    }

    public List<Schedule> saveShedules(List<Schedule> schedules)
    {
        log.debug("Save {} schedules", schedules.size());
        List<Schedule> savedSchedules = scheduleRepository.saveAll(schedules);
        log.debug("Schedules successfully saved");
        return savedSchedules;
    }

    public Page<Schedule> getSchedulesByParams(SortParams sortParams, ScheduleFilter scheduleFilter)
    {
        Pageable page = PageRequest.of(
            sortParams.getPageNumber() - 1, 
            sortParams.getPageSize(),
            sortParams.getSortOrder().equalsIgnoreCase("asc")
                ? Sort.by(sortParams.getSortBy()).ascending()
                : Sort.by(sortParams.getSortBy()).descending());
        
        Specification<Schedule> specification = ScheduleSpecification.dynamicFilter(scheduleFilter);
        Page<Schedule> ans = scheduleRepository.findAll(specification, page);

        log.info("Fetched {} schedules by params", ans.getTotalElements());
        return ans;
    }

    public byte[] exportAllSchedules()
    {
        log.debug("Exporting all schedules");
        List<Schedule> allSchedules = scheduleRepository.findAll();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            objectMapper.writeValue(outputStream, new SchedulesJsonResponse(allSchedules));
        } catch (IOException e) {
            log.error("Error export file. By: {}", e.getMessage());
            throw new ExportException("Schedules");
        }
        log.info("Exported {} schedules", allSchedules.size());
        return outputStream.toByteArray();
    }

    public void receiveUpdatedSchedule(Long courierId)
    {
        List<Schedule> schedules = getSchedulesByCourierId(courierId);
        log.info("SEND '/topic/schedules' | Update schedule by courierId: {}", courierId);
        messagingTemplate.convertAndSend("/topic/schedules", new ScheduleResponse(courierId, schedules));
    }

    public List<Schedule> buildSchedule(Courier courier, DeliveryOrder order, List<Schedule> schedule, int pickupIndex, int deliveryIndex) {
        return schedulingAlgorithm.buildSchedule(courier, order, schedule, pickupIndex, deliveryIndex);
    }

    public SchedulingAlgorithm.ResultFindMaxProfit findMaxProfit(Courier courier, List<Schedule> schedule, DeliveryOrder order) {
        return schedulingAlgorithm.findMaxProfit(courier, schedule, order);
    }

    public long computeTotalOperationTime(Courier courier, List<Schedule> schedule) {
        return schedulingAlgorithm.computeTotalOperationTime(courier, schedule);
    }

    public boolean isLoadFeasible(Courier courier, List<Schedule> schedule) {
        return schedulingAlgorithm.isLoadFeasible(courier, schedule);
    }

    public double computeScheduleProfit(Courier courier, List<Schedule> schedule) {
        return schedulingAlgorithm.computeScheduleProfit(courier, schedule);
    }

    public double sumDeliveryCost(List<Schedule> schedule) {
        return schedulingAlgorithm.sumDeliveryCost(schedule);
    }

    public SchedulingAlgorithm.ResultRebuildSchedule rebuildSchedule(Courier courier, List<Schedule> schedule) {
        return schedulingAlgorithm.rebuildSchedule(courier, schedule);
    }
}
