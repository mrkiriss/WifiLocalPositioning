package com.mrkiriss.wifilocalpositioning.di.modules;

import android.content.Context;
import android.location.LocationManager;
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
    public ConnectivityManager provideConnectivityManager(Context context){
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    @Provides
    @Singleton
    public WifiManager provideWifiManager(Context context){
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }
    @Provides
    @Singleton
    public LocationManager provideLocationManager(Context context){
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
}
