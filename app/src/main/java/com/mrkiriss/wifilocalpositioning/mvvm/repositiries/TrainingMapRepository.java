package com.mrkiriss.wifilocalpositioning.mvvm.repositiries;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.ListOfAllMapPoints;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.models.server.StringResponse;
import com.mrkiriss.wifilocalpositioning.data.sources.IMWifiServerApi;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;

import java.io.Serializable;
import java.sql.DriverPropertyInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
public class TrainingMapRepository implements Serializable {

    private final IMWifiServerApi retrofit;
    private final MapImageManager mapImageManager;

    private final MutableLiveData<String> toastContent;
    private final MutableLiveData<Floor> changeFloor;
    private final MutableLiveData<String> serverResponse;

    public TrainingMapRepository(IMWifiServerApi retrofit, MapImageManager mapImageManager){
        this.retrofit=retrofit;
        this.mapImageManager = mapImageManager;

        toastContent=new MutableLiveData<>();
        changeFloor=new MutableLiveData<>();
        serverResponse=new MutableLiveData<>();

    }

    // обновление данных о всех точках на всех этажах через сервер
    public void startDownloadingDataOnPointsOnAllFloors(){
        retrofit.getListOfAllMapPoints().enqueue(new Callback<ListOfAllMapPoints>() {
            @Override
            public void onResponse(Call<ListOfAllMapPoints> call, Response<ListOfAllMapPoints> response) {
                if (response.body()==null) {
                    Log.i("TrainingMapRep", "server response: is null");
                    return;
                }
                Log.i("TrainingMapRep", "server response: "+response.body().toString());
                serverResponse.setValue(response.body().toString());
                mapImageManager.setDataOnPointsOnAllFloors(response.body().convertToMap());
            }

            @Override
            public void onFailure(Call<ListOfAllMapPoints> call, Throwable t) {
                Log.i("TrainingMapRep", "server error: "+ Arrays.toString(t.getStackTrace()));
                serverResponse.setValue(Arrays.toString(t.getStackTrace()));
            }
        });
    }

    // изменение картинки текущего этажа
    public void changeFloor(int floorIdInt, boolean needToDisplayPoints){
        FloorId floorId = Floor.convertFloorIdToEnum(floorIdInt);
        if (needToDisplayPoints){
            if (mapImageManager.getDataOnPointsOnAllFloors().containsKey(floorId)){
                toastContent.setValue("Ошибка! Данные о точках от сервера отсутсвуют!");
                return;
            }
            Floor needfulFloor = mapImageManager.getFloorWithPointers(mapImageManager.getDataOnPointsOnAllFloors().get(floorId), floorId);
            changeFloor.setValue(needfulFloor);
        }else {
            Floor foundFloor = mapImageManager.getBasicFloor(floorId);
            changeFloor.setValue(foundFloor);
        }
    }
    public void changeFloor(int floorIdInt, boolean needToDisplayPoints, MapPoint mapPoint){
        FloorId floorId = Floor.convertFloorIdToEnum(floorIdInt);
        if (needToDisplayPoints){
            if (mapImageManager.getDataOnPointsOnAllFloors().containsKey(floorId)){
                toastContent.setValue("Ошибка! Данные о точках от сервера отсутсвуют!");
                return;
            }
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
        }
    }

    // обучение сервера информации о точке
    private void postFromTrainingWithCoordinates(LocationPointInfo locationPointInfo){
        toastContent.setValue("Обучение координатам началось");
        retrofit.postCalibrationLPInfo(locationPointInfo).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "GOOD_TRAINING_CORD_ROOM",
                        String.format("Server response=%s", response.body()));
                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    return;
                }
                serverResponse.setValue(response.body().toString());
            }
            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
    // scanning result convert
    private MapPoint convertToMapPoint(DefinedLocationPoint definedLocationPoint){
        return new MapPoint(definedLocationPoint.getX(),
                definedLocationPoint.getY(), definedLocationPoint.getRoomName());
    }
}
