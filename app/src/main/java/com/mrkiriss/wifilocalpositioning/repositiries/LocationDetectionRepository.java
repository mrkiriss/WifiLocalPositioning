package com.mrkiriss.wifilocalpositioning.repositiries;

import android.graphics.LinearGradient;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.sources.FloorSchemasDownloader;
import com.mrkiriss.wifilocalpositioning.data.sources.wifi.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.AccessPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.sources.IMWifiServerApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
public class LocationDetectionRepository implements Serializable {

    private final IMWifiServerApi retrofit;
    private final WifiScanner wifiScanner;
    private final FloorSchemasDownloader floorSchemasDownloader;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private final MutableLiveData<MapPoint> resultOfDefinition;
    private final MutableLiveData<Floor> changeFloor;

    private List<Floor> floors;

    public LocationDetectionRepository(IMWifiServerApi retrofit, WifiScanner wifiScanner, FloorSchemasDownloader floorSchemasDownloader){
        this.retrofit=retrofit;
        this.wifiScanner=wifiScanner;
        this.floorSchemasDownloader=floorSchemasDownloader;

        completeKitsOfScansResult=wifiScanner.getCompleteScanResults();

        resultOfDefinition=new MutableLiveData<>();
        changeFloor=new MutableLiveData<>();
        floors=new ArrayList<>();

        wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);

        //testMap();
    }
    private int x=0;
    private int y=630;
    private int count=30;
    private Handler handler = new android.os.Handler();
    private void testMap(){
        onePostMapPoint();
    }
    private void onePostMapPoint(){
        Log.i("test/onePostMapPoint", "Запрос изменения местоположения, осталось "+count);
        if (count<0) return;
        count--;
        MapPoint mp = new MapPoint();
        if (x%160==0) {
            mp.setFloorWithPointer(downloadSingleFloorForPointer(FloorId.SECOND_FLOOR, x, 610));
        }else{
            mp.setFloorWithPointer(downloadSingleFloorForPointer(FloorId.FIRST_FLOOR, x, 610));
        }
        x+=40;
        y+=0;
        resultOfDefinition.setValue(mp);
        Runnable task = ()-> onePostMapPoint();
        handler.postDelayed(task, 1000);
    }


    // floor
    public void changeFloor(int floorIdInt){
        Floor foundFloor=findFloorById(floorIdInt);
        if (foundFloor==null) foundFloor=downloadSimpleSingleFloor(Floor.convertFloorIdToEnum(floorIdInt));
            changeFloor.setValue(foundFloor);
    }
    private Floor findFloorById(int requiredFloorIdInt){
        FloorId floorId = Floor.convertFloorIdToEnum(requiredFloorIdInt);

        for (Floor floor : floors){
            if (floor.getFloorId()==floorId){
                return floor;
            }
        }

        return null;
    }
    private Floor downloadSimpleSingleFloor(FloorId floorId){
        Floor result = floorSchemasDownloader.downloadFloor(floorId);
        floors.add(result);
        return result;
    }
    private Floor downloadSingleFloorForPointer(FloorId floorId, int x, int y){
        Floor result = floorSchemasDownloader.downloadFloor(floorId, x, y);
        return result;
    }

    // scanning
    public void startProcessingCompleteKitsOfScansResult(CompleteKitsContainer completeKitsContainer){

        if (completeKitsContainer.getRequestSourceType()!=WifiScanner.TYPE_DEFINITION) return;

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
        postFromDefinitionWithCabinet(calibrationLocationPoint);
    }

    // server
    private void postFromDefinitionWithCabinet(CalibrationLocationPoint calibrationLocationPoint){

        if (calibrationLocationPoint.getCalibrationSets()==null || calibrationLocationPoint.getCalibrationSets().size()==0){
            wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);
            return;
        }

        retrofit.defineLocation(calibrationLocationPoint).enqueue(new Callback<DefinedLocationPoint>() {
            @Override
            public void onResponse(Call<DefinedLocationPoint> call, Response<DefinedLocationPoint> response) {
                wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);

                Log.println(Log.INFO, "GOOD_DEFINITION_ROOM",
                        String.format("Server definition=%s", response.body()));

                if (response.body()==null || response.body().getFloorId()==-1) return;

                Log.println(Log.INFO, "SEND_CONVERT_RESULT",
                        convertToMapPoint(response.body()).toString());

                resultOfDefinition.setValue(convertToMapPoint(response.body()));
            }
            @Override
            public void onFailure(Call<DefinedLocationPoint> call, Throwable t) {
                Log.e("SERVER_ERROR", t.getMessage());
                wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);
            }
        });
    }

    // scanning result convert
    private MapPoint convertToMapPoint(DefinedLocationPoint definedLocationPoint){
        MapPoint result = new MapPoint();

        Floor foundFloor=downloadSingleFloorForPointer(Floor.convertFloorIdToEnum(definedLocationPoint.getFloorId()),
                definedLocationPoint.getX(), definedLocationPoint.getY());

        result.setFloorWithPointer(foundFloor);
        result.setX(definedLocationPoint.getX());
        result.setY(definedLocationPoint.getY());
        result.setTag(definedLocationPoint.getRoomName());
        return result;
    }
}
