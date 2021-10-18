package com.mrkiriss.wifilocalpositioning.data.repositiries;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.utils.livedata.Event;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.search.PreviousNameInput;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchItem;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;
import com.mrkiriss.wifilocalpositioning.data.models.server.AccessPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.ListOfAllMapPoints;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.db.MapPointsDao;
import com.mrkiriss.wifilocalpositioning.data.sources.db.PreviousMapPointsDao;
import com.mrkiriss.wifilocalpositioning.data.sources.remote.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.utils.ConnectionManager;
import com.mrkiriss.wifilocalpositioning.utils.livedata.SingleLiveEvent;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
public class LocationDetectionRepository implements Serializable {

    private final LocationDataApi retrofit;
    private final WifiScanner wifiScanner;
    private final MapImageManager mapImageManager;
    private final ConnectionManager connectionManager;
    private final SettingsManager settingsManager;
    private final MapPointsDao mapPointsDao;
    private final PreviousMapPointsDao previousInputDao;

    private final LiveData<Event<CompleteKitsContainer>> completeKitsOfScansResult;
    private final MutableLiveData<MapPoint> requestToChangeFloorByMapPoint;
    private final MutableLiveData<Floor> requestToChangeFloor;
    private final SingleLiveEvent<String> toastContent;
    private final MutableLiveData<Event<String>> requestToRefreshFloor;
    private final MutableLiveData<Event<Boolean>> wifiEnabledState; // для отправки состояния включение wifi
    private final SingleLiveEvent<String> requestToHideKeyboard;
    private final SingleLiveEvent<Boolean> requestToUpdateProgressStatusBuildingRoute;
    private final SingleLiveEvent<String> requestToChangeDepartureInput;
    private final SingleLiveEvent<String> requestToChangeDestinationInput;
    private final SingleLiveEvent<String> requestToChangeFindInput;
    private final SingleLiveEvent<Integer> requestToChangeDepartureIcon;
    private final SingleLiveEvent<Integer> requestToChangeDestinationIcon;
    private final SingleLiveEvent<SearchData> requestToLaunchSearchMode;
    private final SingleLiveEvent<MapPoint> requestToUpdateSearchResultContainerData;


    private List<LocationPointInfo> listOfSearchableLocations;

    private MapPoint resultOfDefinition; // актуальное местоположение пользователя
    private boolean showRoute; // VM изменяет, отвечает за идикацию необходимости рисовать маршрут
    private int currentFloorIdInt; // VM изменяет, текущий номер этажа

    @Inject
    public LocationDetectionRepository(LocationDataApi retrofit, WifiScanner wifiScanner,
                                       ConnectionManager connectionManager, MapImageManager mapImageManager,
                                       SettingsManager settingsManager, MapPointsDao mapPointsDao,
                                       PreviousMapPointsDao previousInputDao){
        this.retrofit=retrofit;
        this.wifiScanner=wifiScanner;
        this.mapImageManager = mapImageManager;
        this.connectionManager=connectionManager;
        this.settingsManager=settingsManager;
        this.mapPointsDao=mapPointsDao;
        this.previousInputDao=previousInputDao;

        completeKitsOfScansResult=wifiScanner.getCompleteScanResults();
        requestToRefreshFloor=mapImageManager.getRequestToRefreshFloor();
        wifiEnabledState=wifiScanner.getWifiEnabledState();

        requestToChangeFloorByMapPoint =new MutableLiveData<>();
        requestToChangeFloor =new MutableLiveData<>();
        toastContent=new SingleLiveEvent<>();
        requestToHideKeyboard=new SingleLiveEvent<>();
        requestToUpdateProgressStatusBuildingRoute=new SingleLiveEvent<>();
        requestToChangeDepartureInput=new SingleLiveEvent<>();
        requestToChangeDestinationInput=new SingleLiveEvent<>();
        requestToChangeFindInput = new SingleLiveEvent<>();
        requestToLaunchSearchMode = new SingleLiveEvent<>();
        requestToUpdateSearchResultContainerData = new SingleLiveEvent<>();
        requestToChangeDepartureIcon = new SingleLiveEvent<>();
        requestToChangeDestinationIcon = new SingleLiveEvent<>();

        wifiScanner.startDefiningScan(WifiScanner.TypeOfScanning.DEFINITION);

        requestListOfLocationPointsInfo();
    }

