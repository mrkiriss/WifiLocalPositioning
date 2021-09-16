package com.mrkiriss.wifilocalpositioning.data.models.search;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class SearchData implements Serializable {
    private final List<SearchItem> prevViewedSearchItems;
    private final List<SearchItem> availableForSearchItems;
    private final SearchItem currentLocation;
    private final String hintInSearchLine;
    private final TypeOfSearchRequester typeOfRequester;

    public SearchData(List<SearchItem> prevViewedSearchItems, List<SearchItem> availableForSearchItems, SearchItem currentLocation, String hintInSearchLine, TypeOfSearchRequester typeOfRequester) {
        this.prevViewedSearchItems=prevViewedSearchItems;
        this.availableForSearchItems=availableForSearchItems;
        this.currentLocation=currentLocation;
        this.hintInSearchLine=hintInSearchLine;
        this.typeOfRequester=typeOfRequester;

        setSearchIcons();
    }

    private void setSearchIcons(){
        int previousIcon = R.drawable.ic_past;
        int availableIcon = R.drawable.ic_circle;
        int currentLocationIcon = R.drawable.ic_cursor;

        for (SearchItem prevItem: prevViewedSearchItems) {
            prevItem.setIcon(previousIcon);
        }

        for (SearchItem availableItem: availableForSearchItems) {
            availableItem.setIcon(availableIcon);
        }

        if (currentLocation != null) currentLocation.setIcon(currentLocationIcon);
    }
}
