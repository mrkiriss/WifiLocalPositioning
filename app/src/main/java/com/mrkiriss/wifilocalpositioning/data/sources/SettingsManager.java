package com.mrkiriss.wifilocalpositioning.data.sources;

import com.mrkiriss.wifilocalpositioning.data.models.preference.Settings;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;

import java.util.Set;

import lombok.Data;

@Data
public class SettingsManager {
    private SettingsDao settingsDao;

    private int scanInterval=-1;
    private int numberOfScanning=-1;
    private boolean isAdmin=true;

    private final Long settingID=0L;

    public final int defaultScanInterval=5; // in seconds
    public final int defaultNumberOfScanning = 1;
    public final String[] keys = new String[]{"scanInterval", "variousOfNumberScans"};

    // проверка, для единоразовой проверки
    private boolean checkSettingsNow;

    public SettingsManager(SettingsDao settingsDao){
        this.settingsDao=settingsDao;

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
                settingsDao.insert(settings);
                currentSettings=settings;
            }

            scanInterval=currentSettings.getScanInterval();
            numberOfScanning=currentSettings.getNumberOfScans();

            checkSettingsNow=false;
        };
        new Thread(task).start();
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
}
