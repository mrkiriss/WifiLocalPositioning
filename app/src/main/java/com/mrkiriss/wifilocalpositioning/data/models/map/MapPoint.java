package com.mrkiriss.wifilocalpositioning.data.models.map;

import java.util.Map;

import lombok.Data;

@Data
public class MapPoint {
    private int x;
    private int y;
    private Floor floorWithPointer;
    private String tag;
    private boolean isRoom;

    public MapPoint(int x, int y, String tag, boolean isRoom){
        this.x=x;
        this.y=y;
        this.tag=tag;
        this.isRoom=isRoom;
    }
    public MapPoint(){}

    @Override
    public String toString(){
        return "x="+x+" "+
                " y="+y+"\n"+
                "isRoom="+isRoom+
                " roomName="+tag;
    }
    public String toOneString(){
            return "x="+x+" "+
            " y="+y+" isRoom="+isRoom+
                    " roomName="+tag;
    }
}