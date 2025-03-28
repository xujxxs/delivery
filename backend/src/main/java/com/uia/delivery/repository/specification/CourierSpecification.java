package com.uia.delivery.repository.specification;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.domain.Specification;

import com.uia.delivery.controller.filter.CourierFilter;
import com.uia.delivery.entity.Courier;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

public class CourierSpecification
{
    private CourierSpecification()
    {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Courier> dynamicFilter(CourierFilter filter) 
    {
        return Specification.where(filterSupportedTypesOrder(filter.getSupportedTypesOrder()))
                .and(BaseSpecification.filterLikeString(
                    filter.getFirstname(), 
                    "firstname"))
                .and(BaseSpecification.filterLikeString(
                    filter.getSurname(), 
                    "surname"))
                .and(BaseSpecification.filterLikeString(
                    filter.getLastname(), 
                    "lastname"))
                .and(BaseSpecification.filterMinMaxDouble(
                    filter.getMinCost(), 
                    filter.getMaxCost(), 
                    "cost"))
                .and(BaseSpecification.filterMinMaxDouble(
                    filter.getMinSpeed(), 
                    filter.getMaxSpeed(), 
                    "speed"))
                .and(BaseSpecification.filterMinMaxInteger(
                    filter.getMinLoadCapacity(), 
                    filter.getMaxLoadCapacity(), 
                    "loadCapacity"));
    }

    private static Specification<Courier> filterSupportedTypesOrder(String supportedTypesOrder)
    {
        return (root, query, builder) -> {
            Predicate predicate = builder.conjunction();

            if(supportedTypesOrder == null || supportedTypesOrder.isEmpty())
                return predicate;
            
            List<String> typesOrder = Stream.of(supportedTypesOrder.split("-")).toList();
            
            for (String type : typesOrder) {
                Subquery<Long> subquery = query.subquery(Long.class);
                var subJoin = subquery.correlate(root).join("supportedTypeOrders");
                subquery.select(builder.literal(1L))
                        .where(builder.equal(subJoin.get("type"), type));
                predicate = builder.and(predicate, builder.exists(subquery));
            }

            return predicate;
        };
    }
}
