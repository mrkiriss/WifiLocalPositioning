package com.mrkiriss.wifilocalpositioning.di;

import android.app.Application;

public class App extends Application {

    private static App instance;
    private ComponentManager componentManager;

    public static App getInstance(){
        return instance;
    }

    public ComponentManager getComponentManager(){
        return componentManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        componentManager=new ComponentManager(this);
    }
}
