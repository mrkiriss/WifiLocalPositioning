package com.mrkiriss.wifilocalpositioning.di;

import android.content.Context;

import com.mrkiriss.wifilocalpositioning.di.components.AppComponent;
import com.mrkiriss.wifilocalpositioning.di.components.DaggerAppComponent;
import com.mrkiriss.wifilocalpositioning.di.components.LocationDetectionSubcomponent;
import com.mrkiriss.wifilocalpositioning.di.components.TrainingSubcomponent;
import com.mrkiriss.wifilocalpositioning.di.modules.AppContextModule;
import com.mrkiriss.wifilocalpositioning.di.modules.definition.DefinitionRepositoryModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingRepositoryModule;

public class ComponentManager {

    private AppComponent appComponent;
    private TrainingSubcomponent trainingSubcomponent;
    private LocationDetectionSubcomponent locationDetectionSubcomponent;

    public ComponentManager(Context context){
        appComponent= DaggerAppComponent.builder()
                .appContextModule(new AppContextModule(context))
                .build();
    }

    public TrainingSubcomponent getTrainingSubcomponent(){
        if (trainingSubcomponent==null){
            trainingSubcomponent=appComponent.trainingSubcomponentBuilder()
                    .repModule(new TrainingRepositoryModule())
                    .build();
        }
        return trainingSubcomponent;
    }

    public LocationDetectionSubcomponent getLocationDetectionSubcomponent(){
        if (locationDetectionSubcomponent==null){
            locationDetectionSubcomponent=appComponent.locationDetectionSubcomponentBuilder()
                    .repModule(new DefinitionRepositoryModule())
                    .build();
        }
        return locationDetectionSubcomponent;
    }

}
