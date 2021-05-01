package com.mrkiriss.wifilocalpositioning.data.models.server;

import lombok.Data;

@Data
public class LocationPointInfo {
    private String roomName;

    private int floorId;
    private int x;
    private int y;
    private String isRoom; // иначе коридор (для создания маршрута)

    public LocationPointInfo(int x, int y, String roomName, int floorId, String isRoom){
        this.x=x;
        this.y=y;
        this.roomName=roomName;
        this.floorId=floorId;
        this.isRoom=isRoom;
    }
    public LocationPointInfo(){}
    public boolean isRoom(){
        return isRoom.equals("true");
    }
}
