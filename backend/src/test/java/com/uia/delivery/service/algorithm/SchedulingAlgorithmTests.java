package com.uia.delivery.service.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uia.delivery.entity.Courier;
import com.uia.delivery.entity.DeliveryOrder;
import com.uia.delivery.entity.Schedule;
import com.uia.delivery.entity.subsidiary.Coordinates;
import com.uia.delivery.entity.subsidiary.TypeOperation;
import com.uia.delivery.entity.subsidiary.TypeOrder;

@ExtendWith(MockitoExtension.class)
class SchedulingAlgorithmTests 
{
    private final SchedulingAlgorithm schedulingAlgorithm = new SchedulingAlgorithm();

    private Courier testCourer;

    @BeforeEach
    void setUp()
    {
        testCourer = new Courier();
        testCourer.setId(1L);
        testCourer.setLoadCapacity(30000);
        testCourer.setCost(1.0);
    }

    private DeliveryOrder createTestDeliveryOrder(Long id, Integer weight, Double cost, Long deliveryPeriod, String typeOrderName) {
        DeliveryOrder order = new DeliveryOrder();
        order.setId(id);
        order.setWeight(weight);
        order.setCost(cost);
        order.setDeliveryPeriod(deliveryPeriod);
        order.setTypeOrder(new TypeOrder(null, typeOrderName));
        
        Coordinates dummyCoord = new Coordinates(0.0, 0.0);
        order.setPositionPickUp(dummyCoord);
        order.setPositionDelivery(dummyCoord);
        return order;
    }

    private List<Schedule> createExistentSchedules()
    {
        DeliveryOrder order1 = createTestDeliveryOrder(null, 15000, 2000.0, 1000L, null);
        DeliveryOrder order2 = createTestDeliveryOrder(null, 15000, 1600.0, 1500L, null);
        DeliveryOrder order3 = createTestDeliveryOrder(null, 15000, 2000.0, 1600L, null);
        return List.of(
            Schedule.builder().deliveryOrder(order1).typeOperation(TypeOperation.PICKUP).periodOperation(200L).build(),
            Schedule.builder().deliveryOrder(order2).typeOperation(TypeOperation.PICKUP).periodOperation(200L).build(),
            Schedule.builder().deliveryOrder(order1).typeOperation(TypeOperation.DELIVERY).periodOperation(600L).build(),
            Schedule.builder().deliveryOrder(order3).typeOperation(TypeOperation.PICKUP).periodOperation(200L).build(),
            Schedule.builder().deliveryOrder(order2).typeOperation(TypeOperation.DELIVERY).periodOperation(200L).build(),
            Schedule.builder().deliveryOrder(order3).typeOperation(TypeOperation.DELIVERY).periodOperation(200L).build()
        );
    }

    private List<Schedule> createNonexistentSchedules()
    {
        DeliveryOrder order1 = createTestDeliveryOrder(null, 15000, null, 1000L, null);
        DeliveryOrder order2 = createTestDeliveryOrder(null, 15000, null, 1000L, null);
        DeliveryOrder order3 = createTestDeliveryOrder(null, 15000, null, 1500L, null);
        return List.of(
            Schedule.builder().deliveryOrder(order1).typeOperation(TypeOperation.PICKUP).periodOperation(200L).build(),
            Schedule.builder().deliveryOrder(order2).typeOperation(TypeOperation.PICKUP).periodOperation(200L).build(),
            Schedule.builder().deliveryOrder(order3).typeOperation(TypeOperation.PICKUP).periodOperation(200L).build(),
            Schedule.builder().deliveryOrder(order1).typeOperation(TypeOperation.DELIVERY).periodOperation(600L).build(),
            Schedule.builder().deliveryOrder(order2).typeOperation(TypeOperation.DELIVERY).periodOperation(200L).build(),
            Schedule.builder().deliveryOrder(order3).typeOperation(TypeOperation.DELIVERY).periodOperation(200L).build()
        );
    }

    @Test
    void computeTotalOperationTime_ReturnsLong()
    {
        List<Schedule> testSchedules = createExistentSchedules();

        Long result = schedulingAlgorithm.computeTotalOperationTime(testCourer, testSchedules);
        assertEquals(1600L, result);
    }

    @Test
    void computeTotalOperationTime_ReturnsMinusOne()
    {
        List<Schedule> testSchedules = createNonexistentSchedules();

        Long result = schedulingAlgorithm.computeTotalOperationTime(testCourer, testSchedules);
        assertEquals(-1L, result);
    }

    @Test
    void computeTotalOperationTime_ReturnsTrue()
    {
        List<Schedule> testSchedules = createExistentSchedules();

        assertTrue(schedulingAlgorithm.isLoadFeasible(testCourer, testSchedules));
    }

    @Test
    void computeTotalOperationTime_ReturnsFalse()
    {
        List<Schedule> testSchedules = createNonexistentSchedules();

        assertFalse(schedulingAlgorithm.isLoadFeasible(testCourer, testSchedules));
    }

    @Test
    void computeScheduleProfit_ReturnsDouble()
    {
        List<Schedule> testSchedules = createExistentSchedules();

        Double result = schedulingAlgorithm.computeScheduleProfit(testCourer, testSchedules);
        assertEquals(4000.0, result);
    }

    @Test
    void computeScheduleProfit_ReturnsMinusOne()
    {
        List<Schedule> testSchedules = createNonexistentSchedules();

        Double result = schedulingAlgorithm.computeScheduleProfit(testCourer, testSchedules);
        assertEquals(-1.0, result);
    }

    @Test
    void sumDeliveryCost_ReturnsDouble()
    {
        List<Schedule> testSchedules = createExistentSchedules();

        Double result = schedulingAlgorithm.sumDeliveryCost(testSchedules);
        assertEquals(5600.0, result);
    }
}
