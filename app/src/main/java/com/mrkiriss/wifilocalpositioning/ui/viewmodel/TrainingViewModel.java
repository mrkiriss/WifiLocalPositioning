package com.mrkiriss.wifilocalpositioning.ui.viewmodel;

import android.net.wifi.ScanResult;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.repositiries.TrainingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import lombok.Data;

@Data
public class TrainingViewModel extends ViewModel {

    @Inject
    protected TrainingRepository trainingRepository;

    private ObservableInt remainingNumberOfScanning;
    private ObservableField<String> inputNumberOfScanKits;
    private ObservableField<String> inputY;
    private ObservableField<String> inputX;
    private ObservableField<String> inputCabId;
    private ObservableInt radioMode;
    private ObservableBoolean isScanningStarted;
    private ObservableField<String> selectedFloorId;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private LiveData<String> requestToAddAPs;
    private MutableLiveData<String> resultOfScanningAfterCalibration;
    private MutableLiveData<String> toastContent;
    private MutableLiveData<List<String>> requestToClearRV;
    private LiveData<Integer> remainingNumberOfScanningLD;

    public TrainingViewModel(SavedStateHandle savedStateHandle){
        App.getInstance().getComponentManager().getTrainingSubcomponent().inject(this);

        completeKitsOfScansResult= trainingRepository.getCompleteKitsOfScansResult();
        requestToAddAPs=trainingRepository.getRequestToAddAPs();
        resultOfScanningAfterCalibration=trainingRepository.getServerResponse();
        toastContent=trainingRepository.getToastContent();
        remainingNumberOfScanningLD=trainingRepository.getRemainingNumberOfScanning();

        remainingNumberOfScanning=new ObservableInt(0);
        inputNumberOfScanKits=new ObservableField<>("");
        inputY =new ObservableField<>("");
        inputX =new ObservableField<>("");
        requestToClearRV=new MutableLiveData<>();
        radioMode=new ObservableInt(0);
        inputCabId=new ObservableField<>("");
        isScanningStarted=new ObservableBoolean(false);
        selectedFloorId=new ObservableField<>("");
    }

    public void startScanning(){
        if ((inputNumberOfScanKits.get().equals("") && radioMode.get()!=2) ||
                ((inputY.get().equals("") || inputX.get().equals("") || inputCabId.get().equals("") || selectedFloorId.get().equals("")) && radioMode.get()==2) ||
                (inputCabId.get().equals("") && radioMode.get()==1)){
            toastContent.setValue("Заполните все поля!");
            return;
        }
        resetElements();
        changeScanningStatus(true);
        switch (radioMode.get()){
            case TrainingRepository.MODE_TRAINING_APS:
                trainingRepository.runScanInManager(Integer.parseInt(Objects.requireNonNull(inputNumberOfScanKits.get())),
                        inputCabId.get(), radioMode.get());
                break;
            case TrainingRepository.MODE_TRAINING_COORD:
                trainingRepository.postLocationPointInfoToServer(Integer.parseInt(inputX.get()), Integer.parseInt(inputY.get()),
                        inputCabId.get(), Integer.parseInt(selectedFloorId.get()));
                break;
            case TrainingRepository.MODE_DEFINITION:
                trainingRepository.runScanInManager(Integer.parseInt(Objects.requireNonNull(inputNumberOfScanKits.get())), radioMode.get());
                break;
        }
    }
    public void startProcessingCompleteKitsOfScansResult(CompleteKitsContainer completeKitsContainer){
        trainingRepository.processCompleteKitsOfScanResults(completeKitsContainer);
    }

    public void changeScanningStatus(boolean status){
        isScanningStarted.set(status);
    }
    public void resetElements(){
        remainingNumberOfScanning.set(0);
        //inputNumberOfScanKits.set("");
        //inputLat.set("");
        //inputLon.set("");
        requestToClearRV.setValue(new ArrayList<>());
        resultOfScanningAfterCalibration.setValue("");
    }

    public void onRadioButtonChange(RadioGroup rg, int id){
        switch (rg.getCheckedRadioButtonId()){
            case R.id.radioTrainingAPs:
                radioMode.set(TrainingRepository.MODE_TRAINING_APS);
                break;
            case R.id.radioTrainingCoord:
                radioMode.set(TrainingRepository.MODE_TRAINING_COORD);
                break;
            case R.id.radioDefinition:
                radioMode.set(TrainingRepository.MODE_DEFINITION);
                break;
        }
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
