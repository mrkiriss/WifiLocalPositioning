package com.mrkiriss.wifilocalpositioning.di.modules.training;

import com.mrkiriss.wifilocalpositioning.managers.WifiScanner;
import com.mrkiriss.wifilocalpositioning.network.IMWifiServerApi;
import com.mrkiriss.wifilocalpositioning.repositiries.TrainingRepository;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module
public class TrainingRepositoryModule {

    @Provides
    @TrainingScope
    public TrainingRepository provideRepository(IMWifiServerApi retrofit, WifiScanner wifiScanner){
        return new TrainingRepository(retrofit, wifiScanner);
    }
}
