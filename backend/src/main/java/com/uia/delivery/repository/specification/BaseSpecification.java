package com.uia.delivery.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.uia.delivery.controller.filter.RectandlePosFilter;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public abstract class BaseSpecification 
{
    private BaseSpecification()
    {
        throw new IllegalStateException("Base utility class");
    }

    protected static <T> Specification<T> filterLikeString(String equalAttr, String attributeName) {
        return (root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            if (equalAttr != null) {
                predicate = builder.and(predicate, builder.like(root.get(attributeName), equalAttr + "%"));
            }
            return predicate;
        };
    }

    protected static <T> Specification<T> filterMinMaxDouble(Double minAttr, Double maxAttr, String attributeName) {
        return (root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            if (minAttr != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get(attributeName), minAttr));
            }
            if (maxAttr != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get(attributeName), maxAttr));
            }
            return predicate;
        };
    }

    public static <T> Specification<T> getInRectPos(RectandlePosFilter filter, String attributeName) {
        return (root, query, cb) -> {
            Path<Double> xPath = root.get(attributeName).get("x");
            Path<Double> yPath = root.get(attributeName).get("y");
            Predicate px = cb.between(xPath, filter.getLeftUp().getX(), filter.getRightBottom().getX());
            Predicate py = cb.between(yPath, filter.getRightBottom().getY(), filter.getLeftUp().getY());
            return cb.and(px, py);
        };
    }

    protected static <T> Specification<T> filterMinMaxInteger(Integer minAttr, Integer maxAttr, String attributeName) {
        return (root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            if (minAttr != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get(attributeName), minAttr));
            }
            if (maxAttr != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get(attributeName), maxAttr));
            }
            return predicate;
        };
    }

    protected static <T> Specification<T> filterMinMaxLong(Long minAttr, Long maxAttr, String attributeName) {
        return (root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            if (minAttr != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get(attributeName), minAttr));
            }
            if (maxAttr != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get(attributeName), maxAttr));
            }
            return predicate;
        };
    }
}
