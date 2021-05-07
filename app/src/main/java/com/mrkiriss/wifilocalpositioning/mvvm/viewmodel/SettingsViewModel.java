package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.SettingsRepository;

import java.util.Objects;

import javax.inject.Inject;

import lombok.Data;

@Data
public class SettingsViewModel extends ViewModel {

    @Inject
    protected SettingsRepository repository;

    private final ObservableField<String> scanInterval;
    private final ObservableInt numberOfScanning;

    private final LiveData<Integer> requiredToUpdateNumberOfScanning;
    private final LiveData<String> requiredToUpdateScanInterval;
    private final LiveData<String> toastContent;

    public SettingsViewModel(){

        App.getInstance().getComponentManager().getSettingsSubcomponent().inject(this);

        scanInterval=new ObservableField<>("");
        numberOfScanning =new ObservableInt(0);

        requiredToUpdateNumberOfScanning=repository.getRequiredToUpdateNumberOfScanning();
        requiredToUpdateScanInterval= repository.getRequiredToUpdateScanInterval();
        toastContent=repository.getToastContent();

        initSettingValuesFromDB();
    }

    private void initSettingValuesFromDB(){
        repository.initSettingValuesFromDB();
    }
    public void acceptSettingsChange(){
        repository.acceptSettingsChange(Integer.parseInt(Objects.requireNonNull(scanInterval.get())),
                numberOfScanning.get());
    }
}
