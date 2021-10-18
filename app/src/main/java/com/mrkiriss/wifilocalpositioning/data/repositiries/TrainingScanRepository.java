package com.mrkiriss.wifilocalpositioning.data.repositiries;

import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.server.AccessPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.StringResponse;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.remote.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.utils.livedata.Event;
import com.mrkiriss.wifilocalpositioning.utils.livedata.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
public class TrainingScanRepository {

    private LocationDataApi retrofit;
    private WifiScanner wifiScanner;

    private CalibrationLocationPoint calibrationLocationPoint;

    private final LiveData<Event<CompleteKitsContainer>> completeKitsOfScansResult;
    private MutableLiveData<String> requestToAddAPs;
    private MutableLiveData<String> serverResponse;
    private SingleLiveEvent<String> toastContent;
    private LiveData<Integer> remainingNumberOfScanning;

    @Inject
    public TrainingScanRepository(LocationDataApi retrofit, WifiScanner wifiScanner){
        this.retrofit=retrofit;
        this.wifiScanner=wifiScanner;

        completeKitsOfScansResult=wifiScanner.getCompleteScanResults();
        remainingNumberOfScanning=wifiScanner.getRemainingNumberOfScanning();

        requestToAddAPs=new MutableLiveData<>();
        serverResponse =new MutableLiveData<>();
        toastContent=new SingleLiveEvent<>();
    }

    public void runScanInManager(String _numberOfScanningKits){
        try {
            int numberOfScanningKits = Integer.parseInt(_numberOfScanningKits);

            calibrationLocationPoint = new CalibrationLocationPoint();

            wifiScanner.startTrainingScan(numberOfScanningKits, WifiScanner.TypeOfScanning.TRAINING);
        } catch (NumberFormatException | NullPointerException e) {
            e.printStackTrace();
            toastContent.setValue("Некорректный ввод");
        }
    }

    public void processCompleteKitsOfScanResults(CompleteKitsContainer completeKitsContainer){

        if (completeKitsContainer.getRequestSourceType() != WifiScanner.TypeOfScanning.TRAINING) return;

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

        postFromDefinitionLocation();
    }
    private String convertKitToString(List<AccessPoint> accessPoints, int number){
        String result="Набор №"+number+"\n";
        for (AccessPoint accessPoint: accessPoints){
            result+=accessPoint.toString()+"\n";
        }
        return result;
    }
    private void postFromDefinitionLocation(){
        toastContent.setValue("Определение комнаты началось");
        Log.i("infoAboutCalibrationLP", calibrationLocationPoint.toString());
        retrofit.defineLocation(calibrationLocationPoint).enqueue(new Callback<DefinedLocationPoint>() {
            @Override
            public void onResponse(@NonNull Call<DefinedLocationPoint> call, @NonNull Response<DefinedLocationPoint> response) {
                Log.println(Log.INFO, "GOOD_DEFINITION_ROOM",
                        String.format("Server response=%s", response.body()));
                if (response.body()==null){
                    serverResponse.setValue("Response body is null");
                    return;
                }
                serverResponse.setValue(response.body().toString());
            }
            @Override
            public void onFailure(@NonNull Call<DefinedLocationPoint> call, @NonNull Throwable t) {
                serverResponse.setValue(call.toString()+"\n"+t.getMessage());
                Log.e("SERVER_ERROR", t.getMessage());
            }
        });
    }
}
