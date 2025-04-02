package com.uia.delivery.dto;

import java.util.List;

import com.uia.delivery.entity.DeliveryOrder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrdersJsonResponse 
{
    private List<DeliveryOrder> orders;
}
