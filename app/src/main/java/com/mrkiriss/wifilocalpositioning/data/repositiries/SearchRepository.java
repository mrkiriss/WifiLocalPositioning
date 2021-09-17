package com.mrkiriss.wifilocalpositioning.data.repositiries;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SearchRepository {

    private List<SearchItem> prevViewedMapPoints;
    private List<SearchItem> availableForSearchMapPoints;
    private SearchItem currentLocation;

    private final MutableLiveData<List<SearchItem>> requestToUpdateSearchContent;

    public SearchRepository() {
        requestToUpdateSearchContent = new MutableLiveData<>();
    }

    public void saveSearchData(SearchData data) {
        prevViewedMapPoints = data.getPrevViewedSearchItems();
        availableForSearchMapPoints = data.getAvailableForSearchItems();
        currentLocation = data.getCurrentLocation();

        Log.i("searchMode", "availableForSearchMapPoints in saveSearchDate: "+ availableForSearchMapPoints.toString());
    }

    public void responseToSearchInput(String input) {
        List<SearchItem> result = new ArrayList<>();

        if (currentLocation != null) result.add(currentLocation);

        if (input.isEmpty()) {
            result = new ArrayList<>(prevViewedMapPoints);
        }else {
            if (availableForSearchMapPoints != null) {
                for (SearchItem item : availableForSearchMapPoints) {
                    if (item.getName().toLowerCase().contains(input.toLowerCase())
                            || item.getDescription().toLowerCase().contains(input.toLowerCase())) {
                        result.add(item);
                    }
                }
            }
        }

        Log.i("searchMode", "result of filtering after responseToSearchInput: "+ result.toString());

        requestToUpdateSearchContent.setValue(result);
    }

}
