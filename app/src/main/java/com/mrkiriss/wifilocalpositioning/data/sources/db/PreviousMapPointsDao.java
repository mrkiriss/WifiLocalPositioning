package com.mrkiriss.wifilocalpositioning.data.sources.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;

import java.util.List;

@Dao
public interface PreviousMapPointsDao {
    @Query("SELECT * FROM mappoint")
    List<MapPoint> findAll();

    @Insert
    MapPoint insert(MapPoint mapPoint);

    @Delete
    void delete(MapPoint mapPoint);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAll( List<LocationPointInfo> data);
}
