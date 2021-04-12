package com.mrkiriss.wifilocalpositioning.di.modules;

import android.content.Context;

import com.mrkiriss.wifilocalpositioning.managers.WifiScanner;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ManagersModule {

    @Provides
    @Singleton
    public WifiScanner provideWifiScanner(Context context){
        return new WifiScanner(context);
    }
}
