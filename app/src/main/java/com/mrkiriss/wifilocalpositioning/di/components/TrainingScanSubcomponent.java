package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScanRepositoryModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScope;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.TrainingScanViewModel;

import dagger.Subcomponent;

@Subcomponent(modules = {TrainingScanRepositoryModule.class})
@TrainingScope
public interface TrainingScanSubcomponent {

    @Subcomponent.Builder
    interface Builder{
        TrainingScanSubcomponent.Builder repModule(TrainingScanRepositoryModule repositoryModule);
        TrainingScanSubcomponent build();
    }

    void inject(TrainingScanViewModel trainingScanViewModel);
}
