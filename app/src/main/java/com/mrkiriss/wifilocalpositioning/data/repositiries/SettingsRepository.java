package com.mrkiriss.wifilocalpositioning.data.repositiries;

import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.utils.LiveData.SingleLiveEvent;

import javax.inject.Inject;

import lombok.Data;

@Data
public class SettingsRepository {
    private final SettingsDao settingsDao;
    private final SettingsManager settingsManager;

    private final SingleLiveEvent<Integer> requestToUpdateNumberOfScanning;
    private final SingleLiveEvent<String> requestToUpdateScanInterval;
    private final MutableLiveData<Integer> requestToUpdateAccessLevel;
    private final SingleLiveEvent<String> requestToUpdateCopyUUID;
    private final SingleLiveEvent<String> toastContent;

    private int currentSavedScanInterval;
    private int currentSavedNumberOfScanning;

    private int numberOfTryingToCopy;
    private final int requiredNumberOfTryingToCopy=7;

    @Inject
    public SettingsRepository(SettingsDao settingsDao, SettingsManager settingsManager){
        this.settingsDao=settingsDao;
        this.settingsManager=settingsManager;

        requestToUpdateNumberOfScanning =new SingleLiveEvent<>();
        requestToUpdateScanInterval =new SingleLiveEvent<>();
        requestToUpdateAccessLevel =settingsManager.getRequestToUpdateAccessLevel();
        toastContent=new SingleLiveEvent<>();
        requestToUpdateCopyUUID =new SingleLiveEvent<>();

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

    public void acceptSettingsChange(String _scanInterval, int numberOfScanning){

        int scanInterval = convertToValidSettingsData(_scanInterval, numberOfScanning);
        // check valid
        if (scanInterval == -1) return;

        // change manager data
        settingsManager.setScanInterval(scanInterval);
        settingsManager.setNumberOfScanning(numberOfScanning);

        updateDataFromSaved();

        settingsManager.saveChangedSettingsData(scanInterval, numberOfScanning);

        toastContent.setValue("Настройки успешно изменены");

        // вставляем те же данные, чтобы кнопка обновилась
        requestToUpdateScanInterval.setValue(String.valueOf(currentSavedScanInterval));
    }
    private int convertToValidSettingsData(String _scanInterval, int numberOfScanning){
        int scanInterval = -1;
        try {
            scanInterval = Integer.parseInt(_scanInterval);

            if (scanInterval < 3 || numberOfScanning < 0 || numberOfScanning > 5) {
                requestToUpdateScanInterval.setValue(String.valueOf(settingsManager.defaultScanInterval));
                toastContent.setValue("Неккоректные данные");
                scanInterval = -1;
            }
        } catch (NumberFormatException | NullPointerException e) {
            e.printStackTrace();
            toastContent.setValue("Некорректный ввод");
        }

        return scanInterval;
    }
}
