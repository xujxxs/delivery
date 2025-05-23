package com.uia.delivery.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Column(nullable = false)
    private LocalTime openPeriod;

    @Column(nullable = false)
    private LocalTime closePeriod;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = true)
    private Courier assignedCourier;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime redactedAt;

    public void setCost(Double cost)
    {
        this.cost = BigDecimal.valueOf(cost).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
