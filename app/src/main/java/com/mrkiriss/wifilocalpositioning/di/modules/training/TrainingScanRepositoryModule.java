package com.mrkiriss.wifilocalpositioning.di.modules.training;

import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.IMWifiServerApi;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.TrainingScanRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class TrainingScanRepositoryModule {

    @Provides
    @TrainingScope
    public TrainingScanRepository provideRepository(IMWifiServerApi retrofit, WifiScanner wifiScanner){
        return new TrainingScanRepository(retrofit, wifiScanner);
    }
}
