package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingMapRepModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScope;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.TrainingMapViewModel;

import dagger.Subcomponent;

@Subcomponent(modules = {TrainingMapRepModule.class})
@TrainingScope
public interface TrainingMapSubcomponent {
    @Subcomponent.Builder
    interface Builder{
        TrainingMapSubcomponent.Builder repModule(TrainingMapRepModule module);
        TrainingMapSubcomponent build();
    }

    void inject(TrainingMapViewModel trainingMapViewModel);
}
