package com.uia.delivery.dto.mapresponse;

import com.uia.delivery.entity.Schedule;
import com.uia.delivery.entity.subsidiary.Coordinates;

import lombok.Data;

@Data
public class ScheduleMapResponse 
{
    private Long id;
    private Coordinates startPosition;
    private Coordinates endPosition;

    public ScheduleMapResponse(Schedule schedule)
    {
        this.id = schedule.getId();
        this.startPosition = schedule.getPositionStart();
        this.endPosition = schedule.getPositionEnd();
    }
}
