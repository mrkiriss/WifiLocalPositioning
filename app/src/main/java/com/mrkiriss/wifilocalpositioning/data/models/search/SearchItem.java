package com.mrkiriss.wifilocalpositioning.data.models.search;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import lombok.Data;

@Data
public class SearchItem {
    private String name;
    private String description;
    private int icon;

    private MutableLiveData<SearchItem> requestToProcessSelectedLocation;

    public SearchItem(@NonNull String name, @NonNull String description) {
        this.name = name;
        this.description = description;
    }

    public void onItemClick() {
        requestToProcessSelectedLocation.setValue(this);
    }
}
