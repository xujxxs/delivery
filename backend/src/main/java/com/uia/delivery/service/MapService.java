package com.uia.delivery.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.uia.delivery.controller.filter.RectandlePosFilter;
import com.uia.delivery.dto.mapresponse.CourierMapResponse;
import com.uia.delivery.dto.mapresponse.MapResponse;
import com.uia.delivery.dto.mapresponse.OrderMapResponse;
import com.uia.delivery.dto.mapresponse.ScheduleMapResponse;
import com.uia.delivery.entity.Courier;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.entity.subsidiary.Coordinates;
import com.uia.delivery.repository.CourierRepository;
import com.uia.delivery.repository.DeliveryOrderRepository;
import com.uia.delivery.repository.ScheduleRepository;
import com.uia.delivery.repository.specification.CourierSpecification;
import com.uia.delivery.repository.specification.DeliveryOrderSpecification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MapService 
{
    private final CourierRepository courierRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final ScheduleRepository scheduleRepository;

    public MapService(
            CourierRepository courierRepository,
            DeliveryOrderRepository deliveryOrderRepository,
            ScheduleRepository scheduleRepository
    ) {
        this.courierRepository = courierRepository;
        this.deliveryOrderRepository = deliveryOrderRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public boolean doSegmentsIntersect(
            Coordinates p1, Coordinates p2,
            Coordinates q1, Coordinates q2
    ) {
        int o1 = orientation(p1, p2, q1);
        int o2 = orientation(p1, p2, q2);
        int o3 = orientation(q1, q2, p1);
        int o4 = orientation(q1, q2, p2);

        if (o1 != o2 && o3 != o4)
            return true;

        if ((o1 == 0 && onSegment(p1, q1, p2))
            || (o2 == 0 && onSegment(p1, q2, p2))
            || (o3 == 0 && onSegment(q1, p1, q2))
            || (o4 == 0 && onSegment(q1, p2, q2)))
        {
            return true;
        }

        return false;
    }

    private int orientation(Coordinates a, Coordinates b, Coordinates c) {
        double val = (b.getY() - a.getY()) * (c.getX() - b.getX())
                   - (b.getX() - a.getX()) * (c.getY() - b.getY());
        if (Math.abs(val) < 1e-10) return 0;
        return (val > 0) ? 1 : 2;
    }

    private boolean onSegment(Coordinates p, Coordinates r, Coordinates q) {
        return r.getX() <= Math.max(p.getX(), q.getX()) &&
               r.getX() >= Math.min(p.getX(), q.getX()) &&
               r.getY() <= Math.max(p.getY(), q.getY()) &&
               r.getY() >= Math.min(p.getY(), q.getY());
    }

    private boolean isInside(RectandlePosFilter rect, Coordinates point)
    {
        return point.getX() <= rect.getRightBottom().getX() 
            && point.getY() >= rect.getRightBottom().getY() 
            && point.getX() >= rect.getLeftUp().getX()
            && point.getY() <= rect.getLeftUp().getY();
    }

    public boolean doesSegmentIntersectRectangle(
            Coordinates p1,
            Coordinates p2,
            RectandlePosFilter rect
    ) {
        double x1 = rect.getLeftUp().getX();
        double y1 = rect.getLeftUp().getY();
        double x2 = rect.getRightBottom().getX();
        double y2 = rect.getRightBottom().getY();

        if (isInside(rect, p1) || isInside(rect, p2)) {
            return true;
        }

        Coordinates topLeft = new Coordinates(x1, y1);
        Coordinates topRight = new Coordinates(x2, y1);
        Coordinates bottomRight = new Coordinates(x2, y2);
        Coordinates bottomLeft = new Coordinates(x1, y2);

        if (doSegmentsIntersect(p1, p2, topLeft, topRight)
            || doSegmentsIntersect(p1, p2, topRight, bottomRight)
            || doSegmentsIntersect(p1, p2, bottomRight, bottomLeft)
            || doSegmentsIntersect(p1, p2, bottomLeft, topLeft))
        {
            return true;
        }

        return false;
    }

    private List<ScheduleMapResponse> findExchangesSchedules(RectandlePosFilter rect)
    {
        return scheduleRepository.findAll().stream()
                .filter(schedule -> doesSegmentIntersectRectangle(
                    schedule.getPositionStart(), 
                    schedule.getPositionEnd(), 
                    rect
                )).map(ScheduleMapResponse::new)    
            .toList();
    }

    public MapResponse getObjectsByCoordinates(RectandlePosFilter filter) 
    {
        MapResponse result = new MapResponse();

        log.debug("Fetch couriers in rectangle: {}", filter);
        Specification<Courier> specCourier = CourierSpecification.getInRectPos(filter);
        result.setCouriers(courierRepository.findAll(specCourier).stream().map(CourierMapResponse::new).toList());
        log.debug("Fetched {} couriers in rectangle: {}", result.getCouriers().size(), filter);

        log.debug("Fetch orders in rectangle: {}", filter);
        Specification<DeliveryOrder> specOrder = DeliveryOrderSpecification.getInRectPos(filter);
        result.setOrders(deliveryOrderRepository.findAll(specOrder).stream().map(OrderMapResponse::new).toList());
        log.debug("Fetched {} orders in rectangle: {}", result.getOrders().size(), filter);

        log.debug("Fetch schedule in rectangle: {}", filter);
        result.setSchedules(findExchangesSchedules(filter));
        log.debug("Fetched {} schedule in rectangle: {}", result.getSchedules().size(), filter);

        return result;
    }
}
