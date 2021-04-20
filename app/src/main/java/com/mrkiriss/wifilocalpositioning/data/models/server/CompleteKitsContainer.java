package com.mrkiriss.wifilocalpositioning.data.models.server;

import android.net.wifi.ScanResult;

import java.util.List;

import lombok.Data;

@Data
public class CompleteKitsContainer {
    private List<List<ScanResult>> completeKits;
    private String requestSourceType;
}
