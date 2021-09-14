package com.mrkiriss.wifilocalpositioning.view;

import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;

public interface IProcessingSelectedByFindLocation {
    void processSelectedByFindLocation(TypeOfSearchRequester typeOfRequester, MapPoint selectedLocation);
}
