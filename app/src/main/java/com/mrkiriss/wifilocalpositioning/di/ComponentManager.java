package com.mrkiriss.wifilocalpositioning.di;

import android.content.Context;

import com.mrkiriss.wifilocalpositioning.di.components.AppComponent;
import com.mrkiriss.wifilocalpositioning.di.components.DaggerAppComponent;
import com.mrkiriss.wifilocalpositioning.di.components.ViewModelSubcomponent;
import com.mrkiriss.wifilocalpositioning.di.modules.AppContextModule;

public class ComponentManager {

    private final AppComponent appComponent;
    private ViewModelSubcomponent viewModelSubcomponent;

    public ComponentManager(Context context){
        appComponent= DaggerAppComponent.builder()
                .appContextModule(new AppContextModule(context))
                .build();
    }

    public ViewModelSubcomponent getViewModelSubcomponent() {
        if (viewModelSubcomponent == null) {
            viewModelSubcomponent = appComponent.getViewModelSubcomponentBuilder()
                    .build();
        }
        return viewModelSubcomponent;
    }

}
