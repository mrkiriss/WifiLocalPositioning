package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.settings.SettingsRepAndVMModule;
import com.mrkiriss.wifilocalpositioning.di.modules.settings.SettingsScope;
import com.mrkiriss.wifilocalpositioning.mvvm.view.SettingsFragment;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.SettingsViewModel;

import dagger.Subcomponent;

@Subcomponent(modules = {SettingsRepAndVMModule.class})
@SettingsScope
public interface SettingsSubcomponent {
    @Subcomponent.Builder
    interface Builder{
        SettingsSubcomponent.Builder setRepositoryModule(SettingsRepAndVMModule module);
        SettingsSubcomponent build();
    }

    void inject(SettingsFragment fragment);
}
