package com.uia.delivery.repository.specification;

import java.util.stream.Stream;

import org.springframework.data.jpa.domain.Specification;

import com.uia.delivery.controller.filter.OrderFilter;
import com.uia.delivery.entity.DeliveryOrder;

public class DeliveryOrderSpecification
{
    private DeliveryOrderSpecification()
    {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<DeliveryOrder> dynamicFilter(OrderFilter filter)
    {
        return Specification.where(filterTypesOrder(filter.getTypesOrder()))
                .and(BaseSpecification.filterMinMaxDouble(
                    filter.getMinCost(), 
                    filter.getMaxCost(),
                    "cost"))
                .and(BaseSpecification.filterMinMaxInteger(
                    filter.getMinWeight(), 
                    filter.getMaxWeight(),
                    "weight"))
                .and(BaseSpecification.filterMinMaxLong(
                    filter.getMinDeliveryPeriod(), 
                    filter.getMaxDeliveryPeriod(),
                    "deliveryPeriod"))
                .and(BaseSpecification.filterLikeString(
                    filter.getName(),
                    "name"));
    }

    private static Specification<DeliveryOrder> filterTypesOrder(String typesOrder)
    {
        return (root, query, builder) -> {
            if(typesOrder == null || typesOrder.isEmpty())
                return builder.conjunction();
            
            return root.join("typeOrder")
                    .get("type")
                    .in(Stream.of(typesOrder.split("-")).toList());
        };
    }
}
