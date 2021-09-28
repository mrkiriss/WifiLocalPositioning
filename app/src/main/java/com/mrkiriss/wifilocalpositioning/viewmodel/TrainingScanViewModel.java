package com.mrkiriss.wifilocalpositioning.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.repositiries.TrainingScanRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TrainingScanViewModel extends ViewModel {


    private final TrainingScanRepository trainingScanRepository;

    private ObservableInt remainingNumberOfScanning;
    private ObservableField<String> inputNumberOfScanKits;
    private ObservableField<String> inputY;
    private ObservableField<String> inputX;
    private ObservableField<String> inputCabId;
    private ObservableInt scanningMode;
    private ObservableBoolean isScanningStarted;
    private ObservableInt selectedMod;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private LiveData<String> requestToAddAPs;
    private MutableLiveData<String> resultOfScanningAfterCalibration;
    private MutableLiveData<String> toastContent;
    private MutableLiveData<List<String>> requestToClearRV;
    private LiveData<Integer> remainingNumberOfScanningLD;

    @Inject
    public TrainingScanViewModel(TrainingScanRepository repository){

        this.trainingScanRepository=repository;

        completeKitsOfScansResult= trainingScanRepository.getCompleteKitsOfScansResult();
        requestToAddAPs= trainingScanRepository.getRequestToAddAPs();
        resultOfScanningAfterCalibration= trainingScanRepository.getServerResponse();
        toastContent= trainingScanRepository.getToastContent();
        remainingNumberOfScanningLD= trainingScanRepository.getRemainingNumberOfScanning();

        remainingNumberOfScanning=new ObservableInt(0);
        inputNumberOfScanKits=new ObservableField<>("");
        inputY =new ObservableField<>("");
        inputX =new ObservableField<>("");
        requestToClearRV=new MutableLiveData<>();
        scanningMode =new ObservableInt();
        inputCabId=new ObservableField<>("");
        isScanningStarted=new ObservableBoolean(false);
        selectedMod=new ObservableInt(0);
    }

    public void startScanning(){
        if ((inputNumberOfScanKits.get().equals("") && scanningMode.get()!=3) ||
                (inputNumberOfScanKits.get().equals("") && inputCabId.get().equals("") && scanningMode.get()==1)){
            toastContent.setValue("Заполните все поля!");
            return;
        }
        resetElements();
        changeScanningStatus(true);
        convertScanningMode();
        switch (scanningMode.get()){
            case TrainingScanRepository.MODE_TRAINING_APS:
                trainingScanRepository.runScanInManager(Integer.parseInt(Objects.requireNonNull(inputNumberOfScanKits.get())),
                        inputCabId.get(), scanningMode.get());
                break;
            case TrainingScanRepository.MODE_DEFINITION:
                trainingScanRepository.runScanInManager(Integer.parseInt(Objects.requireNonNull(inputNumberOfScanKits.get())), scanningMode.get());
                break;
        }
    }
    private void convertScanningMode(){
        switch (selectedMod.get()){
            case 0:
                scanningMode.set(TrainingScanRepository.MODE_TRAINING_APS);
                break;
            case 1:
                scanningMode.set(TrainingScanRepository.MODE_DEFINITION);
                break;
        }
    }


    public void startProcessingCompleteKitsOfScansResult(CompleteKitsContainer completeKitsContainer){
        trainingScanRepository.processCompleteKitsOfScanResults(completeKitsContainer);
    }

    public void changeScanningStatus(boolean status){
        isScanningStarted.set(status);
    }
    public void resetElements(){
        remainingNumberOfScanning.set(0);
        requestToClearRV.setValue(new ArrayList<>());
        resultOfScanningAfterCalibration.setValue("");
    }
}
