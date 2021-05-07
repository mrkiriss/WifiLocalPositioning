package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.settings.SettingsRepModule;
import com.mrkiriss.wifilocalpositioning.di.modules.settings.SettingsScope;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.SettingsViewModel;

import dagger.Subcomponent;

@Subcomponent(modules = {SettingsRepModule.class})
@SettingsScope
public interface SettingsSubcomponent {
    @Subcomponent.Builder
    interface Builder{
        SettingsSubcomponent.Builder setRepositoryModule(SettingsRepModule module);
        SettingsSubcomponent build();
    }

    void inject(SettingsViewModel viewModel);
}
