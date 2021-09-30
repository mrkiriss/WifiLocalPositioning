package com.mrkiriss.wifilocalpositioning.viewmodel;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.repositiries.SettingsRepository;

import java.util.Objects;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class SettingsViewModel extends ViewModel {

    private final SettingsRepository repository;

    private final ObservableField<String> scanInterval;
    private final ObservableInt numberOfScanning;
    private final ObservableInt accessLevel;

    private final LiveData<Integer> requestToUpdateNumberOfScanning;
    private final LiveData<String> requestToUpdateScanInterval;
    private final LiveData<Integer> requestToUpdateAccessLevel;
    private final LiveData<String> requestToUpdateCopyUUID;
    private final MutableLiveData<String> toastContent;

    @Inject
    public SettingsViewModel(SettingsRepository repository){

        this.repository=repository;

        scanInterval=new ObservableField<>("");
        numberOfScanning =new ObservableInt(0);
        accessLevel=new ObservableInt(0);

        requestToUpdateNumberOfScanning =repository.getRequestToUpdateNumberOfScanning();
        requestToUpdateScanInterval = repository.getRequestToUpdateScanInterval();
        requestToUpdateAccessLevel =repository.getRequestToUpdateAccessLevel();
        requestToUpdateCopyUUID=repository.getRequestToUpdateCopyUUID();
        toastContent=repository.getToastContent();

        initSettingValuesFromDB();
    }

    private void initSettingValuesFromDB(){
        repository.updateSettingValuesFromDB();
    }
    public void acceptSettingsChange(){
        if (scanInterval.get() == null || scanInterval.get().isEmpty()){
            toastContent.setValue("Пустое поле недопустимо");
            scanInterval.set(String.valueOf(repository.getSettingsManager().defaultScanInterval));
            return;
        }

        repository.acceptSettingsChange(scanInterval.get(), numberOfScanning.get());
    }
    public void requestToUpdateAccessLevel(){
        repository.requestToUpdateAccessLevel();
    }
    public void requestToCopyUUID(){
        repository.requestToGetUUID();
    }
}
