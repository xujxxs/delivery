package com.uia.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.uia.delivery.entity.Courier;

public interface CourierRepository extends JpaRepository<Courier, Long>, JpaSpecificationExecutor<Courier> {

}
