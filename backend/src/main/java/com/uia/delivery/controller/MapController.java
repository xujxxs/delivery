package com.uia.delivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uia.delivery.controller.filter.RectandlePosFilter;
import com.uia.delivery.dto.mapresponse.MapResponse;
import com.uia.delivery.entity.subsidiary.Coordinates;
import com.uia.delivery.service.MapService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
@RequestMapping("/api/map")
public class MapController 
{
    private final MapService mapService;

    public MapController(MapService mapService)
    {
        this.mapService = mapService;
    }

    @GetMapping
    public ResponseEntity<MapResponse> getMap(
            @RequestParam(name = "left_up_x", required = true) Double leftUpX,
            @RequestParam(name = "left_up_y", required = true) Double leftUpY,
            @RequestParam(name = "right_bottom_x", required = true) Double rightBottomX,
            @RequestParam(name = "right_bottom_y", required = true) Double rightBottomY
    ) {
        RectandlePosFilter filter = RectandlePosFilter.builder()
                .leftUp(new Coordinates(leftUpX, leftUpY))
                .rightBottom(new Coordinates(rightBottomX, rightBottomY))
            .build();

        log.info("GET '/api/map' | Filter: {}", filter);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(mapService.getObjectsByCoordinates(filter));
    }
    
}
