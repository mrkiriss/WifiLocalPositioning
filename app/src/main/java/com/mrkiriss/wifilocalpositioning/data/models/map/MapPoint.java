package com.mrkiriss.wifilocalpositioning.data.models.map;

import java.util.Map;

import lombok.Data;

@Data
public class MapPoint {
    private int x;
    private int y;
    private Floor floorWithPointer;
    private String tag;

    public MapPoint(int x, int y, String tag){
        this.x=x;
        this.y=y;
        this.tag=tag;
    }
    public MapPoint(){}

    @Override
    public String toString(){
        return "x="+x+"\n"+
                " y="+y+"\n"+
                " roomName="+tag;
    }
}