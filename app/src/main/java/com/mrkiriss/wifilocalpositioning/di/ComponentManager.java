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
import com.mrkiriss.wifilocalpositioning.di.modules.definition.DefinitionRepositoryModule;
import com.mrkiriss.wifilocalpositioning.di.modules.settings.SettingsRepModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingMapRepModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScanRepositoryModule;

public class ComponentManager {

    private AppComponent appComponent;
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

    public TrainingScanSubcomponent getTrainingScanSubcomponent(){
        if (trainingScanSubcomponent ==null){
            trainingScanSubcomponent =appComponent.trainingSubcomponentBuilder()
                    .repModule(new TrainingScanRepositoryModule())
                    .build();
        }
        return trainingScanSubcomponent;
    }

    public LocationDetectionSubcomponent getLocationDetectionSubcomponent(){
        if (locationDetectionSubcomponent==null){
            locationDetectionSubcomponent=appComponent.locationDetectionSubcomponentBuilder()
                    .repModule(new DefinitionRepositoryModule())
                    .build();
        }
        return locationDetectionSubcomponent;
    }

    public MainActivitySubcomponent getMainActivitySubcomponent() {
        if (mainActivitySubcomponent == null) {
            mainActivitySubcomponent = appComponent.mainActivitySubcomponentBuilder()
                    .build();
        }
        return mainActivitySubcomponent;
    }

    public TrainingMapSubcomponent getTrainingMapSubcomponent() {
        if (trainingMapSubcomponent == null) {
            trainingMapSubcomponent = appComponent.trainingMapSubcomponent()
                    .repModule(new TrainingMapRepModule())
                    .build();
        }
        return trainingMapSubcomponent;
    }

    public SettingsSubcomponent getSettingsSubcomponent() {
        if (settingsSubcomponent == null) {
            settingsSubcomponent = appComponent.settingsSubcomponent()
                    .setRepositoryModule(new SettingsRepModule())
                    .build();
        }
        return settingsSubcomponent;
    }

}
