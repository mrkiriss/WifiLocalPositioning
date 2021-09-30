package com.mrkiriss.wifilocalpositioning.data.repositiries;

import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;

import javax.inject.Inject;

import lombok.Data;

@Data
public class SettingsRepository {
    private final SettingsDao settingsDao;
    private final SettingsManager settingsManager;

    private final MutableLiveData<Integer> requestToUpdateNumberOfScanning;
    private final MutableLiveData<String> requestToUpdateScanInterval;
    private final MutableLiveData<Integer> requestToUpdateAccessLevel;
    private final MutableLiveData<String> requestToUpdateCopyUUID;
    private final MutableLiveData<String> toastContent;

    private int currentSavedScanInterval;
    private int currentSavedNumberOfScanning;

    private int numberOfTryingToCopy;
    private final int requiredNumberOfTryingToCopy=7;

    @Inject
    public SettingsRepository(SettingsDao settingsDao, SettingsManager settingsManager){
        this.settingsDao=settingsDao;
        this.settingsManager=settingsManager;

        requestToUpdateNumberOfScanning =new MutableLiveData<>();
        requestToUpdateScanInterval =new MutableLiveData<>();
        requestToUpdateAccessLevel =settingsManager.getRequestToUpdateAccessLevel();
        toastContent=new MutableLiveData<>();
        requestToUpdateCopyUUID =new MutableLiveData<>();

        updateDataFromSaved();
    }

    public void updateSettingValuesFromDB(){
        requestToUpdateScanInterval.setValue(String.valueOf(settingsManager.getScanInterval()));
        requestToUpdateNumberOfScanning.setValue(settingsManager.getNumberOfScanning());
        requestToUpdateAccessLevel.setValue(settingsManager.getAccessLevel());
    }
    public void updateDataFromSaved(){
        currentSavedScanInterval=settingsManager.getScanInterval();
        currentSavedNumberOfScanning=settingsManager.getNumberOfScanning();
    }
    public void requestToUpdateAccessLevel(){
        settingsManager.checkAccessLevel();
    }
    public void requestToGetUUID(){
        numberOfTryingToCopy++;
        if (numberOfTryingToCopy==requiredNumberOfTryingToCopy){
            requestToUpdateCopyUUID.setValue(settingsManager.getUUID());
            numberOfTryingToCopy=0;
        }
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
        requestToUpdateScanInterval.setValue(String.valueOf(currentSavedScanInterval));
    }
    private boolean isValidSettingsData(int scanInterval, int numberOfScanning){
        if (scanInterval<3 || numberOfScanning<0 || numberOfScanning>5){
            requestToUpdateScanInterval.setValue(String.valueOf(settingsManager.defaultScanInterval));
            toastContent.setValue("Неккоректные данные");
            return false;
        }
        return true;
    }
}
