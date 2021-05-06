package com.mrkiriss.wifilocalpositioning.data.sources.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.mrkiriss.wifilocalpositioning.data.models.general.Settings;

@Dao
public interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = :id")
    Settings findById(Long id);
    @Insert
    void insert(Settings settings);
}
