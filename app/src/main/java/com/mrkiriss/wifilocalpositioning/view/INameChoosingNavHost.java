package com.mrkiriss.wifilocalpositioning.view;

import androidx.fragment.app.Fragment;

import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;

public interface INameChoosingNavHost {
    void useUpButton();
    void navigateToFindFragment(Fragment current, SearchData searchData);
    void navigateToDefinitionFragment(Fragment current, Fragment target, TypeOfSearchRequester typeOfRequester, MapPoint selectedLocation);
}