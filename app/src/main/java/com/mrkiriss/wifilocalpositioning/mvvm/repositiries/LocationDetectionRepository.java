package com.mrkiriss.wifilocalpositioning.mvvm.repositiries;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.models.server.ListOfAllMapPoints;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.sources.api.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.AccessPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.utils.ConnectionManager;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private final LiveData<Integer> requestToUpdateAccessLevel;
    private final MutableLiveData<MapPoint> requestToChangeFloorByMapPoint;
    private final MutableLiveData<Floor> requestToChangeFloor;
    private final MutableLiveData<String> toastContent;
    private final MutableLiveData<String> requestToRefreshFloor;
    private final MutableLiveData<Map<FloorId, List<MapPoint>>> requestToAddAllPointsDataInAutoFinders;
    private final MutableLiveData<Boolean> wifiEnabledState; // для отправки состояния включение wifi
    private final MutableLiveData<String> requestToHideKeyboard;
    private final MutableLiveData<Boolean> requestToUpdateProgressStatusBuildingRoute;
    private final MutableLiveData<MapPoint> requestToUpdateCurrentLocationOnAutoComplete;
    private final MutableLiveData<String> requestToChangeDepartureInput;


    private List<LocationPointInfo> listOfSearchableLocations;

    private MapPoint resultOfDefinition;
    private boolean showRoute; // VM изменяет, отвечает за идикацию необходимости рисовать маршрут
    private int currentFloorIdInt; // VM изменяет, текущий номер этажа

    public LocationDetectionRepository(LocationDataApi retrofit, WifiScanner wifiScanner,
                                       ConnectionManager connectionManager, MapImageManager mapImageManager, SettingsManager settingsManager){
        this.retrofit=retrofit;
        this.wifiScanner=wifiScanner;
        this.mapImageManager = mapImageManager;
        this.connectionManager=connectionManager;
        this.settingsManager=settingsManager;

        completeKitsOfScansResult=wifiScanner.getCompleteScanResults();
        requestToRefreshFloor=mapImageManager.getRequestToRefreshFloor();
        requestToUpdateAccessLevel=settingsManager.getRequestToUpdateAccessLevel();

        requestToChangeFloorByMapPoint =new MutableLiveData<>();
        requestToChangeFloor =new MutableLiveData<>();
        toastContent=new MutableLiveData<>();
        requestToAddAllPointsDataInAutoFinders=new MutableLiveData<>();
        requestToHideKeyboard=new MutableLiveData<>();
        requestToUpdateProgressStatusBuildingRoute=new MutableLiveData<>();
        requestToUpdateCurrentLocationOnAutoComplete=new MutableLiveData<>();
        requestToChangeDepartureInput=new MutableLiveData<>();

        wifiEnabledState=wifiScanner.getWifiEnabledState();

        wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);

        requestListOfLocationPointsInfo(5);
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
            Log.e("LocationDetectionRep", "required floor is null");
            return null;
        }
        Log.i("LocationDetectionRep", "required floor is="+requiredFloor);
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

    // find
    public void findRoom(String name){
        Map<FloorId, List<MapPoint>> data = mapImageManager.getDataOnPointsOnAllFloors();
        for (FloorId floorId:data.keySet()){
            for (MapPoint mapPoint: Objects.requireNonNull(data.get(floorId))){
                if (mapPoint.getRoomName().equals(name) && (mapPoint.isRoom() || settingsManager.isModerator())){
                    // получает базовый этаж и вставялет его в объект точки, чтобы получить внутри фрагмента и прорисовать этаж
                    MapPoint result = mapPoint.copy();
                    // изменяем номер этажа для успешного определения необходимого объекта Floor с помощью defineNecessaryFloor()
                    currentFloorIdInt=result.getFloorIdInt();
                    // определяем этаж и вставляем его в обхект для отправки
                    result.setFloorWithPointer(defineNecessaryFloor());
                    // изменяем строку ввода начала маршрута в меню построения маршрута
                    requestToChangeDepartureInput.setValue(result.getRoomName());

                    // прорисовываем
                    requestToChangeFloorByMapPoint.setValue(result);
                    toastContent.setValue("Локация найдена");

                    return;
                }
            }
        }
        toastContent.setValue("Локация не найдена");
    }

    // scanning
    public void startProcessingCompleteKitsOfScansResult(CompleteKitsContainer completeKitsContainer){

        if (!completeKitsContainer.getRequestSourceType().equals(WifiScanner.TYPE_DEFINITION)) return;

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

        if (calibrationLocationPoint.getCalibrationSets()==null || calibrationLocationPoint.getCalibrationSets().size()==0){
            wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);
            return;
        }

        Log.println(Log.INFO, "LocationDetectionRep",
                String.format("start definition location with data=%s", calibrationLocationPoint));

        retrofit.defineLocation(calibrationLocationPoint).enqueue(new Callback<DefinedLocationPoint>() {
            @Override
            public void onResponse(@NotNull Call<DefinedLocationPoint> call, @NotNull Response<DefinedLocationPoint> response) {
                wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);

                Log.println(Log.INFO, "LocationDetectionRep",
                        String.format("Server define location result=%s", response.body()));
                try {
                    if (response.body() == null || response.body().getFloorId() == -1 || response.body().getRoomName() == null)
                        return;

                    // сохраняет в поле репозитория
                    resultOfDefinition = convertToMapPoint(response.body());

                    Log.println(Log.INFO, "convert result= ",
                            resultOfDefinition.toStringAllObject());

                    // уведомление о имени через тост
                    // + обновление текущего местоположения в автодополняющемся поиске
                    // + обновление строки ввода начала маршруту в меню построения маршрута
                    if (resultOfDefinition.isRoom() || settingsManager.isModerator()) {
                        toastContent.setValue("Местоположение: " + resultOfDefinition.getRoomName());
                        requestToUpdateCurrentLocationOnAutoComplete.setValue(resultOfDefinition);
                        requestToChangeDepartureInput.setValue(resultOfDefinition.getRoomName());
                    }
                    // требует изменение картинки этажа в соответсвии со всеми параметрами
                    changeFloor();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<DefinedLocationPoint> call, Throwable t) {
                Log.e("SERVER_ERROR_LDRep", t.getMessage());
                wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);
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
                Log.println(Log.INFO, "LocationDetectionRep",
                        String.format("Server route=%s", response.body()));

                if (response.body()==null){
                    Log.e("SERVER_ERROR_LDRep", "Response body is null");
                    toastContent.setValue("Маршрут построить не удалось");
                    return;
                }
                // начинаем создавать маршрут
                mapImageManager.startCreatingFloorsWithRout(response.body());
                // скрываем клавиатуру (текст роли не играет)
                requestToHideKeyboard.setValue("successful building route");
                // обновляем состяоние процесса построения маршурат (кнопка включается, прогрессбар скрывается)
                requestToUpdateProgressStatusBuildingRoute.setValue(false);
            }

            @Override
            public void onFailure(Call<List<LocationPointInfo>> call, Throwable t) {
                Log.e("SERVER_ERROR", t.getMessage());
                toastContent.setValue("Маршрут построить не удалось");
                // обновляем состяоние процесса построения маршурат (кнопка включается, прогрессбар скрывается)
                requestToUpdateProgressStatusBuildingRoute.setValue(false);
            }
        });
    }

    public void requestListOfLocationPointsInfo(int remainingNumberOfScanning){
        retrofit.getListOfAllMapPoints().enqueue(new Callback<ListOfAllMapPoints>() {
            @Override
            public void onResponse(Call<ListOfAllMapPoints> call, Response<ListOfAllMapPoints> response) {
                if (response.body()==null) {
                    Log.i("LocationDetectionRep", "server response: is null");

                    // повторный запрос на точки
                    if (remainingNumberOfScanning>0){
                        requestListOfLocationPointsInfo(remainingNumberOfScanning-1);
                    }

                    return;
                }
                Log.i("LocationDetectionRep", "server response about allInfo: "+response.body().toString());
                Map<FloorId, List<MapPoint>> converted = response.body().convertToMap();
                mapImageManager.setDataOnPointsOnAllFloors(converted);
                // отправляем данны во все поисковые строки с автодополнением
                requestToAddAllPointsDataInAutoFinders.setValue(converted);
                Log.i("LocationDetectionRep", "after convert List of allInfo: "+converted.toString());

            }

            @Override
            public void onFailure(Call<ListOfAllMapPoints> call, Throwable t) {
                Log.i("LocationDetectionRep", "server error: "+ Arrays.toString(t.getStackTrace()));
            }
        });
    }
}
