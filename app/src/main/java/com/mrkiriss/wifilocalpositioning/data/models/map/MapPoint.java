package com.mrkiriss.wifilocalpositioning.data.models.map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
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
    public boolean equals(MapPoint mapPoint){
        return (mapPoint.getRoomName().equals(roomName) &&
                mapPoint.getX()==x &&
                mapPoint.getY()==y);
    }
    public String getFloorIdIntWithFloorText(){
        return "Этаж: "+floorIdInt;
    }

    private final String currentLocationText = "Текущее местоположение: ";
    private final String currentLocationSmallText = "Текущее местоположение";
    @Override
    public String toString(){
        if (roomName.contains(currentLocationText)) return roomName.replace(currentLocationText,"");
        return roomName;
    }
    public MapPoint copyForCurrentLocation(){
        MapPoint result = new MapPoint(x, y, currentLocationText + roomName, isRoom);
        result.setFloorIdInt(floorIdInt);
        result.setFloorWithPointer(floorWithPointer);
        return result;
    }
    public MapPoint copyForCurrentLocationNotRoom(){
        MapPoint result = new MapPoint(x, y, currentLocationSmallText, isRoom);
        result.setFloorIdInt(floorIdInt);
        result.setFloorWithPointer(floorWithPointer);
        return result;
    }

    public MapPoint copy(){
        MapPoint result = new MapPoint(x, y, roomName, isRoom);
        result.setFloorIdInt(floorIdInt);
        result.setFloorWithPointer(floorWithPointer);
        return result;
    }
}