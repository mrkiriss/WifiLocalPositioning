package com.mrkiriss.wifilocalpositioning.models;

import lombok.Data;

@Data
public class DefinedLocationPoint {
    private Double lat;
    private Double lon;
    private String roomName;
    private String steps;
}
