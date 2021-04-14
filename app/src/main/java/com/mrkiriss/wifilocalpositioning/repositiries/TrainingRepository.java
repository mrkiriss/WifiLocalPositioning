package com.mrkiriss.wifilocalpositioning.repositiries;

import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.managers.WifiScanner;
import com.mrkiriss.wifilocalpositioning.models.AccessPoint;
import com.mrkiriss.wifilocalpositioning.models.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.models.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.models.StringResponse;
import com.mrkiriss.wifilocalpositioning.network.IMWifiServerApi;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
public class TrainingRepository {

    private IMWifiServerApi retrofit;
    private WifiScanner wifiScanner;

    private CalibrationLocationPoint calibrationLocationPoint;
    private int scanningMode;
    private int requiredNumberOfScanningKits;
    private boolean requestPosted;
    public static final int MODE_TRAINING=1;
    public static final int MODE_DEFINITION=2;
    public static final int MODE_TRAINING2=3;
    public static final int MODE_DEFINITION2=4;

    private final LiveData<List<ScanResult>> kitOfScanResults;
    private MutableLiveData<String> requestToAddAPs;
    private MutableLiveData<String> resultOfScanningAfterCalibration;
    private MutableLiveData<String> toastContent;


    public TrainingRepository(IMWifiServerApi retrofit, WifiScanner wifiScanner){
        this.retrofit=retrofit;
        this.wifiScanner=wifiScanner;

        kitOfScanResults=wifiScanner.getScanResults();

        requestToAddAPs=new MutableLiveData<>();
        resultOfScanningAfterCalibration =new MutableLiveData<>();
        toastContent=new MutableLiveData<>();
    }

    public void runScanInManager(int numberOfScanningKits, int lat, int lon, int radioMode){
        Log.println(Log.INFO, "START_SCANNING", String.format("Parameters: numberOfScanning=%s, lat=%s, lon=%s", numberOfScanningKits, lat, lon));

        calibrationLocationPoint=new CalibrationLocationPoint();
        calibrationLocationPoint.setLat(lat);
        calibrationLocationPoint.setLon(lon);

        scanningMode=radioMode;
        requiredNumberOfScanningKits=numberOfScanningKits;
        requestPosted=false;

        wifiScanner.startScanningWithParameters(numberOfScanningKits);
    }

    public void runScanInManager(int numberOfScanningKits, String roomName, int radioMode){
        Log.println(Log.INFO, "START_SCANNING", String.format("Parameters: numberOfScanning=%s, roomName=%s, radioMode=%s", numberOfScanningKits, roomName, radioMode));

        calibrationLocationPoint=new CalibrationLocationPoint();
        calibrationLocationPoint.setRoomName(roomName);

        scanningMode=radioMode;
        requiredNumberOfScanningKits=numberOfScanningKits;
        requestPosted=false;

        wifiScanner.startScanningWithParameters(numberOfScanningKits);
    }

