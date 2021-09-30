package com.mrkiriss.wifilocalpositioning.data.repositiries;

import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.AccessPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.models.server.Connections;
import com.mrkiriss.wifilocalpositioning.data.models.server.ListOfAllMapPoints;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.models.server.ScanInformation;
import com.mrkiriss.wifilocalpositioning.data.models.server.StringResponse;
import com.mrkiriss.wifilocalpositioning.data.sources.remote.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
public class TrainingMapRepository implements Serializable {

    private final LocationDataApi retrofit;
    private final MapImageManager mapImageManager;
    private WifiScanner wifiScanner;

    private final MutableLiveData<String> toastContent;
    private final MutableLiveData<Floor> changeFloor;
    private final MutableLiveData<Boolean> requestToUpdateInteractionWithServerIsCarriedOut;
    private final MutableLiveData<String> requestToUpdateDescriptionOfInteractionWithServer;
    private final MutableLiveData<String> serverResponse;
    private final MutableLiveData<List<MapPoint>> serverConnectionsResponse;
    private final MutableLiveData<String> requestToUpdateFloor;
    private final MutableLiveData<List<ScanInformation>> requestToSetListOfScanInformation;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private LiveData<Integer> remainingNumberOfScanning;
    private CalibrationLocationPoint calibrationLocationPoint;

    @Inject
    public TrainingMapRepository(LocationDataApi retrofit, MapImageManager mapImageManager, WifiScanner wifiScanner){
        this.retrofit=retrofit;
        this.mapImageManager = mapImageManager;
        this.wifiScanner=wifiScanner;

        toastContent=new MutableLiveData<>();
        changeFloor=new MutableLiveData<>();
        requestToUpdateInteractionWithServerIsCarriedOut = new MutableLiveData<>();
        requestToUpdateDescriptionOfInteractionWithServer = new MutableLiveData<>();
        serverResponse=new MutableLiveData<>();
        serverConnectionsResponse=new MutableLiveData<>();
        requestToUpdateFloor=new MutableLiveData<>();
        requestToSetListOfScanInformation=new MutableLiveData<>();

        completeKitsOfScansResult=wifiScanner.getCompleteScanResults();
        remainingNumberOfScanning=wifiScanner.getRemainingNumberOfScanning();
    }

    // изменение картинки текущего этажа
    public void changeFloor(int floorIdInt, boolean needToDisplayPoints){
        FloorId floorId = Floor.convertFloorIdToEnum(floorIdInt);
        if (needToDisplayPoints){
            Log.i("TrainingMapRepository","data of allInfo contains floorId="+mapImageManager.getDataOnPointsOnAllFloors().containsKey(floorId));
            if (checkContainsInCurrentData(floorId)) return;

            Floor needfulFloor = mapImageManager.getFloorWithPointers(mapImageManager.getDataOnPointsOnAllFloors().get(floorId), floorId);
            changeFloor.setValue(needfulFloor);
            Log.i("TrainingMapRepository","change floor on floor with points with id="+floorId+" bitmap="+needfulFloor.getFloorSchema());

        }else {
            Floor needfulFloor = mapImageManager.getBasicFloor(floorId);
            changeFloor.setValue(needfulFloor);
            Log.i("TrainingMapRepository","change floor on basic with id="+floorId+" bitmap="+needfulFloor.getFloorSchema());
        }
    }
    public void changeFloor(int floorIdInt, boolean needToDisplayPoints, MapPoint mapPoint){
        FloorId floorId = Floor.convertFloorIdToEnum(floorIdInt);
        if (needToDisplayPoints){
            if (checkContainsInCurrentData(floorId)) return;

            Floor needfulFloor = mapImageManager.getFloorWithPointers(mapImageManager.getDataOnPointsOnAllFloors().get(floorId), floorId);
            // к текущему состоянию пола пририсовываем маркер по требуемым коодинатам
            Bitmap changedBitmap = mapImageManager.mergePointerAndBitmap(needfulFloor.getFloorSchema(), mapPoint.getX(), mapPoint.getY());
            needfulFloor.setFloorSchema(changedBitmap);
            changeFloor.setValue(needfulFloor);
        }else {
            Floor needfulFloor = mapImageManager.getBasicFloor(floorId);
            // к текущему состоянию пола пририсовываем маркер по требуемым коодинатам
            Bitmap changedBitmap = mapImageManager.mergePointerAndBitmap(needfulFloor.getFloorSchema(), mapPoint.getX(), mapPoint.getY());
            needfulFloor.setFloorSchema(changedBitmap);
            changeFloor.setValue(needfulFloor);
            Log.i("TrainingMapRepository","change floor on basic with selected point with id="+floorId+" bitmap="+needfulFloor.getFloorSchema());
        }
    }
    private boolean checkContainsInCurrentData(FloorId floorId){
        if (!mapImageManager.getDataOnPointsOnAllFloors().containsKey(floorId) || mapImageManager.getDataOnPointsOnAllFloors().get(floorId)==null){
            toastContent.setValue("Ошибка! Данные о точках от сервера отсутсвуют!");
            changeFloor.setValue(null);
            return true;
        }
        return false;
    }

