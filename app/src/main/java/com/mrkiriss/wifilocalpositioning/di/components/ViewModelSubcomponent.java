package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.annotations.ActivityScope;
import com.mrkiriss.wifilocalpositioning.di.modules.ViewModelModule;
import com.mrkiriss.wifilocalpositioning.ui.detection.LocationDetectionFragment;
import com.mrkiriss.wifilocalpositioning.ui.MainActivity;
import com.mrkiriss.wifilocalpositioning.ui.search.SearchFragment;
import com.mrkiriss.wifilocalpositioning.ui.settings.SettingsFragment;
import com.mrkiriss.wifilocalpositioning.ui.training.TrainingMapFragment;
import com.mrkiriss.wifilocalpositioning.ui.scanconsole.TrainingScanFragment;

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
