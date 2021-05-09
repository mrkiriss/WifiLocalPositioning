package com.mrkiriss.wifilocalpositioning.data.sources;

import android.content.Context;
import android.util.Log;

import com.mrkiriss.wifilocalpositioning.data.models.settings.Settings;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
    private Boolean isAdmin=true;

    private final Long settingID=0L;

    public final int defaultScanInterval=5; // in seconds
    public final int defaultNumberOfScanning = 1;
    public final String[] keys = new String[]{"scanInterval", "variousOfNumberScans"};

    // проверка, для существования только одного запроса в определённый момент
    private boolean checkSettingsNow;
    // проверка, для существования только одного запроса в определённый момент
    private boolean checkAccessLevelNow;


    public SettingsManager(SettingsDao settingsDao, UUIDManager uuidManager, AccessLevelApi accessLevelApi){
        this.settingsDao=settingsDao;
        this.uuidManager=uuidManager;
        this.accessLevelApi=accessLevelApi;

        checkSettingsNow=false;
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

            checkSettingsNow=false;
        };
        new Thread(task).start();
    }
    public void checkAccessLevel(){
        accessLevelApi.getAccessLevel(UUID).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NotNull Call<Boolean> call, @NotNull Response<Boolean> response) {
                if (response.body()==null){
                    Log.i("SettingsManager", "response after get accessLevel is null");
                    isAdmin=null; // будет вызвана перепроверка
                }else{
                    isAdmin=response.body();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                t.printStackTrace();
                isAdmin=null; // будет вызвана перепроверка
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
    public boolean isAdmin(){
        if (isAdmin==null && !checkAccessLevelNow){
            checkAccessLevel();
            return false;
        }
        return isAdmin != null && isAdmin;
    }
}
