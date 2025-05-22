package com.uia.delivery.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long index;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Courier courier;

    @ManyToOne
    @JoinColumn(name = "delivery_order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DeliveryOrder deliveryOrder;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TypeOperation typeOperation;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(nullable = false)
    private Long amountTimeSpent;
    
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

    public void calculateOperationTime(LocalTime prevTime)
    {
        double distanceDelivery = positionEnd.distanceTo(positionStart);
        long secondsToArrive = (long) (distanceDelivery / courier.getSpeed() * 3600);
        this.amountTimeSpent = secondsToArrive;
        
        LocalTime minTimeEndOperation = prevTime.plusSeconds(secondsToArrive);
        if(typeOperation.equals(TypeOperation.PICKUP) 
                && !minTimeEndOperation.isAfter(deliveryOrder.getOpenPeriod())) 
        {
            this.arrivalTime = deliveryOrder.getOpenPeriod();
            return;
        }
        this.arrivalTime = minTimeEndOperation;
    }
}
