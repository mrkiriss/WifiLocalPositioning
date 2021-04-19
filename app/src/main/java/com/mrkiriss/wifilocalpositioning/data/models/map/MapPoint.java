package com.mrkiriss.wifilocalpositioning.models.map;

import lombok.Data;

@Data
public class MapPoint {
    private int x;
    private int y;
    private Floor floorWithPointer;
    private String tag;
}