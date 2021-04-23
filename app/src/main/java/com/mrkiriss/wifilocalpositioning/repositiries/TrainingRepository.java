package com.mrkiriss.wifilocalpositioning.repositiries;

import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.server.AccessPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.models.server.StringResponse;
import com.mrkiriss.wifilocalpositioning.data.sources.IMWifiServerApi;
import com.mrkiriss.wifilocalpositioning.data.sources.wifi.WifiScanner;

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
    public static final int MODE_TRAINING_APS=1;
    public static final int MODE_TRAINING_COORD=2;
    public static final int MODE_DEFINITION=3;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private MutableLiveData<String> requestToAddAPs;
    private MutableLiveData<String> serverResponse;
    private MutableLiveData<String> toastContent;
    private LiveData<Integer> remainingNumberOfScanning;


    public TrainingRepository(IMWifiServerApi retrofit, WifiScanner wifiScanner){
        this.retrofit=retrofit;
        this.wifiScanner=wifiScanner;

        completeKitsOfScansResult=wifiScanner.getCompleteScanResults();
        remainingNumberOfScanning=wifiScanner.getRemainingNumberOfScanning();

        requestToAddAPs=new MutableLiveData<>();
        serverResponse =new MutableLiveData<>();
        toastContent=new MutableLiveData<>();
    }

    public void runScanInManager(int numberOfScanningKits, int radioMode){
        calibrationLocationPoint=new CalibrationLocationPoint();

        scanningMode=radioMode;
        requiredNumberOfScanningKits=numberOfScanningKits;

        wifiScanner.startTrainingScan(numberOfScanningKits, WifiScanner.TYPE_TRAINING);
    }
    public void runScanInManager(int numberOfScanningKits, String roomName, int radioMode){

        calibrationLocationPoint=new CalibrationLocationPoint();
        calibrationLocationPoint.setRoomName(roomName);

        scanningMode=radioMode;
        requiredNumberOfScanningKits=numberOfScanningKits;

        wifiScanner.startTrainingScan(numberOfScanningKits, WifiScanner.TYPE_TRAINING);
    }
    public void postLocationPointInfoToServer(int x, int y, String roomName, int floorId){
        LocationPointInfo locationPointInfo = new LocationPointInfo(x,y,roomName, floorId);
        postFromTrainingWithCoordinates(locationPointInfo);
    }

    public void processCompleteKitsOfScanResults(CompleteKitsContainer completeKitsContainer){

        if (completeKitsContainer.getRequestSourceType()!=WifiScanner.TYPE_TRAINING) return;

        int numberOfCurrentSuccessfulKits=0;
        for (List<ScanResult> oneScanResults: completeKitsContainer.getCompleteKits()) {
            List<AccessPoint> accessPoints = new ArrayList<>();
            numberOfCurrentSuccessfulKits++;
            for (ScanResult scanResult : oneScanResults) {
                accessPoints.add(new AccessPoint(scanResult.BSSID, scanResult.level));
            }
            // создаём запрос на добавление информации набора на экран пользователя
            requestToAddAPs.setValue(convertKitToString(accessPoints, numberOfCurrentSuccessfulKits));
            if (calibrationLocationPoint==null) return;
            calibrationLocationPoint.addOneCalibrationSet(accessPoints);
        }

        chosePostCalibrationLPToServer();
    }
    private String convertKitToString(List<AccessPoint> accessPoints, int number){
        String result="Набор №"+number+"\n";
        for (AccessPoint accessPoint: accessPoints){
            result+=accessPoint.toString()+"\n";
        }
        return result;
    }

    private void chosePostCalibrationLPToServer(){
        switch (scanningMode){
            case MODE_TRAINING_APS:
                postFromTrainingWithAPs();
                break;
            case MODE_DEFINITION:
                postFromDefinitionLocation();
                break;
        }
    }

    private void postFromTrainingWithAPs(){
        toastContent.setValue("Обучение точкам доступа началось");
        retrofit.postCalibrationLPWithAPs(calibrationLocationPoint).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "GOOD_TRAINING_APs_ROOM",
                        String.format("Server response=%s", response.body()));
                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    return;
                }
                serverResponse.setValue(response.body().getResponse());
            }
            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
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
    private void postFromDefinitionLocation(){
        toastContent.setValue("Определение комнаты началось");
        Log.i("infoAboutCalibrationLP", calibrationLocationPoint.toString());
        retrofit.defineLocation(calibrationLocationPoint).enqueue(new Callback<DefinedLocationPoint>() {
            @Override
            public void onResponse(Call<DefinedLocationPoint> call, Response<DefinedLocationPoint> response) {
                Log.println(Log.INFO, "GOOD_DEFINITION_ROOM",
                        String.format("Server response=%s", response.body()));
                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    return;
                }
                serverResponse.setValue(response.body().toString());
            }
            @Override
            public void onFailure(Call<DefinedLocationPoint> call, Throwable t) {
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
}
