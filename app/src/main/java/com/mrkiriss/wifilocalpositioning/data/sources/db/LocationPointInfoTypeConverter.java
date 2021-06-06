package com.mrkiriss.wifilocalpositioning.data.sources.db;

import android.text.TextUtils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.mrkiriss.wifilocalpositioning.data.models.server.ListOfAllMapPoints;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LocationPointInfoTypeConverter {

    private final Gson gson = new Gson();

    @TypeConverter
    public LocationPointInfo toLPIList(String jsonContainer){
        return gson.fromJson(jsonContainer, LocationPointInfo.class);
    }

    @TypeConverter
    public String fromLPIList(LocationPointInfo data){
        return gson.toJson(data);
    }
}
