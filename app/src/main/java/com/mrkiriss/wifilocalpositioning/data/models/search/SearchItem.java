package com.mrkiriss.wifilocalpositioning.data.models.search;

import android.graphics.drawable.Drawable;

import lombok.Data;

@Data
public class SearchItem {
    private String name;
    private String description;
    private int icon;

    public SearchItem(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
