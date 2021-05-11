package com.mrkiriss.wifilocalpositioning.di.modules.definition;

import android.content.Context;

import com.mrkiriss.wifilocalpositioning.data.sources.api.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.LocationDetectionRepository;
import com.mrkiriss.wifilocalpositioning.utils.ConnectionManager;

import dagger.Module;
import dagger.Provides;

@Module
public class DefinitionRepositoryModule {
    @Provides
    @DefinitionScope
    public LocationDetectionRepository provideRepository(LocationDataApi retrofit, WifiScanner wifiScanner, ConnectionManager connectionManager, Context context, SettingsManager settingsManager){
        return new LocationDetectionRepository(retrofit, wifiScanner, connectionManager, new MapImageManager(context), settingsManager);
    }
}
