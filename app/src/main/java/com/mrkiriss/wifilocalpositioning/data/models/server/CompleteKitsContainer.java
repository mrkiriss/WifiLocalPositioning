package com.mrkiriss.wifilocalpositioning.data.models.server;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CompleteKitsContainer {
    private List<List<ScanResult>> completeKits;
    private String requestSourceType;
    private int maxNumberOfScans;

    public CompleteKitsContainer(){
        this.completeKits=new ArrayList<>();
    }

    public void addKitOfScan(List<ScanResult> results){
        if (completeKits.size()<maxNumberOfScans){
            completeKits.add(results);
        }
    }
}
