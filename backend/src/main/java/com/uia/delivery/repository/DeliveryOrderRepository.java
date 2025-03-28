package com.uia.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.uia.delivery.entity.DeliveryOrder;

public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long>, JpaSpecificationExecutor<DeliveryOrder> {

}
