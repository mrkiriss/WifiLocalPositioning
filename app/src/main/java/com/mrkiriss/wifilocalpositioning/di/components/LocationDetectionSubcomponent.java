package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.detection.DefinitionRepositoryAndVMModule;
import com.mrkiriss.wifilocalpositioning.di.modules.detection.DefinitionScope;
import com.mrkiriss.wifilocalpositioning.view.LocationDetectionFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {DefinitionRepositoryAndVMModule.class})
@DefinitionScope
public interface LocationDetectionSubcomponent {
    @Subcomponent.Builder
    interface Builder{
        LocationDetectionSubcomponent.Builder repModule(DefinitionRepositoryAndVMModule module);
        LocationDetectionSubcomponent build();
    }

    void inject(LocationDetectionFragment fragment);
}
