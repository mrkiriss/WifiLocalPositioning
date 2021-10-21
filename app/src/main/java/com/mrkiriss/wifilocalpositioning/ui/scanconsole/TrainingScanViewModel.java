package com.mrkiriss.wifilocalpositioning.ui.scanconsole;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.repositiries.TrainingScanRepository;
import com.mrkiriss.wifilocalpositioning.utils.livedata.Event;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TrainingScanViewModel extends ViewModel {


    private final TrainingScanRepository trainingScanRepository;

    private ObservableInt remainingNumberOfScanning;
    private ObservableField<String> inputNumberOfScanKits;
    private ObservableBoolean isScanningStarted;

    private final LiveData<Event<CompleteKitsContainer>> completeKitsOfScansResult;
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
        requestToClearRV=new MutableLiveData<>();
        isScanningStarted=new ObservableBoolean(false);
    }

    public void startScanning(){
        if (inputNumberOfScanKits.get().equals("")){
            toastContent.setValue("Заполните все поля!");
            return;
        }

        resetElements();
        changeScanningStatus(true);

        trainingScanRepository.runScanInManager(inputNumberOfScanKits.get());
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
