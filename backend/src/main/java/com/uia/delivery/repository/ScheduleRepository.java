package com.uia.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.uia.delivery.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>, JpaSpecificationExecutor<Schedule> {

}
