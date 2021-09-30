package com.mrkiriss.wifilocalpositioning.data.sources.db;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;

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
