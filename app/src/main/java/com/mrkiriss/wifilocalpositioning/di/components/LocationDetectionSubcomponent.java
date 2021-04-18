package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.definition.DefinitionRepositoryModule;
import com.mrkiriss.wifilocalpositioning.di.modules.definition.DefinitionScope;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingRepositoryModule;
import com.mrkiriss.wifilocalpositioning.ui.viewmodel.LocationDetectionViewModel;
import com.mrkiriss.wifilocalpositioning.ui.viewmodel.TrainingViewModel;

import dagger.Subcomponent;

@Subcomponent(modules = {DefinitionRepositoryModule.class})
@DefinitionScope
public interface LocationDetectionSubcomponent {
    @Subcomponent.Builder
    interface Builder{
        LocationDetectionSubcomponent.Builder repModule(DefinitionRepositoryModule module);
        LocationDetectionSubcomponent build();
    }

    void inject(LocationDetectionViewModel locationDetectionViewModel);
}
