package com.mrkiriss.wifilocalpositioning.models.server;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CalibrationLocationPoint {
    private double lat;
    private double lon;
    private String roomName;
    private List<List<AccessPoint>> calibrationSets;

    public CalibrationLocationPoint(){
        calibrationSets= new ArrayList<>();
    }
    public void addCalibrationSet(List<AccessPoint> accessPoints){
        calibrationSets.add(accessPoints);
    }
}
