package com.mrkiriss.wifilocalpositioning.data.models.map;

import lombok.Data;

@Data
public class MapPoint {
    private int x;
    private int y;
    private Floor floorWithPointer;
    private int floorIdInt;
    private String roomName;
    private boolean isRoom;

    public MapPoint(int x, int y, String roomName, boolean isRoom){
        this.x=x;
        this.y=y;
        this.roomName = roomName;
        this.isRoom=isRoom;
    }
    public MapPoint(){}

    public String toStringAllObject(){
        return "x="+x+" "+
                " y="+y+"\n"+
                "isRoom="+isRoom+
                " roomName="+ roomName;
    }
    public String toOneString(){
            return "x="+x+" "+
            " y="+y+" isRoom="+isRoom+
                    " roomName="+ roomName;
    }
    @Override
    public String toString(){
        return roomName;
    }
    public boolean equals(MapPoint mapPoint){
        return (mapPoint.getRoomName().equals(roomName) &&
                mapPoint.getX()==x &&
                mapPoint.getY()==y);
    }
    public String getFloorIdIntWithFloorText(){
        return "Этаж: "+floorIdInt;
    }
}