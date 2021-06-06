package com.mrkiriss.wifilocalpositioning.data.sources.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.mrkiriss.wifilocalpositioning.data.models.server.ListOfAllMapPoints;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;

import java.util.List;

@Dao
@TypeConverters(LocationPointInfoTypeConverter.class)
public interface MapPointsDao {

    @Query("SELECT * FROM locationpointinfo")
    List<LocationPointInfo> findAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAll( List<LocationPointInfo> data);

    @Query("DELETE FROM locationpointinfo")
    void deleteAll();
}
