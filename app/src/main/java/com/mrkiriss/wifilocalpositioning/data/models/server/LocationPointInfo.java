package com.mrkiriss.wifilocalpositioning.data.models.server;

import lombok.Data;

@Data
public class LocationPointInfo {
    private String roomName;

    private int floorId;
    private int x;
    private int y;

    public LocationPointInfo(int x, int y, String roomName, int floorId){
        this.x=x;
        this.y=y;
        this.roomName=roomName;
        this.floorId=floorId;
    }
}
