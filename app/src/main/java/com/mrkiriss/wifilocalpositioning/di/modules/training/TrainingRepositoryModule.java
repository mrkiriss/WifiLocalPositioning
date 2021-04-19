package com.mrkiriss.wifilocalpositioning.di.modules.training;

import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.IMWifiServerApi;
import com.mrkiriss.wifilocalpositioning.repositiries.TrainingRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class TrainingRepositoryModule {

    @Provides
    @TrainingScope
    public TrainingRepository provideRepository(IMWifiServerApi retrofit, WifiScanner wifiScanner){
        return new TrainingRepository(retrofit, wifiScanner);
    }
}
