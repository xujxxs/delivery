package com.uia.delivery.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.uia.delivery.entity.subsidiary.Coordinates;
import com.uia.delivery.entity.subsidiary.TypeOperation;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
public class Schedule 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long index;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @ManyToOne
    @JoinColumn(name = "delivery_order_id", nullable = false)
    private DeliveryOrder deliveryOrder;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TypeOperation typeOperation;

    @Column(nullable = false)
    private Long periodOperation; // Measured in seconds
    
    
    @Column(nullable = false)
    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "position_start_x"))
    @AttributeOverride(name = "y", column = @Column(name = "position_start_y"))
    private Coordinates positionStart;


    @Column(nullable = false)
    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "position_end_x"))
    @AttributeOverride(name = "y", column = @Column(name = "position_end_y"))
    private Coordinates positionEnd;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime redactedAt;

    public void calculateOperationTime()
    {
        double distanceDelivery = positionEnd.distanceTo(positionStart);
        this.periodOperation = (long)(distanceDelivery / (courier.getSpeed() / 3600));
    }
}
