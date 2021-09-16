package com.mrkiriss.wifilocalpositioning.data.models.search;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import lombok.Data;

@Data
public class SearchItem {
    private String name;
    private String description;
    private int icon;

    public SearchItem(@NonNull String name, @NonNull String description) {
        this.name = name;
        this.description = description;
    }
}
