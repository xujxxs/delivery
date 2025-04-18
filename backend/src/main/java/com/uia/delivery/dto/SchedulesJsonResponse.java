package com.uia.delivery.dto;

import java.util.List;

import com.uia.delivery.entity.Schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SchedulesJsonResponse 
{
    private List<Schedule> schedules;
}
