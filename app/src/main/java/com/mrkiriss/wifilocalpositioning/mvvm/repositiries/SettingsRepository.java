package com.mrkiriss.wifilocalpositioning.mvvm.repositiries;

import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;

import lombok.Data;

@Data
public class SettingsRepository {
    private final SettingsDao settingsDao;
    private final SettingsManager settingsManager;

    private final MutableLiveData<Integer> requiredToUpdateNumberOfScanning;
    private final MutableLiveData<String> requiredToUpdateScanInterval;
    private final MutableLiveData<String> toastContent;

    private int currentSavedScanInterval;
    private int currentSavedNumberOfScanning;

    public SettingsRepository(SettingsDao settingsDao, SettingsManager settingsManager){
        this.settingsDao=settingsDao;
        this.settingsManager=settingsManager;

        requiredToUpdateNumberOfScanning=new MutableLiveData<>();
        requiredToUpdateScanInterval=new MutableLiveData<>();
        toastContent=new MutableLiveData<>();

        updateDataFromSaved();
    }

    public void initSettingValuesFromDB(){
        requiredToUpdateScanInterval.setValue(String.valueOf(settingsManager.getScanInterval()));
        requiredToUpdateNumberOfScanning.setValue(settingsManager.getNumberOfScanning());
    }
    public void updateDataFromSaved(){
        currentSavedScanInterval=settingsManager.getScanInterval();
        currentSavedNumberOfScanning=settingsManager.getNumberOfScanning();
    }

    public void acceptSettingsChange(int scanInterval, int numberOfScanning){

        // check valid
        if (!isValidSettingsData(scanInterval, numberOfScanning)) return;

        // change manager data
        settingsManager.setScanInterval(scanInterval);
        settingsManager.setNumberOfScanning(numberOfScanning);

        updateDataFromSaved();

        settingsManager.saveChangedSettingsData(scanInterval, numberOfScanning);

        toastContent.setValue("Настройки успешно изменены");

        // вставляем те же данные, чтобы кнопка обновилась
        requiredToUpdateScanInterval.setValue(requiredToUpdateScanInterval.getValue());
    }
    private boolean isValidSettingsData(int scanInterval, int numberOfScanning){
        if (scanInterval<3 || numberOfScanning<0 || numberOfScanning>5){
            requiredToUpdateScanInterval.setValue(String.valueOf(settingsManager.defaultScanInterval));
            toastContent.setValue("Неккоректные данные");
            return false;
        }
        return true;
    }
}
