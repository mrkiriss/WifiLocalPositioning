package com.mrkiriss.wifilocalpositioning.data.models.server;

import lombok.Data;

@Data
public class DefinedLocationPoint {
    private int x;
    private int y;
    private String roomName;
    private int floorId;
    private String isRoom;

    private String steps;

    public boolean isRoom(){
        return isRoom != null && isRoom.equals("true");
    }
}
