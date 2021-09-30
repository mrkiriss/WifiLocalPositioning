package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.annotations.ActivityScope;
import com.mrkiriss.wifilocalpositioning.di.modules.ViewModelModule;
import com.mrkiriss.wifilocalpositioning.view.LocationDetectionFragment;
import com.mrkiriss.wifilocalpositioning.view.MainActivity;
import com.mrkiriss.wifilocalpositioning.view.SearchFragment;
import com.mrkiriss.wifilocalpositioning.view.SettingsFragment;
import com.mrkiriss.wifilocalpositioning.view.TrainingMapFragment;
import com.mrkiriss.wifilocalpositioning.view.TrainingScanFragment;

import dagger.Subcomponent;

@Subcomponent(modules = {ViewModelModule.class})
@ActivityScope
public interface ViewModelSubcomponent {
    @Subcomponent.Builder
    interface Builder {
        ViewModelSubcomponent build();
    }

    void inject(MainActivity activity);
    void inject(LocationDetectionFragment fragment);
    void inject(SearchFragment fragment);
    void inject(TrainingMapFragment fragment);
    void inject(TrainingScanFragment fragment);
    void inject(SettingsFragment fragment);
}
