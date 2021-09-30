package com.mrkiriss.wifilocalpositioning.data.models.server;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity
public class LocationPointInfo {

    @PrimaryKey
    @NonNull
    private Long id;

    private String roomName;

    private int floorId;
    private int x;
    private int y;
    private String isRoom; // иначе коридор (для создания маршрута)

    @Ignore
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