    public void runCleaningServer(){
        retrofit.cleanServer().enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                if (response.body() == null) {
                    Log.println(Log.INFO, "CLEANING_SUCCESSFUL",
                            String.format("Parameters: server response=%s", response.body()));
                    return;
                }
                toastContent.setValue(response.body().getResponse());
                Log.println(Log.INFO, "CLEANING_SUCCESSFUL",
                        String.format("Parameters: server response=%s", response.body().getResponse()));
            }

            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                Log.println(Log.INFO, "CLEANING_FAILED",
                        String.format("Parameters: server response=%s", t.getMessage()));
            }
        });
    }

    public void startProcessingScanResultKit(List<ScanResult> scanResults, int numberOfCurrentSuccessfulKits){

        if (requestPosted) return;

        List<AccessPoint> accessPoints = new ArrayList<>();
        for (ScanResult scanResult : scanResults){
            accessPoints.add(new AccessPoint(scanResult.BSSID, scanResult.level));
        }

        Log.println(Log.INFO, "START_PROC_SCAN_RESULT",
                String.format("Parameters: numberOfCurrentSuccessfulKits=%s, value=%s", numberOfCurrentSuccessfulKits,
                        convertKitToString(accessPoints, numberOfCurrentSuccessfulKits)));

        if (calibrationLocationPoint==null) return;
        // занесли набор в список наборов у калибровочной точки
        calibrationLocationPoint.addCalibrationSet(accessPoints);
        // создаём запрос на добавление информации набора на экран пользователя
        requestToAddAPs.setValue(convertKitToString(accessPoints, numberOfCurrentSuccessfulKits));

        // проверяем законченность блока сканирования
        if (numberOfCurrentSuccessfulKits==requiredNumberOfScanningKits || wifiScanner.getNumberOfScanning()<1){
            postCalibrationLPToServer();
        }
    }
    private String convertKitToString(List<AccessPoint> accessPoints, int number){
        String result="Набор №"+number+"\n";
        for (AccessPoint accessPoint: accessPoints){
            result+=accessPoint.toString()+"\n";
        }
        return result;
    }

    private void postCalibrationLPToServer(){
        requestPosted=true;
        switch (scanningMode){
            case MODE_TRAINING:
                postFromTrainingWithCoord();
                break;
            case MODE_DEFINITION:
                postFromDefinitionWithCoord();
                break;
            case MODE_TRAINING2:
                postFromTrainingWithCabinet();
                break;
            case MODE_DEFINITION2:
                postFromDefinitionWithCabinet();
                break;
        }
    }
    private void postFromTrainingWithCoord(){
        toastContent.setValue("Обучение началось");
        retrofit.postLPCalibrationWithCoordinateForTraining(calibrationLocationPoint).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "GOOD_TRAINING_COORD",
                        String.format("Server response=%s", response.body()));
                if (response.body()==null)return;
                resultOfScanningAfterCalibration.setValue(response.body().getResponse());
            }
            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                resultOfScanningAfterCalibration.setValue(call.toString()+"\n"+t.getMessage());
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
    private void postFromDefinitionWithCoord(){
        toastContent.setValue("Определение началось");
        retrofit.defineLocation(calibrationLocationPoint).enqueue(new Callback<DefinedLocationPoint>() {
            @Override
            public void onResponse(Call<DefinedLocationPoint> call, Response<DefinedLocationPoint> response) {
                Log.println(Log.INFO, "GOOD_DEFINITION",
                        String.format("Server response=%s", response.body()));
                if (response.body()==null)return;
                resultOfScanningAfterCalibration.setValue(response.body().toString());
            }

            @Override
            public void onFailure(Call<DefinedLocationPoint> call, Throwable t) {
                resultOfScanningAfterCalibration.setValue(call.toString()+"\n"+t.getMessage());
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }

    private void postFromTrainingWithCabinet(){
        retrofit.postLPCalibrationWithCabinetForTraining(calibrationLocationPoint).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "GOOD_TRAINING_ROOM",
                        String.format("Server response=%s", response.body()));
                if (response.body()==null)return;
                resultOfScanningAfterCalibration.setValue(response.body().getResponse());
            }
            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                resultOfScanningAfterCalibration.setValue(call.toString()+"\n"+t.getMessage());
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
    private void postFromDefinitionWithCabinet(){
        toastContent.setValue("Определение кабинета началось");
        retrofit.defineLocationWithCabinet(calibrationLocationPoint).enqueue(new Callback<DefinedLocationPoint>() {
            @Override
            public void onResponse(Call<DefinedLocationPoint> call, Response<DefinedLocationPoint> response) {
                Log.println(Log.INFO, "GOOD_DEFINITION_ROOM",
                        String.format("Server response=%s", response.body()));
                if (response.body()==null)return;
                resultOfScanningAfterCalibration.setValue(response.body().toString());
            }
            @Override
            public void onFailure(Call<DefinedLocationPoint> call, Throwable t) {
                resultOfScanningAfterCalibration.setValue(call.toString()+"\n"+t.getMessage());
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }

    public void unregisterScanCallbacks(){
        wifiScanner.unregisterScanCallback();
    }
}
