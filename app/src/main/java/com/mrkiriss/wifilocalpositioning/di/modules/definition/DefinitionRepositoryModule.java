package com.mrkiriss.wifilocalpositioning.di.modules.definition;

import android.content.Context;

import com.mrkiriss.wifilocalpositioning.data.FloorSchemasDownloader;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScope;
import com.mrkiriss.wifilocalpositioning.managers.WifiScanner;
import com.mrkiriss.wifilocalpositioning.network.IMWifiServerApi;
import com.mrkiriss.wifilocalpositioning.repositiries.LocationDetectionRepository;
import com.mrkiriss.wifilocalpositioning.repositiries.TrainingRepository;
import com.mrkiriss.wifilocalpositioning.ui.viewmodel.LocationDetectionViewModel;

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
