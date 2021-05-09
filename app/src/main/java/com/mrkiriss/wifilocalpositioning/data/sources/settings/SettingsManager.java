package com.mrkiriss.wifilocalpositioning.data.sources.settings;

import android.util.Log;

import com.mrkiriss.wifilocalpositioning.data.models.settings.Settings;
import com.mrkiriss.wifilocalpositioning.data.sources.api.AccessLevelApi;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;

import org.jetbrains.annotations.NotNull;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
public class SettingsManager {
    private SettingsDao settingsDao;
    private UUIDManager uuidManager;
    private AccessLevelApi accessLevelApi;

    private int scanInterval=-1;
    private int numberOfScanning=-1;
    private String UUID;
    private Integer accessLevel;

    private final Long settingID=0L;

    public final int defaultScanInterval=15; // in seconds
    public final int defaultNumberOfScanning = 1;
    public final String[] keys = new String[]{"scanInterval", "variousOfNumberScans"};

    // проверка, для существования только одного запроса в определённый момент
    private boolean checkSettingsNow;

    public SettingsManager(SettingsDao settingsDao, UUIDManager uuidManager, AccessLevelApi accessLevelApi){
        this.settingsDao=settingsDao;
        this.uuidManager=uuidManager;
        this.accessLevelApi=accessLevelApi;

        checkSettingsNow=false;

        checkFirstGettingSettings();
    }

    public void checkFirstGettingSettings(){
        Runnable task= ()-> {
            checkSettingsNow=true;

            Settings currentSettings = settingsDao.findById(settingID);
            if (currentSettings == null) {
                Settings settings = new Settings();
                settings.setId(settingID);
                settings.setScanInterval(defaultScanInterval);
                settings.setNumberOfScans(defaultNumberOfScanning);
                settings.setUUID(uuidManager.getMUUID());
                settingsDao.insert(settings);
                currentSettings=settings;
            }

            scanInterval=currentSettings.getScanInterval();
            numberOfScanning=currentSettings.getNumberOfScans();
            UUID=currentSettings.getUUID();

            checkSettingsNow=false;

            checkAccessLevel();
        };
        new Thread(task).start();
    }
    public void checkAccessLevel(){
        Log.i("SettingsManager", "start define access level with uuid="+UUID);
        accessLevelApi.getAccessLevel(UUID).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NotNull Call<Integer> call, @NotNull Response<Integer> response) {
                if (response.body()==null){
                    Log.i("SettingsManager", "response after get accessLevel is null");
                }else{
                    accessLevel=response.body();
                    Log.i("SettingsManager", "response after get accessLevel="+accessLevel);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void saveChangedSettingsData(int scanInterval, int numberOfScanning){
        Runnable task = ()-> {
            Settings settings = new Settings();

            // change data
            settings.setId(settingID);
            settings.setScanInterval(scanInterval);
            settings.setNumberOfScans(numberOfScanning);
            settings.setUUID(UUID);

            // save settings
            settingsDao.insert(settings);
        };
        new Thread(task).start();
    }

    // собственные гетеры, чтобы вызывать обновление при требовании при отсутсвии данных
    public int getScanInterval(){
        if (scanInterval==-1 && !checkSettingsNow){
            checkFirstGettingSettings();
            return defaultScanInterval;
        }
        return scanInterval;
    }
    public int getNumberOfScanning(){
        if (numberOfScanning==-1 && !checkSettingsNow){
            checkFirstGettingSettings();
            return defaultNumberOfScanning;
        }
        return numberOfScanning;
    }
    public int getAccessLevel(){
        if (UUID==null){
            checkFirstGettingSettings();
            return 0;
        }
        if (accessLevel==null){
            checkAccessLevel();
            return 0;
        }
        return accessLevel;
    }
}
