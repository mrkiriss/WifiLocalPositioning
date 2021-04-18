package com.mrkiriss.wifilocalpositioning.models.server;

import lombok.Data;

@Data
public class DefinedLocationPoint {
    private int x;
    private int y;
    private String roomName;
    private int floorId;
    private String steps;
}