    // поиск ближайшей точки
    public MapPoint findMapPointInCurrentData(int x, int y, int floorInt){
        MapPoint result = new MapPoint();
        double minDelta=Double.MAX_VALUE;
        final double maxError = 100d;
        FloorId floorId = Floor.convertFloorIdToEnum(floorInt);
        if (!checkContainsInCurrentData(floorId)){
            for (MapPoint mapPoint: mapImageManager.getDataOnPointsOnAllFloors().get(floorId)){
                double currentDelta = Math.pow((mapPoint.getX()-x)*(mapPoint.getX()-x)+(mapPoint.getY()-y)*(mapPoint.getY()-y),0.5);
                if (currentDelta<minDelta && currentDelta<maxError){
                    result=mapPoint;
                    minDelta=currentDelta;
                }
            }
        }
        Log.i("TrainingMapRepository", "результат поска ближайшей точки ="+result.toStringAllObject());

        return result;
    }
    // запуск сканирования
    public void runScanInManager(int numberOfScanningKits, String roomName){
        calibrationLocationPoint=new CalibrationLocationPoint();
        calibrationLocationPoint.setRoomName(roomName);
        wifiScanner.startTrainingScan(numberOfScanningKits, WifiScanner.TypeOfScanning.TRAINING);
    }
    public void processCompleteKitsOfScanResults(CompleteKitsContainer completeKitsContainer){

        if (completeKitsContainer.getRequestSourceType()!=WifiScanner.TypeOfScanning.TRAINING) return;

        for (List<ScanResult> oneScanResults: completeKitsContainer.getCompleteKits()) {
            List<AccessPoint> accessPoints = new ArrayList<>();
            for (ScanResult scanResult : oneScanResults) {
                accessPoints.add(new AccessPoint(scanResult.BSSID, scanResult.level));
            }
            if (calibrationLocationPoint==null) return;
            calibrationLocationPoint.addOneCalibrationSet(accessPoints);
        }

        postFromTrainingWithAPs();
    }