    // запрашивает вызов диалога о необходимости включения wifi
    public void showWifiOffering(Context context){
        connectionManager.showOfferSetting(context);
    }

    // floor changing
    public void changeFloor(){
        Floor requiredFloor = defineNecessaryFloor();
        if (requiredFloor==null) return;
        requestToChangeFloor.setValue(requiredFloor);
    }
    private Floor defineNecessaryFloor(){
        Floor requiredFloor;

        if (resultOfDefinition!=null && resultOfDefinition.getFloorIdInt()==currentFloorIdInt){ // этаж совпадает с текущим местоположением
            if (showRoute){
                requiredFloor = (mapImageManager.getRouteFloor(Floor.convertFloorIdToEnum(resultOfDefinition.getFloorIdInt())));
            }else{
                requiredFloor = (mapImageManager.getBasicFloor(Floor.convertFloorIdToEnum(resultOfDefinition.getFloorIdInt())));
            }
            // дорисовываем местоположение
            int requiredX = resultOfDefinition.getX();
            int requiredY = resultOfDefinition.getY();
            requiredFloor = mapImageManager.getFloorWithPointer(
                    requiredFloor, requiredX, requiredY
            );
        }else{ // без текущего местоположения
            if (showRoute){
                requiredFloor = (mapImageManager.getRouteFloor(Floor.convertFloorIdToEnum(currentFloorIdInt)));
            }else{
                requiredFloor = (mapImageManager.getBasicFloor(Floor.convertFloorIdToEnum(currentFloorIdInt)));
            }
        }

        if (requiredFloor==null){
            return null;
        }
        return requiredFloor;
    }
    private Floor defineNecessaryFloorForShowCurrentLocation(){
        Floor requiredFloor;

        if (resultOfDefinition==null){
            toastContent.setValue("Местоположение не определено");
            return null;
        }

        if (showRoute){
            requiredFloor = (mapImageManager.getRouteFloor(Floor.convertFloorIdToEnum(resultOfDefinition.getFloorIdInt())));
        }else{
            requiredFloor = (mapImageManager.getBasicFloor(Floor.convertFloorIdToEnum(resultOfDefinition.getFloorIdInt())));
        }
        // дорисовываем местоположение
        int requiredX = resultOfDefinition.getX();
        int requiredY = resultOfDefinition.getY();
        requiredFloor = mapImageManager.getFloorWithPointer(
                requiredFloor, requiredX, requiredY
        );

        return requiredFloor;
    }

    public void changeFloorWithMapPoint(){
        Floor necessaryFloor = defineNecessaryFloorForShowCurrentLocation();
        if (necessaryFloor==null){
            return;
        }

        resultOfDefinition.setFloorWithPointer(necessaryFloor);
        requestToChangeFloorByMapPoint.setValue(resultOfDefinition);
    }

    public void clearRouteFloors(){
        mapImageManager.getRouteFloors().clear();
    }

