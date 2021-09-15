package com.mrkiriss.wifilocalpositioning.di;

import android.content.Context;

import com.mrkiriss.wifilocalpositioning.di.components.AppComponent;
import com.mrkiriss.wifilocalpositioning.di.components.DaggerAppComponent;
import com.mrkiriss.wifilocalpositioning.di.components.LocationDetectionSubcomponent;
import com.mrkiriss.wifilocalpositioning.di.components.MainActivitySubcomponent;
import com.mrkiriss.wifilocalpositioning.di.components.SettingsSubcomponent;
import com.mrkiriss.wifilocalpositioning.di.components.TrainingMapSubcomponent;
import com.mrkiriss.wifilocalpositioning.di.components.TrainingScanSubcomponent;
import com.mrkiriss.wifilocalpositioning.di.modules.AppContextModule;
import com.mrkiriss.wifilocalpositioning.di.modules.detection.DefinitionRepositoryAndVMModule;
import com.mrkiriss.wifilocalpositioning.di.modules.main.MainRepositoryAndViewModelModule;
import com.mrkiriss.wifilocalpositioning.di.modules.settings.SettingsRepAndVMModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingMapRepAndVMModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScanRepAndVMModule;

public class ComponentManager {

    private final AppComponent appComponent;
    private MainActivitySubcomponent mainActivitySubcomponent;
    private TrainingScanSubcomponent trainingScanSubcomponent;
    private LocationDetectionSubcomponent locationDetectionSubcomponent;
    private TrainingMapSubcomponent trainingMapSubcomponent;
    private SettingsSubcomponent settingsSubcomponent;

    public ComponentManager(Context context){
        appComponent= DaggerAppComponent.builder()
                .appContextModule(new AppContextModule(context))
                .build();
    }

    public MainActivitySubcomponent getMainActivitySubcomponent() {
        if (mainActivitySubcomponent == null) {
            mainActivitySubcomponent = appComponent.mainActivitySubcomponentBuilder()
                    .repAndVMModule(new MainRepositoryAndViewModelModule())
                    .build();
        }
        return mainActivitySubcomponent;
    }

    public TrainingScanSubcomponent getTrainingScanSubcomponent(){
        if (trainingScanSubcomponent ==null){
            trainingScanSubcomponent =appComponent.trainingSubcomponentBuilder()
                    .repModule(new TrainingScanRepAndVMModule())
                    .build();
        }
        return trainingScanSubcomponent;
    }

    public LocationDetectionSubcomponent getLocationDetectionSubcomponent(){
        if (locationDetectionSubcomponent==null){
            locationDetectionSubcomponent=appComponent.locationDetectionSubcomponentBuilder()
                    .repModule(new DefinitionRepositoryAndVMModule())
                    .build();
        }
        return locationDetectionSubcomponent;
    }

    public TrainingMapSubcomponent getTrainingMapSubcomponent() {
        if (trainingMapSubcomponent == null) {
            trainingMapSubcomponent = appComponent.trainingMapSubcomponent()
                    .repModule(new TrainingMapRepAndVMModule())
                    .build();
        }
        return trainingMapSubcomponent;
    }

    public SettingsSubcomponent getSettingsSubcomponent() {
        if (settingsSubcomponent == null) {
            settingsSubcomponent = appComponent.settingsSubcomponent()
                    .setRepositoryModule(new SettingsRepAndVMModule())
                    .build();
        }
        return settingsSubcomponent;
    }

}
