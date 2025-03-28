package com.uia.delivery.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.uia.delivery.controller.filter.ScheduleFilter;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.entity.Schedule;
import com.uia.delivery.entity.subsidiary.TypeOperation;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class ScheduleSpecification
{
    private ScheduleSpecification()
    {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Schedule> dynamicFilter(ScheduleFilter filter)
    {
        return Specification.where(filterTypeOperation(filter.getTypeOperation()))
                .and(BaseSpecification.filterMinMaxLong(
                    filter.getMinIndex(), 
                    filter.getMaxIndex(), 
                    "index"))
                .and(BaseSpecification.filterMinMaxLong(
                    filter.getMinPeriodOperation(), 
                    filter.getMaxPeriodOperation(), 
                    "periodOperation"))
                .and(filterJoinCourierMinMax(
                    filter.getMinCourierId(), 
                    filter.getMaxCourierId()))
                .and(filterJoinOrderMinMax(
                    filter.getMinOrderId(), 
                    filter.getMaxOrderId()));
    }

    private static Specification<Schedule> filterTypeOperation(TypeOperation typeOperation) 
    {
        return (root, query, builder) -> {
            Predicate predicate = builder.conjunction();

            if(typeOperation != null) {
                predicate = builder.and(predicate, builder.equal(root.get("typeOperation"), typeOperation));
            }

            return predicate;
        };
    }

    private static Specification<Schedule> filterJoinCourierMinMax(Long minId, Long maxId) 
    {
        return (root, query, builder) -> {
            Join<Schedule, Courier> courierJoin = root.join("courier");
            Predicate predicate = builder.conjunction();

            if(minId != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(courierJoin.get("id"), minId));
            }

            if(maxId != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(courierJoin.get("id"), maxId));
            }

            return predicate;
        };
    }

    private static Specification<Schedule> filterJoinOrderMinMax(Long minId, Long maxId) 
    {
        return (root, query, builder) -> {
            Join<Schedule, DeliveryOrder> orderJoin = root.join("deliveryOrder");
            Predicate predicate = builder.conjunction();

            if(minId != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(orderJoin.get("id"), minId));
            }

            if(maxId != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(orderJoin.get("id"), maxId));
            }

            return predicate;
        };
    }
}
