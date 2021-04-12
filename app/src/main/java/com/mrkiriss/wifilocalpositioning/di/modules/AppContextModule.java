package com.mrkiriss.wifilocalpositioning.di.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppContextModule {

    private Context appContext;

    public AppContextModule(Context context){
        appContext=context;
    }

    @Singleton
    @Provides
    Context provideAppContext(){
        return appContext;
    }
}