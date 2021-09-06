package com.mrkiriss.wifilocalpositioning.di.modules.main;

import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.data.repositiries.MainRepository;
import com.mrkiriss.wifilocalpositioning.viewmodel.MainViewModel;
import com.mrkiriss.wifilocalpositioning.utils.ScanningAbilitiesManager;

import dagger.Module;
import dagger.Provides;

@Module
public class MainRepositoryAndViewModelModule {

    @Provides
    @MainScope
    public MainRepository provideMainRepository(WifiScanner wifiScanner, SettingsManager settingsManager, ScanningAbilitiesManager abilitiesManager){
        return new MainRepository(wifiScanner, settingsManager, abilitiesManager);
    }

    @Provides
    @MainScope
    public MainViewModel provideMainViewModel(MainRepository repository){
        return new MainViewModel(repository);
    }
}
