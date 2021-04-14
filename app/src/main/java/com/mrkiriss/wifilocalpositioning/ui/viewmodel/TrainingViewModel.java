package com.mrkiriss.wifilocalpositioning.ui.viewmodel;

import android.net.wifi.ScanResult;
import android.view.View;
import android.widget.RadioGroup;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.repositiries.TrainingRepository;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;

import javax.inject.Inject;

import lombok.Data;

@Data
public class TrainingViewModel extends ViewModel {

    @Inject
    protected TrainingRepository trainingRepository;

    private ObservableInt numberOfSuccessfulScans;
    private ObservableField<String> inputNumberOfScanKits;
    private ObservableField<String> inputPasswordToClear;
    private ObservableField<String> inputLat;
    private ObservableField<String> inputLon;
    private ObservableField<String> inputCabId;
    private ObservableInt radioMode;

    private final LiveData<List<ScanResult>> kitOfScanResults;
    private LiveData<String> requestToAddAPs;
    private MutableLiveData<String> resultOfScanningAfterCalibration;
    private MutableLiveData<String> toastContent;
    private MutableLiveData<List<String>> requestToClearRV;

    public TrainingViewModel(){
        App.getInstance().getComponentManager().getTrainingSubcomponent().inject(this);

        kitOfScanResults= trainingRepository.getKitOfScanResults();
        requestToAddAPs=trainingRepository.getRequestToAddAPs();
        resultOfScanningAfterCalibration=trainingRepository.getResultOfScanningAfterCalibration();
        toastContent=trainingRepository.getToastContent();

        numberOfSuccessfulScans=new ObservableInt(0);
        inputNumberOfScanKits=new ObservableField<>("");
        inputPasswordToClear=new ObservableField<>("");
        inputLat=new ObservableField<>("");
        inputLon=new ObservableField<>("");
        requestToClearRV=new MutableLiveData<>();
        radioMode=new ObservableInt(0);
        inputCabId=new ObservableField<>("");
    }

    public void startScanning(View view){
        if (inputNumberOfScanKits.get().equals("") || (inputLat.get().equals("") && inputLon.get().equals("") && radioMode.get()==1)
        || (inputCabId.get().equals("") && radioMode.get()==3)){
            toastContent.setValue("Заполните все поля!");
            return;
        }
        resetElements();
        switch (radioMode.get()){
            case 1:
                trainingRepository.runScanInManager(Integer.parseInt(Objects.requireNonNull(inputNumberOfScanKits.get())),
                        Integer.parseInt(Objects.requireNonNull(inputLat.get())), Integer.parseInt(Objects.requireNonNull(inputLon.get())), radioMode.get());
                break;
            case 2:
                trainingRepository.runScanInManager(Integer.parseInt(Objects.requireNonNull(inputNumberOfScanKits.get())),
                        0, 0, radioMode.get());
                break;
            case 3:
                trainingRepository.runScanInManager(Integer.parseInt(Objects.requireNonNull(inputNumberOfScanKits.get())),
                        inputCabId.get(), radioMode.get());
                break;
            case 4:
                trainingRepository.runScanInManager(Integer.parseInt(Objects.requireNonNull(inputNumberOfScanKits.get())),
                        "", radioMode.get());
                break;
        }
    }
    public void onClearButtonClick(){
        if (inputPasswordToClear.get()==null || !inputPasswordToClear.get().equals("1992163a")) return;
        trainingRepository.runCleaningServer();
    }
    public void startProcessingScanResultKit(List<ScanResult> scanResults){
        numberOfSuccessfulScans.set(numberOfSuccessfulScans.get()+1);
        trainingRepository.startProcessingScanResultKit(scanResults, numberOfSuccessfulScans.get());
    }

    public void onRadioButtonChange(RadioGroup rg, int id){
        switch (rg.getCheckedRadioButtonId()){
            case R.id.radioTraining:
                radioMode.set(TrainingRepository.MODE_TRAINING);
                break;
            case R.id.radioDefinition:
                radioMode.set(TrainingRepository.MODE_DEFINITION);
                break;
            case R.id.radioTraining2:
                radioMode.set(TrainingRepository.MODE_TRAINING2);
                break;
            case R.id.radioDefinition2:
                radioMode.set(TrainingRepository.MODE_DEFINITION2);
                break;
        }
    }

    public void resetElements(){
        numberOfSuccessfulScans.set(0);
        //inputNumberOfScanKits.set("");
        inputPasswordToClear.set("");
        //inputLat.set("");
        //inputLon.set("");
        requestToClearRV.setValue(new ArrayList<>());
        resultOfScanningAfterCalibration.setValue("");
    }

    public void unregisterWifiScannerCallBack(){
        trainingRepository.unregisterScanCallbacks();
    }
}
