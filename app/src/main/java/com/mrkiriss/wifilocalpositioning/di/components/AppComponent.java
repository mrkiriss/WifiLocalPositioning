package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.AppContextModule;
import com.mrkiriss.wifilocalpositioning.di.modules.DatabaseModule;
import com.mrkiriss.wifilocalpositioning.di.modules.ManagersModule;
import com.mrkiriss.wifilocalpositioning.di.modules.NetworkModule;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppContextModule.class, NetworkModule.class, ManagersModule.class, DatabaseModule.class})
@Singleton
public interface AppComponent {
    ViewModelSubcomponent.Builder getViewModelSubcomponentBuilder();
}