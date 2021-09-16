package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.detection.DefinitionRepositoryAndVMModule;
import com.mrkiriss.wifilocalpositioning.di.modules.detection.DefinitionScope;
import com.mrkiriss.wifilocalpositioning.di.modules.search.SearchRepositoryAndVM;
import com.mrkiriss.wifilocalpositioning.view.LocationDetectionFragment;
import com.mrkiriss.wifilocalpositioning.view.SearchFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {SearchRepositoryAndVM.class})
@DefinitionScope
public interface SearchSubcomponent {
    @Subcomponent.Builder
    interface Builder{
        SearchSubcomponent.Builder repModule(SearchRepositoryAndVM module);
        SearchSubcomponent build();
    }

    void inject(SearchFragment fragment);
}