    // search
        // создаёт пакет данных для поиска и подаёт запрос на запуск фрагмента
    public void createActuallySearchDataAndRequestToLaunchSearchMode(TypeOfSearchRequester typeOfRequester) {

        Runnable task = () -> {
            String hint = "";
            switch (typeOfRequester) {
                case DEPARTURE:
                    hint = "Откуда";
                    break;
                case DESTINATION:
                    hint = "Куда";
                    break;
                case FIND:
                default:
                    hint = "Название локации";
            }

            List<SearchItem> prevItems = getListOfPreviousSelectedSearchItems();
            List<SearchItem> availableItems = convertMapPointsMapToListOfSearchItems(mapImageManager.getDataOnPointsOnAllFloors());

            List<SearchItem> actuallyPrevItems = filterLegacySelectedSearchItemsAndStartDeletingLegacy(prevItems, availableItems);

            SearchData data = new SearchData(actuallyPrevItems,
                    availableItems,
                    convertCurrentLocationSearchItem(),
                    hint,
                    typeOfRequester);

            requestToLaunchSearchMode.postValue(data);
        };
        new Thread(task).start();
    }
    private List<SearchItem> convertMapPointsMapToListOfSearchItems(Map<FloorId, List<MapPoint>> data) {
        List<SearchItem> result = new ArrayList<>();
        for (List<MapPoint> listOfMapPoints: data.values()) {
            for (MapPoint point: listOfMapPoints) {
                // только комнаты
                if (point.isRoom()) result.add(new SearchItem(point.getFullRoomName(), point.getDescription()));
            }
        }

        return result;
    }
    private SearchItem convertCurrentLocationSearchItem(){
        if (resultOfDefinition == null) return null;

        return new SearchItem(MapPoint.CURRENT_LOCATION_ADDING + resultOfDefinition.getFullRoomName(), resultOfDefinition.getDescription());
    }
    private List<SearchItem> filterLegacySelectedSearchItemsAndStartDeletingLegacy( List<SearchItem> filterableItems, List<SearchItem> availableItems) {

        // возвращает все предыдущей без фильтрации, так как данные о доступных точках ещё не подгружены из бд
        if (availableItems == null || availableItems.isEmpty()) return filterableItems;

        List<String> availableNames = new ArrayList<>();
        for (SearchItem availableItem: availableItems) {
            availableNames.add(availableItem.getName());
        }

        List<SearchItem> legacyItems = new ArrayList<>();
        List<SearchItem> actuallyItems = new ArrayList<>();
        for (SearchItem filterableItem: filterableItems) {
            if (availableNames.contains(filterableItem.getName())) {
                actuallyItems.add(filterableItem);
            }else {
                legacyItems.add(filterableItem);
            }
        }

        deleteLegacySelectedSearchItems(legacyItems);

        return actuallyItems;

    }

        // манипулирует данными поиска в зависимости от типа запросившего
    public void processSelectedLocation(@Nullable TypeOfSearchRequester typeOfRequester, @Nullable SearchItem selectedLocation) {
        if (typeOfRequester == null || selectedLocation == null) return;

        boolean isCurrentLocation = selectedLocation.getName().contains(MapPoint.CURRENT_LOCATION_ADDING);
        String finalName = selectedLocation.getName().replace(MapPoint.CURRENT_LOCATION_ADDING, "");

        switch (typeOfRequester) {
            case FIND:
                MapPoint availableMapPointByName = findMapPointByName(finalName);
                if (availableMapPointByName != null) {
                    requestToUpdateSearchResultContainerData.setValue(availableMapPointByName);
                    requestToChangeFindInput.setValue(finalName);
                } else {
                    toastContent.setValue("Данные о локации отсутствуют");
                }
                break;
            case DEPARTURE:
                requestToChangeDepartureIcon.setValue(isCurrentLocation ? R.drawable.ic_cursor : R.drawable.ic_circle);
                requestToChangeDepartureInput.setValue(finalName);
                break;
            case DESTINATION:
                requestToChangeDestinationIcon.setValue(isCurrentLocation ? R.drawable.ic_cursor : R.drawable.ic_circle);
                requestToChangeDestinationInput.setValue(finalName);
                break;
        }

        addSelectedSearchInputInDB(selectedLocation);
    }
    private MapPoint findMapPointByName(String name) {
        Map<FloorId, List<MapPoint>> data = mapImageManager.getDataOnPointsOnAllFloors();
        for (List<MapPoint> mapPoints: data.values()) {
            for (MapPoint mapPoint: mapPoints) {
                if (name.equals(mapPoint.getRoomName())) return mapPoint;
            }
        }

        return null;
    }
        // показывает локацию на карте, предварительно проводя поиск по названию в Map'e доступных локаций
    public void showLocationOnMap(String name){
            // получает базовый этаж и вставялет его в объект точки, чтобы получить внутри фрагмента и прорисовать этаж

            MapPoint result = findMapPointByName(name);
            if (result == null) {
                toastContent.setValue("Локация не найдена");
                return;
            }

            result = result.copy();
            // изменяем номер этажа для успешного определения необходимого объекта Floor с помощью defineNecessaryFloor()
            currentFloorIdInt=result.getFloorIdInt();
            // определяем этаж и вставляем его в объект для отправки
            result.setFloorWithPointer(defineNecessaryFloor());
            // прорисовываем
            requestToChangeFloorByMapPoint.setValue(result);
    }
        // заполняем данные в меню построения маршрута
    public void updateRouteData(String destinationName) {
        if (resultOfDefinition == null) {
            requestToChangeDepartureInput.setValue("");
            requestToChangeDepartureIcon.setValue(R.drawable.ic_question);
        }else {
            requestToChangeDepartureInput.setValue(resultOfDefinition.getRoomName());
            requestToChangeDepartureIcon.setValue(R.drawable.ic_cursor);
        }

        requestToChangeDestinationIcon.setValue(R.drawable.ic_circle);
        requestToChangeDestinationInput.setValue(destinationName);
    }

