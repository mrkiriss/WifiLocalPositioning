package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingMapRepAndVMModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScope;
import com.mrkiriss.wifilocalpositioning.mvvm.view.TrainingMapFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {TrainingMapRepAndVMModule.class})
@TrainingScope
public interface TrainingMapSubcomponent {
    @Subcomponent.Builder
    interface Builder{
        TrainingMapSubcomponent.Builder repModule(TrainingMapRepAndVMModule module);
        TrainingMapSubcomponent build();
    }

    void inject(TrainingMapFragment fragment);
}
