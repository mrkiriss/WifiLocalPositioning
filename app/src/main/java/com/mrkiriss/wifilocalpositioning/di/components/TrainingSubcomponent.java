package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingRepositoryModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScope;
import com.mrkiriss.wifilocalpositioning.repositiries.TrainingRepository;
import com.mrkiriss.wifilocalpositioning.ui.viewmodel.TrainingViewModel;

import dagger.Component;
import dagger.Subcomponent;

@Subcomponent(modules = {TrainingRepositoryModule.class})
@TrainingScope
public interface TrainingSubcomponent {

    @Subcomponent.Builder
    interface Builder{
        TrainingSubcomponent.Builder repModule(TrainingRepositoryModule repositoryModule);
        TrainingSubcomponent build();
    }

    void inject(TrainingViewModel trainingViewModel);
}
