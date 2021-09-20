package com.mrkiriss.wifilocalpositioning.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;
import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.repositiries.LocationDetectionRepository;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class LocationDetectionViewModel {

    private final LocationDetectionRepository repository;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private final LiveData<MapPoint> requestToChangeFloorByMapPoint; // изменяет этаж и камеру напрявляет на местоположение
    private final LiveData<Floor> requestToChangeFloor; // изменяет этаж
    private final LiveData<Boolean> wifiEnabledState;
    private final LiveData<String> requestToHideKeyboard;
    private final LiveData<Boolean> requestToUpdateProgressStatusBuildingRoute;
    private final LiveData<String> requestToChangeDestinationInput;
    private final LiveData<String> requestToChangeDepartureInput;
    private final LiveData<String> requestToChangeFindInput;
    private final LiveData<Integer> requestToChangeDepartureIcon;
    private final LiveData<Integer> requestToChangeDestinationIcon;
    private final LiveData<SearchData> requestToLaunchSearchMode;
    private final LiveData<MapPoint> requestToUpdateSearchResultContainerData;

    private final MutableLiveData<String> requestToRefreshFloor;
    private final MutableLiveData<String> toastContent;

    private final ObservableInt floorNumber;
    private final ObservableField<String> findInput;
    private final ObservableField<String> departureInput;
    private final ObservableField<String> destinationInput;
    private final ObservableInt departureIcon;
    private final ObservableInt destinationIcon;
    private final ObservableBoolean progressOfBuildingRouteStatus;
    private final ObservableBoolean searchLineIsDisplayed; // true - показывается строка поиска, false - показывается панель построения маршрута
    private final ObservableBoolean searchResultContainerIsDisplayed;
    private final ObservableField<MapPoint> searchResultContainer;

    public LocationDetectionViewModel(LocationDetectionRepository locationDetectionRepository){

        this.repository=locationDetectionRepository;

        completeKitsOfScansResult= repository.getCompleteKitsOfScansResult();
        requestToChangeFloorByMapPoint = repository.getRequestToChangeFloorByMapPoint();
        requestToChangeFloor = repository.getRequestToChangeFloor();
        toastContent=repository.getToastContent();
        wifiEnabledState=repository.getWifiEnabledState();
        requestToHideKeyboard=repository.getRequestToHideKeyboard();
        requestToUpdateProgressStatusBuildingRoute=repository.getRequestToUpdateProgressStatusBuildingRoute();
        requestToChangeDepartureInput=repository.getRequestToChangeDepartureInput();
        requestToChangeDestinationInput=repository.getRequestToChangeDestinationInput();
        requestToChangeFindInput = repository.getRequestToChangeFindInput();
        requestToChangeDepartureIcon = repository.getRequestToChangeDepartureIcon();
        requestToChangeDestinationIcon = repository.getRequestToChangeDestinationIcon();
        requestToLaunchSearchMode = repository.getRequestToLaunchSearchMode();
        requestToUpdateSearchResultContainerData = repository.getRequestToUpdateSearchResultContainerData();

        floorNumber = new ObservableInt();
        findInput = new ObservableField<>("");
        departureInput = new ObservableField<>("");
        destinationInput = new ObservableField<>("");
        departureIcon = new ObservableInt(R.drawable.ic_circle);
        destinationIcon = new ObservableInt(R.drawable.ic_circle);
        searchLineIsDisplayed = new ObservableBoolean(true);
        progressOfBuildingRouteStatus=new ObservableBoolean(false);
        searchResultContainerIsDisplayed = new ObservableBoolean(false);
        searchResultContainer = new ObservableField<>(new MapPoint());

        requestToRefreshFloor=repository.getRequestToRefreshFloor();
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

    // Методы для взаимодействия со строкой поиска
    public void startLocationSearchProcess(TypeOfSearchRequester type) {
        repository.createActuallySearchDataAndRequestToLaunchSearchMode(type);
    }
    public void processSelectedLocation(TypeOfSearchRequester typeOfRequester, SearchItem searchItem) {
        repository.processSelectedLocation(typeOfRequester, searchItem);
    }

    // Методы для взаимодействия с контейнером результата поиска
    public void showCurrentSearchedLocation() {
        if (searchResultContainer.get() != null)
            repository.showLocationOnMap((searchResultContainer.get().getFullRoomName()));
    }
    public void showBuildingMenuWithCurrentSearchedLocationData() {
        if (searchResultContainer.get() != null)
            repository.updateRouteData(searchResultContainer.get().getRoomName());
        searchLineIsDisplayed.set(false);
        closeSearchContainer();
    }
    public void updateSearchResultContainerData(MapPoint data) {
        searchResultContainer.set(data);
        // отображает контейнер с данными
        searchResultContainerIsDisplayed.set(true);
        // переводит камеру на местоположение
        showCurrentSearchedLocation();
    }
    public void closeSearchContainer() {
        // скрываем контейнер
        searchResultContainerIsDisplayed.set(false);
        // очищаем контент строки поиска
        findInput.set("");
    }

    // Методы для смены картинки этажа / местоположения
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

    // Методы для взаимодействия с контейнером построения маршрутов
    public void startBuildRoute(){
        if (departureInput.get().isEmpty() || destinationInput.get().isEmpty()){
            requestToRefreshFloor.setValue("Не все поля заполены");
            return;
        }
        repository.requestRoute(departureInput.get(), destinationInput.get());
        repository.setShowRoute(true);
    }
    public void closeRouteContainer() {
        // очищаем введённые данные
        departureInput.set("");
        destinationInput.set("");
        // скрываем контейнер маршрутов / показываем строку поиска
        searchLineIsDisplayed.set(true);
        // очищаем данные о построенном маршруте
        repository.clearRouteFloors();
        // даём репозиторию понять, что не нужно отрисовывать маршруты на картах
        repository.setShowRoute(false);
        // запрашиваем обновление карты, чтобы убрать линию маршрута
        repository.changeFloor();
    }

    // получение информации для фильтрации при поиске
    public void showWifiOffering(Context context){
        repository.showWifiOffering(context);
    }
}
