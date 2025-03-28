package com.uia.delivery.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.uia.delivery.controller.filter.ScheduleFilter;
import com.uia.delivery.controller.filter.SortParams;
import com.uia.delivery.entity.Schedule;
import com.uia.delivery.exception.NotFoundException;
import com.uia.delivery.repository.ScheduleRepository;
import com.uia.delivery.repository.specification.ScheduleSpecification;

@Service
public class ScheduleService 
{
    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository)
    {
        this.scheduleRepository = scheduleRepository;
    }

    public Schedule getScheduleById(Long id)
    {
        return scheduleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Schedule"));
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
        return scheduleRepository.findAll(specification, page);
    }
}
