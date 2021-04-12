package com.mrkiriss.wifilocalpositioning.repositiries;

import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.managers.WifiScanner;
import com.mrkiriss.wifilocalpositioning.models.AccessPoint;
import com.mrkiriss.wifilocalpositioning.models.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.models.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.models.StringResponse;
import com.mrkiriss.wifilocalpositioning.network.IMWifiServerApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@Data
public class TrainingRepository {

    private IMWifiServerApi retrofit;
    private WifiScanner wifiScanner;

    private CalibrationLocationPoint calibrationLocationPoint;
    private int scanningMode;
    public static final int MODE_TRAINING=1;
    public static final int MODE_DEFINITION=2;

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

        wifiScanner.startScanningWithParameters(numberOfScanningKits);
    }
    public void runCleaningServer(){
        retrofit.cleanServer().enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "CLEANING_SUCCESSFUL",
                        String.format("Parameters: server response=%s", response.body()));
            }

            @Override
            public void onFailure(Call<StringResponse> call, Throwable t) {
                Log.println(Log.INFO, "CLEANING_FAILED",
                        String.format("Parameters: server response=%s", t.getMessage()));
            }
        });
    }

    public void startProcessingScanResultKit(List<ScanResult> scanResults, int numberOfCurrentSuccessfulKits){

        List<AccessPoint> accessPoints = new ArrayList<>();
        for (ScanResult scanResult : scanResults){
            accessPoints.add(new AccessPoint(scanResult.BSSID, scanResult.level));
        }

        Log.println(Log.INFO, "START_PROC_SCAN_RESULT",
                String.format("Parameters: numberOfCurrentSuccessfulKits=%s, value=%s", numberOfCurrentSuccessfulKits,
                        convertKitToString(accessPoints, numberOfCurrentSuccessfulKits)));

        // занесли набор в список наборов у калибровочной точки
        calibrationLocationPoint.addCalibrationSet(accessPoints);
        // создаём запрос на добавление информации набора на экран пользователя
        requestToAddAPs.setValue(convertKitToString(accessPoints, numberOfCurrentSuccessfulKits));

        // проверяем законченность блока сканирования
        if (wifiScanner.getNumberOfScanning()==0){
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
        switch (scanningMode){
            case MODE_TRAINING:
                postFromTraining();
                break;
            case MODE_DEFINITION:
                postFromDefinition();
                break;
        }
    }
    private void postFromTraining(){
        toastContent.setValue("Обучение началось");
        retrofit.postLocationPointForCalibrationTraining(calibrationLocationPoint).enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(Call<StringResponse> call, Response<StringResponse> response) {
                Log.println(Log.INFO, "GOOD_RESPONSE_SERVER",
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
    private void postFromDefinition(){
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
}
