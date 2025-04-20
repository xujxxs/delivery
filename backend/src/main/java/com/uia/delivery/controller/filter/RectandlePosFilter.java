package com.uia.delivery.controller.filter;

import com.uia.delivery.entity.subsidiary.Coordinates;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RectandlePosFilter 
{
    private Coordinates leftUp;
    private Coordinates rightBottom;
}
