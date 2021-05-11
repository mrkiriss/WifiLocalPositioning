package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

import android.content.Context;

import androidx.databinding.Observable;
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
import java.util.Objects;

import javax.inject.Inject;

import lombok.Data;

@Data
public class LocationDetectionViewModel extends ViewModel {

    @Inject
    protected LocationDetectionRepository repository;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private final LiveData<MapPoint> requestToChangeFloorByMapPoint; // изменяет этаж и камеру напрявляет на местоположение
    private final LiveData<Floor> requestToChangeFloor; // изменяет этаж
    private final LiveData<Map<FloorId, List<MapPoint>>> requestToAddAllPointsDataInAutoFinders;
    private final LiveData<Boolean> wifiEnabledState;
    private final LiveData<String> requestToHideKeyboard;
    private final LiveData<Boolean> requestToUpdateProgressStatusBuildingRoute;
    private final LiveData<Integer> requestToUpdateAccessLevel;

    private final MutableLiveData<String> requestToRefreshFloor;
    private final MutableLiveData<String> toastContent;

    private final ObservableInt floorNumber;
    private final ObservableField<String> findInput;
    private final ObservableField<String> departureInput;
    private final ObservableField<String> destinationInput;
    private final ObservableBoolean showRoute;
    private final ObservableBoolean showFind;
    private final ObservableBoolean progressOfBuildingRouteStatus;

    public LocationDetectionViewModel(){
        App.getInstance().getComponentManager().getLocationDetectionSubcomponent().inject(this);

        completeKitsOfScansResult= repository.getCompleteKitsOfScansResult();
        requestToChangeFloorByMapPoint = repository.getRequestToChangeFloorByMapPoint();
        requestToChangeFloor = repository.getRequestToChangeFloor();
        toastContent=repository.getToastContent();
        requestToAddAllPointsDataInAutoFinders=repository.getRequestToAddAllPointsDataInAutoFinders();
        wifiEnabledState=repository.getWifiEnabledState();
        requestToHideKeyboard=repository.getRequestToHideKeyboard();
        requestToUpdateProgressStatusBuildingRoute=repository.getRequestToUpdateProgressStatusBuildingRoute();
        requestToUpdateAccessLevel=repository.getRequestToUpdateAccessLevel();

        floorNumber = new ObservableInt();
        findInput = new ObservableField<>("");
        departureInput = new ObservableField<>("");
        destinationInput = new ObservableField<>("");
        showRoute=new ObservableBoolean(false);
        showFind = new ObservableBoolean(false);
        progressOfBuildingRouteStatus=new ObservableBoolean(false);

        requestToRefreshFloor=repository.getRequestToRefreshFloor();

        // обозреваем изменение режима навагиции для установки текущего местоположения  в строку начала
        // + изменяем требования в репозитории, чтобы выбирать изображении для отрисовки местоположения
        showRoute.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (requestToChangeFloorByMapPoint.getValue()!=null)
                    departureInput.set(Objects.requireNonNull(requestToChangeFloorByMapPoint.getValue()).getRoomName());

                repository.clearRouteFloors();
                repository.setShowRoute(showRoute.get());
            }
        });
        // изменяет номер этажа в репозитории
        floorNumber.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                repository.setCurrentFloorIdInt(floorNumber.get());
            }
        });
        floorNumber.set(2); // провоцируем начальное изменение при запуске
    }

    public void startProcessingCompleteKitsOfScansResult(CompleteKitsContainer scanResults){
        repository.startProcessingCompleteKitsOfScansResult(scanResults);
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
        repository.changeFloor();
    }

    public void onShowCurrentLocation(){
        repository.changeFloorWithMapPoint();
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
