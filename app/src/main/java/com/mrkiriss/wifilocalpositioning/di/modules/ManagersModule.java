package com.mrkiriss.wifilocalpositioning.di.modules;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.mrkiriss.wifilocalpositioning.data.models.preference.Settings;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.SettingsManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.db.AppDatabase;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ManagersModule {

    @Provides
    @Singleton
    public WifiScanner provideWifiScanner(Context context, AppDatabase db, SettingsManager settingsManager) {
        return new WifiScanner(context,
                (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE), db, settingsManager);
    }

    @Provides
    @Singleton
    public MapImageManager provideMapManager(Context context) {
        return new MapImageManager(context);
    }

    @Provides
    @Singleton
    public SettingsManager provideSettingsManager(SettingsDao dao) {
        return new SettingsManager(dao);
    }
}
