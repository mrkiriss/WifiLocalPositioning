package com.mrkiriss.wifilocalpositioning.data.models.search;

import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.view.LocationNameChoosingFragment;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class SearchData implements Serializable {
    private final List<MapPoint> prevViewedMapPoints;
    private final List<MapPoint> availableForSearchMapPoints;
    private final MapPoint currentLocation;
    private final String hintInSearchLine;
    private final TypeOfSearchRequester typeOfRequester;

    public SearchData(List<MapPoint> prevViewedMapPoints, List<MapPoint> availableForSearchMapPoints, MapPoint currentLocation, String hintInSearchLine, TypeOfSearchRequester typeOfRequester) {
        this.prevViewedMapPoints=prevViewedMapPoints;
        this.availableForSearchMapPoints=availableForSearchMapPoints;
        this.currentLocation=currentLocation;
        this.hintInSearchLine=hintInSearchLine;
        this.typeOfRequester=typeOfRequester;
    }
}