        // DB для предыдущих выборов в фрагменте поиска
    public void addSelectedSearchInputInDB(SearchItem searchItem) {
        PreviousNameInput previousNameInput = new PreviousNameInput();
        previousNameInput.setInputName(searchItem.getName());
        previousNameInput.setInputDate(System.currentTimeMillis());

        Runnable task = () -> {
            PreviousNameInput duplicate = previousInputDao.findByInputName(previousNameInput.getInputName());
            if (duplicate != null) {
                previousInputDao.delete(duplicate);
            }

            previousInputDao.insert(previousNameInput);
        };
        new Thread(task).start();
    }
    private List<SearchItem> getListOfPreviousSelectedSearchItems() {
        List<SearchItem> mapPointNames = new ArrayList<>();
        for (PreviousNameInput i : previousInputDao.findAll()) {
            mapPointNames.add(new SearchItem(i.getInputName(), ""));
        }
        return mapPointNames;
    }
    private void deleteLegacySelectedSearchItems(List<SearchItem> legacySearchItems) {
        Runnable task = () -> {
            for (SearchItem item: legacySearchItems) {
                previousInputDao.deleteByInputName(item.getName());
            }
        };
        new Thread(task).start();
    }


    // scanning
    public void startProcessingCompleteKitsOfScansResult(CompleteKitsContainer completeKitsContainer){
        if (completeKitsContainer.getRequestSourceType() != WifiScanner.TypeOfScanning.DEFINITION) return;

        CalibrationLocationPoint calibrationLocationPoint = new CalibrationLocationPoint();
        for (List<ScanResult> oneScanResults: completeKitsContainer.getCompleteKits()) {
            List<AccessPoint> accessPoints = new ArrayList<>();
            for (ScanResult scanResult : oneScanResults) {
                accessPoints.add(new AccessPoint(scanResult.BSSID, scanResult.level));
            }
            // создаём запрос на добавление информации набора на экран пользователя
            calibrationLocationPoint.addOneCalibrationSet(accessPoints);
        }

        // отправляем запрос на сервер
        requestDefinitionLocation(calibrationLocationPoint);
    }

