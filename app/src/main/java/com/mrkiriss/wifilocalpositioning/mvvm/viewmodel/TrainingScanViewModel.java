package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.TrainingScanRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import lombok.Data;

@Data
public class TrainingScanViewModel extends ViewModel {

    @Inject
    protected TrainingScanRepository trainingScanRepository;

    private ObservableInt remainingNumberOfScanning;
    private ObservableField<String> inputNumberOfScanKits;
    private ObservableField<String> inputY;
    private ObservableField<String> inputX;
    private ObservableField<String> inputCabId;
    private ObservableInt scanningMode;
    private ObservableBoolean isScanningStarted;
    private ObservableField<String> selectedScanningMode;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private LiveData<String> requestToAddAPs;
    private MutableLiveData<String> resultOfScanningAfterCalibration;
    private MutableLiveData<String> toastContent;
    private MutableLiveData<List<String>> requestToClearRV;
    private LiveData<Integer> remainingNumberOfScanningLD;

    public TrainingScanViewModel(){
        App.getInstance().getComponentManager().getTrainingScanSubcomponent().inject(this);

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
        selectedScanningMode=new ObservableField<>("Режим тренировки(сканирование)");
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
        String scanningModeString = selectedScanningMode.get();
        switch (scanningModeString){
            case "Режим тренировки(сканирование)":
                scanningMode.set(TrainingScanRepository.MODE_TRAINING_APS);
                break;
            case "Режим определения":
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

    @BindingAdapter(value = {"app:selectedValue", "selectedValueAttrChanged"}, requireAll = false)
    public static void bindSpinnerData(Spinner spinner, String newSelectedValue, final InverseBindingListener newTextAttrChanged) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newTextAttrChanged.onChange();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (newSelectedValue != null) {
            int pos = ((ArrayAdapter<String>) spinner.getAdapter()).getPosition(newSelectedValue);
            spinner.setSelection(pos, true);
        }
    }
    @InverseBindingAdapter(attribute = "app:selectedValue", event = "selectedValueAttrChanged")
    public static String captureSelectedValue(Spinner spinner) {
        return (String)spinner.getSelectedItem();
    }
}