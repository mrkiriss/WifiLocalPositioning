package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.data.sources.FragmentsFactory;
import com.mrkiriss.wifilocalpositioning.di.modules.detection.FragmentsFactoryModule;
import com.mrkiriss.wifilocalpositioning.di.modules.main.MainRepositoryAndViewModelModule;
import com.mrkiriss.wifilocalpositioning.di.modules.main.MainScope;
import com.mrkiriss.wifilocalpositioning.view.MainActivity;

import dagger.Subcomponent;

@Subcomponent(modules = {MainRepositoryAndViewModelModule.class, FragmentsFactoryModule.class})
@MainScope
public interface MainActivitySubcomponent {
    void inject(MainActivity mainActivity);

    @Subcomponent.Builder
    interface Builder{
        MainActivitySubcomponent.Builder repAndVMModule(MainRepositoryAndViewModelModule module);
        MainActivitySubcomponent.Builder fragmentsBuilderModule(FragmentsFactoryModule module);
        MainActivitySubcomponent build();
    }
}