    // server
    private void requestDefinitionLocation(CalibrationLocationPoint calibrationLocationPoint) {
        // запускаем очередное сканирование для определения местоположения, если пришёл пустой запрос - индикатор начала цикла сканирований для определениея местоположения
        if (calibrationLocationPoint.getCalibrationSets()==null || calibrationLocationPoint.getCalibrationSets().size()==0){
            wifiScanner.startDefiningScan(WifiScanner.TypeOfScanning.DEFINITION);
            return;
        }

        retrofit.defineLocation(calibrationLocationPoint).enqueue(new Callback<DefinedLocationPoint>() {
            @Override
            public void onResponse(@NotNull Call<DefinedLocationPoint> call, @NotNull Response<DefinedLocationPoint> response) {
                wifiScanner.startDefiningScan(WifiScanner.TypeOfScanning.DEFINITION);

                try {
                    if (response.body() == null || response.body().getFloorId() == -1 || response.body().getRoomName() == null)
                        return;

                    // сохраняет в поле репозитория
                    resultOfDefinition = convertToMapPoint(response.body());

                    // требует изменение картинки этажа в соответсвии со всеми параметрами
                    changeFloor();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<DefinedLocationPoint> call, Throwable t) {
                wifiScanner.startDefiningScan(WifiScanner.TypeOfScanning.DEFINITION);
            }
        });
    }
    private MapPoint convertToMapPoint(DefinedLocationPoint definedLocationPoint){
        MapPoint result = new MapPoint();

        result.setX(definedLocationPoint.getX());
        result.setY(definedLocationPoint.getY());
        result.setRoomName(definedLocationPoint.getRoomName());
        result.setFloorWithPointer(mapImageManager.getBasicFloor(Floor.convertFloorIdToEnum(definedLocationPoint.getFloorId())));
        result.setFloorIdInt(definedLocationPoint.getFloorId());
        result.setRoom(definedLocationPoint.isRoom());

        return result;
    }

    public void requestRoute(String start, String end){
        requestToUpdateProgressStatusBuildingRoute.setValue(true);
        retrofit.getRoute(start, end).enqueue(new Callback<List<LocationPointInfo>>() {
            @Override
            public void onResponse(Call<List<LocationPointInfo>> call, Response<List<LocationPointInfo>> response) {

                if (response.body()==null){
                    toastContent.setValue("Маршрут построить не удалось");
                    return;
                }
                // начинаем создавать маршрут
                mapImageManager.startCreatingFloorsWithRout(response.body());
                // скрываем клавиатуру (текст роли не играет)
                requestToHideKeyboard.setValue("successful building route");
                // обновляем состояние процесса построения маршрута (кнопка включается, прогрессбар скрывается)
                requestToUpdateProgressStatusBuildingRoute.setValue(false);
            }

            @Override
            public void onFailure(Call<List<LocationPointInfo>> call, Throwable t) {
                toastContent.setValue("Маршрут построить не удалось");
                // обновляем состяоние процесса построения маршурат (кнопка включается, прогрессбар скрывается)
                requestToUpdateProgressStatusBuildingRoute.setValue(false);
            }
        });
    }

    public void requestListOfLocationPointsInfo(){
        retrofit.getListOfAllMapPoints().enqueue(new Callback<ListOfAllMapPoints>() {
            @Override
            public void onResponse(Call<ListOfAllMapPoints> call, Response<ListOfAllMapPoints> response) {
                if (response.body()==null) {

                    // повторный запрос на точки из локальной бд
                    requestListOfLocationPointsInfoFromDB();

                    return;
                }
                // обрабатываем
                processListOfLPI(response.body());
                // актуализируем в локальной бд
                saveActualListOfLocationPointsInfo(response.body());
            }

            @Override
            public void onFailure(Call<ListOfAllMapPoints> call, Throwable t) {
                // повторный запрос на точки из локальной бд
                requestListOfLocationPointsInfoFromDB();
            }
        });
    }
    // получает данные из бд и запускает их обработки
    private void requestListOfLocationPointsInfoFromDB(){
        Runnable task = () -> {
            List<LocationPointInfo> currentDbData = mapPointsDao.findAll();
            ListOfAllMapPoints container = new ListOfAllMapPoints();
            container.setLocationPointInfos(currentDbData);

            processListOfLPI(container);
        };
        new Thread(task).start();
    }
    // сохраняет данные, пришедшие с сервера в бд, актуилизируя их
    private void saveActualListOfLocationPointsInfo(ListOfAllMapPoints data){
        if (data==null || data.getLocationPointInfos()==null) return;

        Runnable task = () -> {
            // удаляем старые
            mapPointsDao.deleteAll();
            // сохраняем новые
            mapPointsDao.saveAll(data.getLocationPointInfos());
        };
        new Thread(task).start();
    }
    // обрабатывает данные, полученные от сервера или с лок. бд - конвертирует и отправляет в менеджер изображений и строку ввода аудиторий
    private void processListOfLPI(ListOfAllMapPoints data){
        if (data==null || data.getLocationPointInfos()==null) return;

        Map<FloorId, List<MapPoint>> converted = data.convertToMap();
        mapImageManager.setDataOnPointsOnAllFloors(converted);
    }
}
