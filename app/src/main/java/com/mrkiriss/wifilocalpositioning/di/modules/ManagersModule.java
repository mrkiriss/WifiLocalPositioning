package com.mrkiriss.wifilocalpositioning.di.modules;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.db.AbilitiesDao;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;
import com.mrkiriss.wifilocalpositioning.data.sources.remote.AccessLevelApi;
import com.mrkiriss.wifilocalpositioning.data.sources.remote.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.UUIDManager;
import com.mrkiriss.wifilocalpositioning.utils.ConnectionManager;
import com.mrkiriss.wifilocalpositioning.utils.ScanningAbilitiesManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ManagersModule {

    @Provides
    @Singleton
    public WifiScanner provideWifiScanner(Context context, ConnectionManager connectionManager, SettingsManager settingsManager ) {
        return new WifiScanner(context,
                (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE), connectionManager, settingsManager);
    }
    @Provides
    @Singleton
    public ScanningAbilitiesManager provideScanningAbilitiesManager(AbilitiesDao abilitiesDao, LocationDataApi api) {
        return new ScanningAbilitiesManager(abilitiesDao, api);
    }

    @Provides
    @Singleton
    public MapImageManager provideMapManager(Context context) {
        return new MapImageManager(context);
    }

    @Provides
    @Singleton
    public SettingsManager provideSettingsManager(SettingsDao dao, UUIDManager uuidManager, AccessLevelApi api) {
        return new SettingsManager(dao, uuidManager, api);
    }
    @Provides
    @Singleton
    public UUIDManager provideUUIDManager(Context context) {
        return new UUIDManager(context);
    }

    @Provides
    @Singleton
    public ConnectionManager provideConnectionManager(ConnectivityManager manager, WifiManager wifiManager) {
        return new ConnectionManager(manager, wifiManager);
    }

    @Provides
    @Singleton
    public ConnectivityManager provideConnectivityManager(Context context){
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    @Provides
    @Singleton
    public WifiManager provideWifiManager(Context context){
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }
}
