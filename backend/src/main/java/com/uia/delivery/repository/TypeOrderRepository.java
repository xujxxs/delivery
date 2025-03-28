package com.uia.delivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uia.delivery.entity.subsidiary.TypeOrder;

public interface TypeOrderRepository extends JpaRepository<TypeOrder, Long> {

    public Optional<TypeOrder> findByType(String type);
}
