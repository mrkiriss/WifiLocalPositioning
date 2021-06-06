package com.mrkiriss.wifilocalpositioning.di.modules;

import android.content.Context;

import androidx.room.Room;

import com.mrkiriss.wifilocalpositioning.data.sources.db.AbilitiesDao;
import com.mrkiriss.wifilocalpositioning.data.sources.db.AppDatabase;
import com.mrkiriss.wifilocalpositioning.data.sources.db.MapPointsDao;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {
    @Provides
    @Singleton
    AppDatabase provideAppDB(Context context){
        return Room.databaseBuilder(context, AppDatabase.class, "database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    SettingsDao provideSettingsDao(AppDatabase db){
        return db.settingDao();
    }

    @Provides
    @Singleton
    AbilitiesDao provideAbilitiesDao(AppDatabase db){
        return db.abilitiesDao();
    }

    @Provides
    @Singleton
    MapPointsDao provideMapPointsDao(AppDatabase db){
        return db.mapPointsDao();
    }
}
