package com.mrkiriss.wifilocalpositioning.data.sources.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.mrkiriss.wifilocalpositioning.data.models.settings.Settings;

@Database(entities = {Settings.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SettingsDao settingDao();
}
