package com.uia.delivery.entity.subsidiary;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates 
{
    private Double x;
    private Double y;

    public double distanceTo(Coordinates other) 
    {
        return Math.sqrt(Math.pow(x - other.getX(), 2) + Math.pow(y - other.getY(), 2));
    }

    public void setX(Double x)
    {
        this.x = BigDecimal.valueOf(x).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    public void setY(Double y)
    {
        this.y = BigDecimal.valueOf(y).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}
