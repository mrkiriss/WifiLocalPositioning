package com.mrkiriss.wifilocalpositioning.data.sources;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.utils.ConnectionManager;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class WifiScanner {

    private final Context context;
    private final WifiManager wifiManager;
    private final ConnectionManager connectionManager;
    private final SettingsManager settingsManager;

    private BroadcastReceiver scanResultBR;
    private WifiManager.ScanResultsCallback scanResultsCallback;

    private final Handler handler;
    private boolean scanStarted;
    private long scanDelay;
    private final long minScanDelay=100; // устанавливается после начала сканирований одной пачки

    private CompleteKitsContainer uncompleteKitsContainer;
    private final MutableLiveData<CompleteKitsContainer> completeScanResults;

    private final MutableLiveData<Integer> remainingNumberOfScanning;
    private List<List<ScanResult>> scanResultKits;

    private String currentTypeOfRequestSource;
    public final static String TYPE_TRAINING="training";
    public final static String TYPE_DEFINITION="definition";
    public final static String TYPE_NO_SCAN="no_scan";

    private final MutableLiveData<Boolean> wifiEnabledState; // для отправки состояния включение wifi

    public WifiScanner(Context context, WifiManager wifiManager, ConnectionManager connectionManager, SettingsManager settingsManager){
        this.context=context;
        this.wifiManager=wifiManager;
        this.connectionManager=connectionManager;
        this.settingsManager=settingsManager;

        this.completeScanResults=new MutableLiveData<>();
        this.remainingNumberOfScanning=new MutableLiveData<>(0);
        this.handler = new Handler(Looper.getMainLooper());
        this.scanStarted=false;
        this.wifiEnabledState=new MutableLiveData<>();
    }

    public void setCurrentTypeOfRequestSource(String type){
        this.currentTypeOfRequestSource=type;
        scanStarted=false;
        Log.i("WifiScanner", "Change source type to "+type);
        // оправялем пустой ответ для запуска бесконечного цикла сканирований для карты через запрос от DefinitionRepository
        if (type.equals(TYPE_DEFINITION)){
            CompleteKitsContainer container = new CompleteKitsContainer();
            container.setCompleteKits(new ArrayList<>());
            container.setRequestSourceType(type);
            completeScanResults.setValue(container);
        }
    }

    // проверяет состояние wifi и наличия интернет соединения
    private void checkWifiEnabled(){
        wifiEnabledState.postValue(connectionManager.checkWifiEnabled());
    }

    public void startTrainingScan(int requiredNumberOfScans, String typeOfRequestSource){
        if (!typeOfRequestSource.equals(currentTypeOfRequestSource) || scanStarted) return;
        scanStarted=true;

        Log.i("startScan", "start scanning from training");

        uncompleteKitsContainer =new CompleteKitsContainer();
        uncompleteKitsContainer.setRequestSourceType(typeOfRequestSource);
        uncompleteKitsContainer.setMaxNumberOfScans(requiredNumberOfScans);

        scanDelay=getScanIntervalFromSettings();
        remainingNumberOfScanning.setValue(requiredNumberOfScans);

        // выход, если сканирований не планируется
        if (requiredNumberOfScans==0) return;
        registerListeners();

        Log.i("WifiScanner", "training scan start with settingDelay="+scanDelay+" and requiredNUmberOfScan="+requiredNumberOfScans);

        startScanningWithDelay();

    }
    public void startDefiningScan(String typeOfRequestSource){
        if (!typeOfRequestSource.equals(currentTypeOfRequestSource) || scanStarted) return;
        scanStarted=true;

        Log.i("WifiScanner", "start scanning from definition");

        scanDelay = getScanIntervalFromSettings();
        int numberOfScanning = getNumberOfScanningFromSettings();
        remainingNumberOfScanning.setValue(numberOfScanning);

        // выход, если сканирований не планируется
        if (numberOfScanning==0) return;
        registerListeners();

        uncompleteKitsContainer =new CompleteKitsContainer();
        uncompleteKitsContainer.setRequestSourceType(typeOfRequestSource);
        uncompleteKitsContainer.setMaxNumberOfScans(numberOfScanning);


        Log.i("WifiScanner", "definition scan start with settingDelay="+scanDelay+" and requiredNUmberOfScan="+numberOfScanning);

        startScanningWithDelay();

    }
    private long getScanIntervalFromSettings(){
        return settingsManager.getScanInterval()*1000;
    }
    private int getNumberOfScanningFromSettings(){
        return settingsManager.getNumberOfScanning();
    }

    private void startScanningWithDelay(){
        checkWifiEnabled();
        Runnable task = () -> {
            if (remainingNumberOfScanning.getValue()>0){ // если данные в remainingNumberOfScanning успели дойти
                Log.i( "WifiScanner", "successful continue, remainingNumberOfScanning "+ remainingNumberOfScanning.getValue());
                remainingNumberOfScanning.postValue(remainingNumberOfScanning.getValue()-1);
            }else{ // комплект собран, отправка на обработку и далее на сервер

                Log.i( "WifiScanner", "unsuccessful continue (scanStarted="+scanStarted +"), start creating datablock, remainingNumberOfScanning  "+remainingNumberOfScanning.getValue());


                CompleteKitsContainer container = uncompleteKitsContainer;
                if (container.getRequestSourceType()==null || !container.getRequestSourceType().equals(currentTypeOfRequestSource) || container.getCompleteKits().size()==0) {
                    return;
                }

                // post data about all scans in kits
                completeScanResults.postValue(container);
                Log.i( "WifiScanner", "completeScanResults posted with data= "+container);

                // недопускаем лишних сканирований
                unregisterListeners();
                scanStarted=false;

                return;
            }

            Log.println(Log.INFO, "WifiScanner",
                    String.format("START_ONE_SCANNING _Parameters: remaining number of scans=%s", remainingNumberOfScanning.getValue()));
            requestScanResults();
        };
        handler.postDelayed(task, scanDelay);
        // после первого запуска обнуляем задержку, чтобы пачка прошла разом
        if (scanDelay!=50) scanDelay=minScanDelay;
    }

    private void requestScanResults(){
        //scanStarted=true;
        wifiManager.startScan();
    }
    private void registerListeners(){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
            registerScanReceiver();
        }else {
            registerScanCallback();
        }
    }
    private void unregisterListeners(){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
            unregisterScanReceiver();
        }else {
            unregisterScanCallback();
        }
    }

    private void registerScanReceiver(){
        if (scanResultBR==null){
            scanResultBR = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i("WifiScanner",
                            "SCAN_RESULTS_RECEIVED__successful");
                    //scanStarted=false;

                    // прекращение сканирований
                    if (currentTypeOfRequestSource.equals(TYPE_NO_SCAN) || uncompleteKitsContainer.getCompleteKits()==null) return;

                    uncompleteKitsContainer.addKitOfScan(wifiManager.getScanResults());
                    startScanningWithDelay();
                }
            };
        }
        context.registerReceiver(scanResultBR, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
    private void unregisterScanReceiver(){
        try {
            context.unregisterReceiver(scanResultBR);
        }catch (IllegalArgumentException e){
            Log.e("WifiScanner",e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void registerScanCallback(){
        if (scanResultsCallback==null){
            scanResultsCallback=new WifiManager.ScanResultsCallback() {
                @Override
                public void onScanResultsAvailable() {
                    Log.println(Log.INFO, "WifiScanner",
                            "SCAN_RESULTS_CALLBACK__successful");
                    //scanStarted=false;

                    // прекращение сканирований
                    if (currentTypeOfRequestSource.equals(TYPE_NO_SCAN) || uncompleteKitsContainer.getCompleteKits()==null) return;

                    uncompleteKitsContainer.addKitOfScan(wifiManager.getScanResults());
                    startScanningWithDelay();
                }
            };
        }
        wifiManager.registerScanResultsCallback(Runnable::run, scanResultsCallback );
    };
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void unregisterScanCallback(){
        try {
            wifiManager.unregisterScanResultsCallback(scanResultsCallback);
        }catch (IllegalArgumentException e){
            Log.e("WifiScanner",e.getMessage());
        }
    }
}
