package com.uia.delivery.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.uia.delivery.entity.subsidiary.Coordinates;
import com.uia.delivery.entity.subsidiary.TypeOrder;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOrder 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer weight; // Measured in grams

    @Column(nullable = false)
    private Double cost; // Measured in currency

    @Column(nullable = false)
    private Long deliveryPeriod; // Measured in seconds

    @ManyToOne
    @JoinColumn(name = "type_order_id", nullable = false)
    private TypeOrder typeOrder;

    @Column(nullable = false)
    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "pickup_x"))
    @AttributeOverride(name = "y", column = @Column(name = "pickup_y"))
    private Coordinates positionPickUp;

    @Column(nullable = false)
    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "delivery_x"))
    @AttributeOverride(name = "y", column = @Column(name = "delivery_y"))
    private Coordinates positionDelivery;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime redactedAt;
}
