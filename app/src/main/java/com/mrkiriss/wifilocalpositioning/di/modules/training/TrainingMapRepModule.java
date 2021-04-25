package com.mrkiriss.wifilocalpositioning.di.modules.training;

import com.mrkiriss.wifilocalpositioning.data.sources.IMWifiServerApi;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.TrainingMapRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class TrainingMapRepModule {
    @Provides
    @TrainingScope
    public TrainingMapRepository provideRepository(IMWifiServerApi retrofit, MapImageManager mapImageManager){
        return new TrainingMapRepository(retrofit, mapImageManager);
    }
}
