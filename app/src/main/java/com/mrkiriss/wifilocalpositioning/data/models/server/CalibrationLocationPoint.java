package com.mrkiriss.wifilocalpositioning.data.models.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
public class CalibrationLocationPoint {
    private String roomName;
    private List<List<AccessPoint>> calibrationSets;

    public CalibrationLocationPoint(){
        calibrationSets=new LinkedList<>();
    }

    public void addOneCalibrationSet(List<AccessPoint> oneSet){
        calibrationSets.add(oneSet);
    }
}
