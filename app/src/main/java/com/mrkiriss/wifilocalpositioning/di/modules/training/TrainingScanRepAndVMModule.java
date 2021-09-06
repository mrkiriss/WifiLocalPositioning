package com.mrkiriss.wifilocalpositioning.di.modules.training;

import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.api.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.data.repositiries.TrainingScanRepository;
import com.mrkiriss.wifilocalpositioning.viewmodel.TrainingScanViewModel;

import dagger.Module;
import dagger.Provides;

@Module
public class TrainingScanRepAndVMModule {

    @Provides
    @TrainingScope
    public TrainingScanRepository provideRepository(LocationDataApi retrofit, WifiScanner wifiScanner){
        return new TrainingScanRepository(retrofit, wifiScanner);
    }

    @Provides
    @TrainingScope
    public TrainingScanViewModel provideTrainingScanViewModel(TrainingScanRepository repository){
        return new TrainingScanViewModel(repository);
    }
}
