package com.mrkiriss.wifilocalpositioning.di.modules.detection;

import androidx.fragment.app.Fragment;

import com.mrkiriss.wifilocalpositioning.data.sources.FragmentsFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentsFactoryModule {

    @Provides
    FragmentsFactory provideFragmentsFactory() {
        return new FragmentsFactory();
    }
}
