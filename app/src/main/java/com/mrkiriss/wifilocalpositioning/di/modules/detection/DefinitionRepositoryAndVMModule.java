package com.mrkiriss.wifilocalpositioning.di.modules.detection;

import android.content.Context;

import com.mrkiriss.wifilocalpositioning.data.sources.api.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.db.MapPointsDao;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.data.repositiries.LocationDetectionRepository;
import com.mrkiriss.wifilocalpositioning.viewmodel.LocationDetectionViewModel;
import com.mrkiriss.wifilocalpositioning.utils.ConnectionManager;

import dagger.Module;
import dagger.Provides;

@Module
public class DefinitionRepositoryAndVMModule {

    @Provides
    @DefinitionScope
    public LocationDetectionViewModel provideLDVM(LocationDetectionRepository repository){
        return new LocationDetectionViewModel(repository);
    }

    @Provides
    @DefinitionScope
    public LocationDetectionRepository provideRepository(LocationDataApi retrofit, WifiScanner wifiScanner, ConnectionManager connectionManager, Context context, SettingsManager settingsManager, MapPointsDao mpDao){
        return new LocationDetectionRepository(retrofit, wifiScanner, connectionManager, new MapImageManager(context), settingsManager, mpDao);
    }
}
