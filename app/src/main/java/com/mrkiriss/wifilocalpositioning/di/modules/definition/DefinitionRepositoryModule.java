package com.mrkiriss.wifilocalpositioning.di.modules.definition;

import android.content.Context;

import com.mrkiriss.wifilocalpositioning.data.sources.FloorSchemasDownloader;
import com.mrkiriss.wifilocalpositioning.data.sources.wifi.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.IMWifiServerApi;
import com.mrkiriss.wifilocalpositioning.repositiries.LocationDetectionRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class DefinitionRepositoryModule {
    @Provides
    @DefinitionScope
    public LocationDetectionRepository provideRepository(IMWifiServerApi retrofit, WifiScanner wifiScanner, Context context){
        return new LocationDetectionRepository(retrofit, wifiScanner, new FloorSchemasDownloader(context));
    }
}
