package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.LocationDetectionRepository;

import javax.inject.Inject;

import lombok.Data;

@Data
public class LocationDetectionViewModel extends ViewModel {

    @Inject
    protected LocationDetectionRepository repository;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private final LiveData<MapPoint> resultOfDefinition;
    private final LiveData<Floor> changeFloor;

    private final MutableLiveData<MapPoint> showCurrentLocation;
    private final MutableLiveData<String> toastContent;

    private final ObservableInt floorNumber;
    private final ObservableField<String> findInput;
    private final ObservableField<String> departureInput;
    private final ObservableField<String> destinationInput;
    private final ObservableBoolean showRoute;

    public LocationDetectionViewModel(){
        App.getInstance().getComponentManager().getLocationDetectionSubcomponent().inject(this);

        completeKitsOfScansResult= repository.getCompleteKitsOfScansResult();
        resultOfDefinition= repository.getResultOfDefinition();
        changeFloor= repository.getChangeFloor();
        toastContent=repository.getToastContent();

        showCurrentLocation=new MutableLiveData<>();

        floorNumber = new ObservableInt(2);
        findInput = new ObservableField<>("");
        departureInput = new ObservableField<>("");
        destinationInput = new ObservableField<>("");
        showRoute=new ObservableBoolean(false);
    }

    public void startProcessingCompleteKitsOfScansResult(CompleteKitsContainer scanResults){
        repository.startProcessingCompleteKitsOfScansResult(scanResults);
    }

    public void onShowCurrentLocation(){
        showCurrentLocation.setValue(resultOfDefinition.getValue());
    }

    public void arrowInc(){
        if (floorNumber.get()<4){
            floorNumber.set(floorNumber.get()+1);
            startFloorChanging();
        }
    }
    public void arrowDec(){
        if (floorNumber.get()>0){
            floorNumber.set(floorNumber.get()-1);
            startFloorChanging();
        }
    }
    public void startFloorChanging(){
        repository.changeFloor(floorNumber.get(), showRoute.get());
    }

    public void startBuildRoute(){
        showRoute.set(true);
        if (departureInput.get().isEmpty() || destinationInput.get().isEmpty()){
            toastContent.setValue("Не все поля заполены!");
            return;
        }
        repository.requestRoute(departureInput.get(), destinationInput.get());
    }
}
