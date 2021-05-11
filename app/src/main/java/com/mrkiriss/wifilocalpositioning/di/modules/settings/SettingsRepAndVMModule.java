package com.mrkiriss.wifilocalpositioning.di.modules.settings;

import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.SettingsRepository;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.SettingsViewModel;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingsRepAndVMModule {

    @Provides
    @SettingsScope
    public SettingsRepository provide(SettingsDao dao, SettingsManager settingsManager){
        return new SettingsRepository(dao, settingsManager);
    }

    @Provides
    @SettingsScope
    public SettingsViewModel provideSettingsViewModel(SettingsRepository repository){
        return new SettingsViewModel(repository);
    }
}
