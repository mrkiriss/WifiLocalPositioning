package com.mrkiriss.wifilocalpositioning.data.sources.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.mrkiriss.wifilocalpositioning.data.models.server.ListOfAllMapPoints;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.models.settings.AbilitiesScanningData;
import com.mrkiriss.wifilocalpositioning.data.models.settings.Settings;

@Database(entities = {Settings.class, AbilitiesScanningData.class, LocationPointInfo.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SettingsDao settingDao();
    public abstract AbilitiesDao abilitiesDao();
    public abstract MapPointsDao mapPointsDao();
}
