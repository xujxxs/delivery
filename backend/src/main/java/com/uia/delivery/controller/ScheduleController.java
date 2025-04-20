package com.uia.delivery.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uia.delivery.controller.filter.ScheduleFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.entity.Schedule;
import com.uia.delivery.entity.subsidiary.TypeOperation;
import com.uia.delivery.service.ScheduleService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController 
{
    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService)
    {
        this.scheduleService = scheduleService;
    }
    
    @GetMapping("/{scheduleId}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long scheduleId)
    {
        log.info("GET '/api/schedule/{}'", scheduleId);
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        log.debug("Getted schedule: {}", schedule);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(schedule);
    }
    
    @GetMapping
    public ResponseEntity<Page<Schedule>> getSchedulesByParams(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "redactedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Long minIndex,
            @RequestParam(required = false) Long maxIndex,
            @RequestParam(required = false) Long minCourierId,
            @RequestParam(required = false) Long maxCourierId,
            @RequestParam(required = false) Long minOrderId,
            @RequestParam(required = false) Long maxOrderId,
            @RequestParam(required = false) TypeOperation typeOperation,
            @RequestParam(required = false) Long minPeriodOperation,
            @RequestParam(required = false) Long maxPeriodOperation
    ) {
        SortParams sortParams = SortParams.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
            .build();
        
        ScheduleFilter scheduleFilter = ScheduleFilter.builder()
                .minIndex(minIndex)
                .maxIndex(maxIndex)
                .minCourierId(minCourierId)
                .maxCourierId(maxCourierId)
                .minOrderId(minOrderId)
                .maxOrderId(maxOrderId)
                .typeOperation(typeOperation)
                .minPeriodOperation(minPeriodOperation)
                .maxPeriodOperation(maxPeriodOperation)
            .build();

        log.info("GET '/api/schedule' | Sort params: {}, Courier filter: {}",
            sortParams, scheduleFilter);
        Page<Schedule> pageSchedule = scheduleService.getSchedulesByParams(sortParams, scheduleFilter);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pageSchedule);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportJSON() 
    {
        log.info("GET '/api/schedule/export'");
        byte[] exportFile = scheduleService.exportAllSchedules();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=schedules.json")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(exportFile);
    }
    
}
