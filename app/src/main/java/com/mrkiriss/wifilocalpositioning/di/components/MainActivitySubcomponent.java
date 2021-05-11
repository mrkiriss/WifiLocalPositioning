package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.main.MainRepositoryAndViewModelModule;
import com.mrkiriss.wifilocalpositioning.di.modules.main.MainScope;
import com.mrkiriss.wifilocalpositioning.mvvm.view.MainActivity;

import javax.inject.Singleton;

import dagger.Subcomponent;

@Subcomponent(modules = {MainRepositoryAndViewModelModule.class})
@MainScope
public interface MainActivitySubcomponent {
    void inject(MainActivity mainActivity);

    @Subcomponent.Builder
    interface Builder{
        MainActivitySubcomponent.Builder repAndVMModule(MainRepositoryAndViewModelModule module);
        MainActivitySubcomponent build();
    }
}