    // -----SERVER-----
    // обновление данных о всех точках на всех этажах через сервер
    public void startDownloadingDataOnPointsOnAllFloors(){
        // обновление информации о запросе к серверу
        serverResponse.setValue("Запрос отправлен на сервер. Ждёмс");
        requestToUpdateInteractionWithServerIsCarriedOut.setValue(true);
        requestToUpdateDescriptionOfInteractionWithServer.setValue("Запрос на обновление данных о доступных точках обрабатывается");

        retrofit.getListOfAllMapPoints().enqueue(new Callback<ListOfAllMapPoints>() {
            @Override
            public void onResponse(Call<ListOfAllMapPoints> call, Response<ListOfAllMapPoints> response) {

                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);

                if (response.body()==null) {
                    Log.i("TrainingMapRep", "server response: is null");
                    return;
                }

                Log.i("TrainingMapRep", "server response about allInfo: "+response.body().toString());
                serverResponse.setValue(response.body().toString());

                Map<FloorId, List<MapPoint>> converted = response.body().convertToMap();
                mapImageManager.setDataOnPointsOnAllFloors(converted);
                Log.i("TrainingMapRep", "after convert List of allInfo: "+converted.toString());

                // провоцируем оновление картинки
                requestToUpdateFloor.setValue("Данные о точках получены");

            }

            @Override
            public void onFailure(Call<ListOfAllMapPoints> call, Throwable t) {
                Log.i("TrainingMapRep", "server error: "+ Arrays.toString(t.getStackTrace()));

                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);
                serverResponse.setValue(Arrays.toString(t.getStackTrace()));
            }
        });
    }
    // обучение сервера информации о точке
    public void postFromTrainingWithCoordinates(int intX, int intY, String inputCabId, int floorId, String isRoom){

        // сгладить координаты, чтобы они находились на одной прямой
        intX-=intX%5;
        intY-=intY%5;

        if (inputCabId.isEmpty()) inputCabId=intX+"_"+intY;
        LocationPointInfo locationPointInfo = new LocationPointInfo(intX, intY, inputCabId, floorId, isRoom);

        // обновление информации о запросе к серверу
        requestToUpdateInteractionWithServerIsCarriedOut.setValue(true);
        requestToUpdateDescriptionOfInteractionWithServer.setValue("Запрос на добавление локации обрабатывается");
        serverResponse.setValue("Запрос отправлен на сервер. Ждёмс");

        retrofit.postCalibrationLPInfo(locationPointInfo).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "GOOD_TRAINING_CORD_ROOM",
                        String.format("Server response=%s", response.body()));

                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);

                if (response.body() == null){
                    // обновление информации о запросе к серверу
                    serverResponse.setValue("Response body is null");
                    toastContent.setValue("Ошибка! Добавление провалилось");
                    return;
                }

                // обновление информации о запросе к серверу
                serverResponse.setValue(response.body().toString());
                toastContent.setValue("Локация добавлена");
            }
            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                // обновление информации о запросе к серверу
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);

                toastContent.setValue("Ошибка! Добавление провалилось");
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
    private void saveNewLPInfoIntoLocaleMap(LocationPointInfo locationPointInfo){
        FloorId floorId = Floor.convertFloorIdToEnum(locationPointInfo.getFloorId());
        if (!mapImageManager.getDataOnPointsOnAllFloors().containsKey(floorId)){
            mapImageManager.getDataOnPointsOnAllFloors().put(floorId, new ArrayList<>());
        }
        List<MapPoint> list = mapImageManager.getDataOnPointsOnAllFloors().get(floorId);
        list.add(new MapPoint(locationPointInfo.getX(), locationPointInfo.getY(), locationPointInfo.getRoomName(), locationPointInfo.isRoom()));
    }

    // отправить точку с её информацией о APs
    private void postFromTrainingWithAPs(){
        // обновление информации о запросе к серверу
        requestToUpdateInteractionWithServerIsCarriedOut.setValue(true);
        requestToUpdateDescriptionOfInteractionWithServer.setValue("Запрос на обработку результатов сканирования обрабатывается");

        retrofit.postCalibrationLPWithAPs(calibrationLocationPoint).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "GOOD_TRAINING_APs_ROOM",
                        String.format("Server response=%s", response.body()));

                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);

                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    toastContent.setValue("Обработка сканирований провалилась");
                    return;
                }

                serverResponse.setValue(response.body().getResponse());
                toastContent.setValue("Сканирования успешно обработаны");

            }
            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {

                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());

                toastContent.setValue("Обработка сканирований провалилась");

                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
    // получение списка информации о сканированиях для точки
    public void getScanningInformationAboutLocation(String locationName){
        if (locationName==null || locationName.isEmpty()){
            toastContent.setValue("Имя некорректно");
            return;
        }

        // обновление информации о запросе к серверу
        requestToUpdateInteractionWithServerIsCarriedOut.setValue(true);
        requestToUpdateDescriptionOfInteractionWithServer.setValue("Запрос получение информации о сканированиях точки обрабатывается");

        retrofit.getScanningInfoAboutLocation(locationName).enqueue(new Callback<List<ScanInformation>>() {
            @Override
            public void onResponse(Call<List<ScanInformation>> call, Response<List<ScanInformation>> response) {
                Log.println(Log.INFO, "TrainingMapRepository",
                        String.format("Server response after getScanningInfoAboutLocation=%s", response.body()));

                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);

                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    return;
                }
                serverResponse.setValue(response.body().toString());

                // отправляем на вставку в RecyclerView предварительно удалив объекты с невалидным именем
                requestToSetListOfScanInformation.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<ScanInformation>> call, Throwable t) {
                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());

                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
    private List<ScanInformation> deleteInvalidInfoByName(List<ScanInformation> info){
        List<ScanInformation> result = new ArrayList<>();
        for(ScanInformation scanInformation:info){
            if (scanInformation.getDate()!=null && !scanInformation.getDate().isEmpty()) result.add(scanInformation);
        }
        return result;
    }

    // получение связей по имени для добавления на экран в RecyclerView
    public void startDownloadingConnections(String mainName){
        // обновление информации о запросе к серверу
        requestToUpdateInteractionWithServerIsCarriedOut.setValue(true);
        requestToUpdateDescriptionOfInteractionWithServer.setValue("Запрос получение связей точки " + mainName + " обрабатывается");
        serverResponse.setValue("Запрос отправлен на сервер. Ждёмс");

        retrofit.getConnectionsByName(mainName).enqueue(new Callback<Connections>() {
            @Override
            public void onResponse(Call<Connections> call, Response<Connections> response) {
                Log.println(Log.INFO, "TrainingMapRepository",
                        String.format("Server response after downloading connections=%s", response.body()));

                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);

                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    return;
                }

                serverResponse.setValue(response.body().toString());

                if (response.body().getMainRoomName()!= null) {
                    serverConnectionsResponse.setValue(response.body().convertToListOfMapPoints());
                }else{
                    serverResponse.setValue("Точки на сервере не существует");
                }
            }
            @Override
            public void onFailure(Call<Connections> call, Throwable t) {
                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());

                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
    // отправка на сервер изменённых пользователем связей
    public void postChangedConnections(List<MapPoint> mapPoints, String mainName){
        // обновление информации о запросе к серверу
        requestToUpdateInteractionWithServerIsCarriedOut.setValue(true);
        requestToUpdateDescriptionOfInteractionWithServer.setValue("Запрос на обновление связей точки " + mainName + "обрабатывается");
        serverResponse.setValue("Запрос отправлен на сервер. Ждёмс");

        retrofit.postConnections(Connections.convertToOnlyNameConnections(mainName, mapPoints)).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "TrainingMapRepository",
                        String.format("Server response after postChangedConnections=%s", response.body()));

                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);

                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    toastContent.setValue("Данные о точке изменить не удалось");
                    return;
                }

                serverResponse.setValue(response.body().getResponse());
                toastContent.setValue("Данные обновлены");
            }

            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());

                Log.e("SERVER_ERROR", t.getMessage());
                toastContent.setValue("Данные о точке изменить не удалось");
            }
        });
    }

    // удаление информации о точки, самой точки с её информацией о APs
    public void deleteLocationPointInfoOnServer(String roomName){
        // обновление информации о запросе к серверу
        requestToUpdateInteractionWithServerIsCarriedOut.setValue(true);
        requestToUpdateDescriptionOfInteractionWithServer.setValue("Запрос на удаление точки " + roomName + "обрабатывается");
        serverResponse.setValue("Запрос отправлен на сервер. Ждёмс");

        retrofit.deleteLPInfo(roomName).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "TrainingMapRepository",
                        String.format("Server response after delete lpInfo=%s", response.body()));

                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);

                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    toastContent.setValue("Удалить точку не удалось");
                    return;
                }

                serverResponse.setValue(response.body().getResponse());
                toastContent.setValue("Тока удалена");
            }

            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());

                Log.e("SERVER_ERROR", t.getMessage());

                toastContent.setValue("Удалить точку не удалось");
            }
        });
    }
    public void deleteLocationPointOnServer(String roomName){
        // обновление информации о запросе к серверу
        requestToUpdateInteractionWithServerIsCarriedOut.setValue(true);
        requestToUpdateDescriptionOfInteractionWithServer.setValue("Запрос на удаление сканирований точки " + roomName + "обрабатывается");
        serverResponse.setValue("Запрос отправлен на сервер. Ждёмс");

        retrofit.deleteLPAps(roomName).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "TrainingMapRepository",
                        String.format("Server response after delete lpAPs=%s", response.body()));

                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);

                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    toastContent.setValue("Сканирования не удалены");
                    return;
                }

                serverResponse.setValue(response.body().getResponse());

                toastContent.setValue("Сканирования удалены");
            }

            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                // обновление информации о запросе к серверу
                requestToUpdateInteractionWithServerIsCarriedOut.setValue(false);
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());

                Log.e("SERVER_ERROR", t.getMessage());

                toastContent.setValue("Сканирования не удалены");
            }
        });
    }

}
