package com.mrkiriss.wifilocalpositioning.repositiries;

import android.net.wifi.ScanResult;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.FloorSchemasDownloader;
import com.mrkiriss.wifilocalpositioning.managers.WifiScanner;
import com.mrkiriss.wifilocalpositioning.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.models.server.AccessPoint;
import com.mrkiriss.wifilocalpositioning.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.network.IMWifiServerApi;
import com.mrkiriss.wifilocalpositioning.ui.view.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
public class LocationDetectionRepository {

    private IMWifiServerApi retrofit;
    private WifiScanner wifiScanner;
    private FloorSchemasDownloader floorSchemasDownloader;

    private MutableLiveData<List<ScanResult>> kitOfScanResults;
    private MutableLiveData<MapPoint> resultOfDefinition;
    private MutableLiveData<Floor> changeFloor;

    private List<Floor> floors;

    public LocationDetectionRepository(IMWifiServerApi retrofit, WifiScanner wifiScanner, FloorSchemasDownloader floorSchemasDownloader){
        this.retrofit=retrofit;
        this.wifiScanner=wifiScanner;
        this.floorSchemasDownloader=floorSchemasDownloader;

        kitOfScanResults=wifiScanner.getScanResults();

        resultOfDefinition=new MutableLiveData<>();
        changeFloor=new MutableLiveData<>();
        floors=new ArrayList<>();

        startInfinityScanning();

        testMap();
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
        handler.postDelayed(task, 4000);
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
    private void startInfinityScanning(){
        wifiScanner.startInfinityScanning();
    }
    public void startProcessingScanResultKit(List<ScanResult> scanResults){

        CalibrationLocationPoint calibrationLocationPoint = new CalibrationLocationPoint();
        List<AccessPoint> accessPoints = new ArrayList<>();

        for (ScanResult scanResult : scanResults){
            accessPoints.add(new AccessPoint(scanResult.BSSID, scanResult.level));
        }

        // занесли набор в список наборов у калибровочной точки
        calibrationLocationPoint.addCalibrationSet(accessPoints);

        // отправляем запрос на сервер
        //postFromDefinitionWithCabinet(calibrationLocationPoint);
    }

    // server
    private void postFromDefinitionWithCabinet(CalibrationLocationPoint calibrationLocationPoint){
        retrofit.defineLocationWithCabinet(calibrationLocationPoint).enqueue(new Callback<DefinedLocationPoint>() {
            @Override
            public void onResponse(Call<DefinedLocationPoint> call, Response<DefinedLocationPoint> response) {
                Log.println(Log.INFO, "GOOD_DEFINITION_ROOM",
                        String.format("Server response=%s", response.body()));

                if (response.body()==null) return;

                resultOfDefinition.setValue(convertToMapPoint(response.body()));
            }
            @Override
            public void onFailure(Call<DefinedLocationPoint> call, Throwable t) {
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }

    // scanning result convert
    private MapPoint convertToMapPoint(DefinedLocationPoint definedLocationPoint){
        MapPoint result = new MapPoint();

        Floor foundFloor = findFloorById(definedLocationPoint.getFloorId());
        if (foundFloor==null) foundFloor=downloadSingleFloorForPointer(Floor.convertFloorIdToEnum(definedLocationPoint.getFloorId()),
                definedLocationPoint.getX(), definedLocationPoint.getY());

        result.setFloorWithPointer(foundFloor);
        result.setX(definedLocationPoint.getX());
        result.setY(definedLocationPoint.getY());
        result.setTag(definedLocationPoint.getRoomName());
        return result;
    }
}
