package com.mrkiriss.wifilocalpositioning.di.modules;

import android.content.Context;

import androidx.room.Room;

import com.mrkiriss.wifilocalpositioning.data.sources.db.AppDatabase;
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
                .build();
    }

    @Provides
    @Singleton
    SettingsDao provideSettingsDao(AppDatabase db){
        return db.settingDao();
    }
}
