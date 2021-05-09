package com.mrkiriss.wifilocalpositioning.di.modules.settings;

import com.mrkiriss.wifilocalpositioning.data.sources.SettingsManager;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.SettingsRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingsRepModule {

    @Provides
    @SettingsScope
    public SettingsRepository provide(SettingsDao dao, SettingsManager settingsManager){
        return new SettingsRepository(dao, settingsManager);
    }
}
