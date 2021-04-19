package com.mrkiriss.wifilocalpositioning.data.models.server;

import lombok.Data;

@Data
public class AccessPoint {
    private final String mac; //MAC
    private final Integer rssi; //signal strength

    public AccessPoint(String mac, int rssi){
        this.mac=mac;
        this.rssi = rssi;
    }
}
