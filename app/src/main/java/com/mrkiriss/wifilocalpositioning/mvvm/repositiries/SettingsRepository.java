package com.mrkiriss.wifilocalpositioning.mvvm.repositiries;

import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.sources.SettingsManager;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;

import lombok.Data;

@Data
public class SettingsRepository {
    private final SettingsDao settingsDao;
    private final SettingsManager settingsManager;

    private final MutableLiveData<Integer> requiredToUpdateNumberOfScanning;
    private final MutableLiveData<String> requiredToUpdateScanInterval;
    private final MutableLiveData<String> toastContent;


    public SettingsRepository(SettingsDao settingsDao, SettingsManager settingsManager){
        this.settingsDao=settingsDao;
        this.settingsManager=settingsManager;

        requiredToUpdateNumberOfScanning=new MutableLiveData<>();
        requiredToUpdateScanInterval=new MutableLiveData<>();
        toastContent=new MutableLiveData<>();
    }

    // TODO: данные могут отрисоваться дефолтные, если настройки не успеют загрузиться в потоке
    public void initSettingValuesFromDB(){
        requiredToUpdateScanInterval.setValue(String.valueOf(settingsManager.getScanInterval()));
        requiredToUpdateNumberOfScanning.setValue(settingsManager.getNumberOfScanning());
    }

    public void acceptSettingsChange(int scanInterval, int numberOfScanning){

        // check valid
        if (!isValidSettingsData(scanInterval, numberOfScanning)) return;

        // change manager data
        settingsManager.setScanInterval(scanInterval);
        settingsManager.setNumberOfScanning(numberOfScanning);

        settingsManager.saveChangedSettingsData(scanInterval, numberOfScanning);

        toastContent.setValue("Настройки успешно изменены");
    }
    private boolean isValidSettingsData(int scanInterval, int numberOfScanning){
        if (scanInterval<3 || numberOfScanning<0 || numberOfScanning>5){
            toastContent.setValue("Неккоректные данные");
            return false;
        }
        return true;
    }
}
