package com.uia.delivery.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.uia.delivery.entity.subsidiary.Coordinates;
import com.uia.delivery.entity.subsidiary.TypeOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Courier 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = true)
    private String surname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false)
    private Double cost; // Measured in currency per second

    @Column(nullable = false)
    private Double speed; // Measured in km per hour

    @Column(nullable = false)
    private Integer loadCapacity; // Measured in grams

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "supported_courier_order_types",
        joinColumns = @JoinColumn(name = "courier_id"),
        inverseJoinColumns = @JoinColumn(name = "type_order_id")
    )
    private List<TypeOrder> supportedTypeOrders;

    @Column(nullable = false)
    @Embedded
    private Coordinates position;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime redactedAt;
}
