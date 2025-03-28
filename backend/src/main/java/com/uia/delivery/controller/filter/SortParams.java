package com.uia.delivery.controller.filter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SortParams 
{
    private Integer pageNumber;
    private Integer pageSize;
    private String sortBy;
    private String sortOrder;
}
