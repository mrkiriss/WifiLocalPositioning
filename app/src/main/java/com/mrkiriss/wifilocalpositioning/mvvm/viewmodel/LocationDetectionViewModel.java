package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

import android.content.Context;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.LocationDetectionRepository;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Data;

@Data
public class LocationDetectionViewModel extends ViewModel {

    @Inject
    protected LocationDetectionRepository repository;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private final LiveData<MapPoint> resultOfDefinition;
    private final LiveData<Floor> changeFloor;
    private final LiveData<Map<FloorId, List<MapPoint>>> requestToAddAllPointsDataInAutoFinders;
    private final LiveData<Boolean> wifiEnabledState;
    private final MutableLiveData<String> requestToRefreshFloor;
    private final MutableLiveData<MapPoint> showCurrentLocation;
    private final MutableLiveData<String> toastContent;

    private final ObservableInt floorNumber;
    private final ObservableField<String> findInput;
    private final ObservableField<String> departureInput;
    private final ObservableField<String> destinationInput;
    private final ObservableBoolean showRoute;
    private final ObservableBoolean showFind;

    public LocationDetectionViewModel(){
        App.getInstance().getComponentManager().getLocationDetectionSubcomponent().inject(this);

        completeKitsOfScansResult= repository.getCompleteKitsOfScansResult();
        resultOfDefinition= repository.getResultOfDefinition();
        changeFloor= repository.getChangeFloor();
        toastContent=repository.getToastContent();
        requestToAddAllPointsDataInAutoFinders=repository.getRequestToAddAllPointsDataInAutoFinders();
        wifiEnabledState=repository.getWifiEnabledState();

        showCurrentLocation=repository.getShowCurrentLocation();

        floorNumber = new ObservableInt(2);
        findInput = new ObservableField<>("");
        departureInput = new ObservableField<>("");
        destinationInput = new ObservableField<>("");
        showRoute=new ObservableBoolean(false);
        showFind = new ObservableBoolean(false);

        requestToRefreshFloor=repository.getRequestToRefreshFloor();
    }

    public void startProcessingCompleteKitsOfScansResult(CompleteKitsContainer scanResults){
        repository.startProcessingCompleteKitsOfScansResult(scanResults);
    }

    public void onShowCurrentLocation(){
        showCurrentLocation.setValue(resultOfDefinition.getValue());
    }
    // show\hide route views
    public void onShowRoute(){
        showRoute.set(true);
        requestToRefreshFloor.setValue("Маршрутизация показывается");
    }
    public void onHideRoute(){
        showRoute.set(false);
        requestToRefreshFloor.setValue("Маршрутизация скрыта");
    }
    // show\hide find views
    public void onShowFind(){
        showFind.set(true);
        requestToRefreshFloor.setValue("Меню поиска показывается");
    }
    public void onHideFind(){
        showFind.set(false);
        requestToRefreshFloor.setValue("Меню поиска скрыто");
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
        if (departureInput.get().isEmpty() || destinationInput.get().isEmpty()){
            requestToRefreshFloor.setValue("Не все поля заполены!");
            return;
        }
        repository.requestRoute(departureInput.get(), destinationInput.get());
    }
    public void startFindRoom(){
        if (findInput.get().isEmpty()){
            requestToRefreshFloor.setValue("Поле поиска не заполнено!");
            return;
        }
        repository.findRoom(findInput.get());
    }

    // получение информации для фильтрации при поиске
    public void showWifiOffering(Context context){
        repository.showWifiOffering(context);
    }
}
