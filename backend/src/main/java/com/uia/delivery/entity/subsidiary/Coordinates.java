package com.uia.delivery.entity.subsidiary;

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
}
