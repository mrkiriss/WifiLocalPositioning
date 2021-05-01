package com.mrkiriss.wifilocalpositioning.data.models.server;

import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Connections {
    private String mainRoomName;
    private List<LocationPointInfo> secondaryRooms;

    public List<String> getListOfNames(){
        List<String> result = new ArrayList<>();
        for (LocationPointInfo lpi: secondaryRooms){
            result.add(lpi.getRoomName());
        }
        return result;
    }
    public List<MapPoint> convertToListOfMapPoints(){
        List<MapPoint> result = new ArrayList<>();
        for (LocationPointInfo lpi:secondaryRooms){
            result.add(new MapPoint(lpi.getX(), lpi.getY(), lpi.getRoomName(), lpi.isRoom()));
        }
        return result;
    }
    public static Connections convertToOnlyNameConnections(String mainName, List<MapPoint> mapPoints){
        Connections result = new Connections();
        result.setMainRoomName(mainName);

        List<LocationPointInfo> lpis = new ArrayList<>();
        for (MapPoint mapPoint:mapPoints){
            LocationPointInfo lpi = new LocationPointInfo();
            lpi.setRoomName(mapPoint.getRoomName());
            lpis.add(lpi);
        }
        result.setSecondaryRooms(lpis);

        return result;
    }
}
