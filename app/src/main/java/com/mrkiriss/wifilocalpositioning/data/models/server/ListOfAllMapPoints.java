package com.mrkiriss.wifilocalpositioning.data.models.server;

import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ListOfAllMapPoints {
    private List<LocationPointInfo> locationPointInfos;

    public Map<FloorId, List<MapPoint>> convertToMap(){
        HashMap<FloorId, List<MapPoint>> result = new HashMap<>();
        FloorId floorId = null;

        for (LocationPointInfo info: locationPointInfos){
            floorId = Floor.convertFloorIdToEnum(info.getFloorId());

            if (!result.keySet().contains(floorId)){
                result.put(floorId, new ArrayList<>());
            }

            MapPoint mapPoint = new MapPoint(info.getX(), info.getY(), info.getRoomName());
            result.get(floorId).add(mapPoint);

        }

        return result;
    }
}
