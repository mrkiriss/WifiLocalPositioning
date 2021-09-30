package com.mrkiriss.wifilocalpositioning.data.sources.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mrkiriss.wifilocalpositioning.data.models.settings.AbilitiesScanningData;

@Dao
public interface AbilitiesDao {
    @Query("SELECT * FROM abilitiesscanningdata WHERE id= :id")
    AbilitiesScanningData findFirst(Long id);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AbilitiesScanningData data);
}
