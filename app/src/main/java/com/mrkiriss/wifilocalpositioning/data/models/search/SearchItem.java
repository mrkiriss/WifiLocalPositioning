package com.mrkiriss.wifilocalpositioning.data.models.search;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.R;

import java.io.Serializable;

import lombok.Data;

@Data
public class SearchItem implements Comparable<SearchItem>, Serializable {
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

    @Override
    public int compareTo(SearchItem o) {
        if (icon == R.drawable.ic_past) {
            return 0;
        } else {
            return name.compareTo(o.name);
        }
    }
}
