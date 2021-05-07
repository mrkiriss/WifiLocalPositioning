package com.mrkiriss.wifilocalpositioning.data.sources;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.sources.db.AppDatabase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class WifiScanner {

    private final Context context;
    private final WifiManager wifiManager;
    private final SharedPreferences sharedPreferences;
    private final SettingsManager settingsManager;

    private BroadcastReceiver scanResultBR;
    private WifiManager.ScanResultsCallback scanResultsCallback;

    private final Handler handler;
    private boolean scanStarted;
    private long scanDelay;

    private CompleteKitsContainer uncompleteKitsContainer;
    private final MutableLiveData<CompleteKitsContainer> completeScanResults;

    private final MutableLiveData<Integer> remainingNumberOfScanning;
    private List<List<ScanResult>> scanResultKits;

    private String currentTypeOfRequestSource;
    public final static String TYPE_TRAINING="training";
    public final static String TYPE_DEFINITION="definition";
    public final static String TYPE_NO_SCAN="no_scan";


    public WifiScanner(Context context, WifiManager wifiManager, AppDatabase db, SettingsManager settingsManager){
        this.context=context;
        this.wifiManager=wifiManager;
        this.sharedPreferences=context.getSharedPreferences("com.mrkiriss.wifilocalpositioning_preferences", Context.MODE_PRIVATE);
        this.settingsManager=settingsManager;

        this.completeScanResults=new MutableLiveData<>();
        this.remainingNumberOfScanning=new MutableLiveData<>(0);
        this.handler = new Handler(Looper.getMainLooper());
        this.scanStarted=false;

        registerListeners();
    }

    public void setCurrentTypeOfRequestSource(String type){
        this.currentTypeOfRequestSource=type;
        Log.i("WifiScanner", "Change source type to "+type);
        // оправялем пустой ответ для запуска бесконечного цикла сканирований для карты через запрос от DefinitionRepository
        if (type.equals(TYPE_DEFINITION)){
            CompleteKitsContainer container = new CompleteKitsContainer();
            container.setCompleteKits(new ArrayList<>());
            container.setRequestSourceType(type);
            completeScanResults.setValue(container);
        }
    }

    public boolean startTrainingScan(int requiredNumberOfScans, String typeOfRequestSource){
        if (!typeOfRequestSource.equals(currentTypeOfRequestSource)) return false;

        Log.i("startScan", "start scanning from training");

        uncompleteKitsContainer =new CompleteKitsContainer();
        uncompleteKitsContainer.setRequestSourceType(typeOfRequestSource);

        scanResultKits=new LinkedList<>();
        scanDelay=getScanIntervalFromSettings();
        remainingNumberOfScanning.setValue(requiredNumberOfScans);

        Log.i("WifiScanner", "training scan start with settingDelay="+scanDelay+" and requiredNUmberOfScan="+requiredNumberOfScans);

        startScanningWithDelay(-1);

        return true;
    }
    public boolean startDefiningScan(String typeOfRequestSource){
        if (!typeOfRequestSource.equals(currentTypeOfRequestSource)) return false;

        Log.i("WifiScanner", "start scanning from definition");

        uncompleteKitsContainer =new CompleteKitsContainer();
        uncompleteKitsContainer.setRequestSourceType(typeOfRequestSource);

        scanResultKits=new LinkedList<>();

        Runnable task = () -> {
            scanDelay = getScanIntervalFromSettings();
            int numberOfScanning = getNumberOfScanningFromSettings();
            remainingNumberOfScanning.postValue(numberOfScanning);

            Log.i("WifiScanner", "training scan start with settingDelay="+scanDelay+" and requiredNUmberOfScan="+numberOfScanning);


            startScanningWithDelay(numberOfScanning);
        };
        new Thread(task).start();

        return true;
    }
    private long getScanIntervalFromSettings(){
        return settingsManager.getScanInterval();
    }
    private int getNumberOfScanningFromSettings(){
        return settingsManager.getDefaultNumberOfScanning();
    }


    private void startScanningWithDelay(int remainingNumberOfScanningLocale){
        if (remainingNumberOfScanning.getValue()>0 && !scanStarted){
            Log.i( "WifiScanner", "successful continue, remainingNumberOfScanning "+ remainingNumberOfScanning.getValue());
            remainingNumberOfScanning.postValue(remainingNumberOfScanning.getValue()-1);
        }else if(remainingNumberOfScanningLocale>0 && !scanStarted){
            Log.i( "WifiScanner", "successful continue locale, remainingNumberOfScanning "+ remainingNumberOfScanning.getValue());
            remainingNumberOfScanning.postValue(remainingNumberOfScanningLocale-1);
        }else{ // комплект собран, отправка на обработку и далее на сервер
            Log.i( "WifiScanner", "unsuccessful continue, remainingNumberOfScanning  "+remainingNumberOfScanning.getValue());

            CompleteKitsContainer container = uncompleteKitsContainer;
            container.setCompleteKits(scanResultKits);
            if (container.getRequestSourceType()==null || !container.getRequestSourceType().equals(currentTypeOfRequestSource)) {
                return;
            }

            completeScanResults.postValue(container);
            return;
        }
        Runnable task = () -> {

            Log.println(Log.INFO, "WifiScanner",
                    String.format("START_ONE_SCANNING _Parameters: remaining number of scans=%s", remainingNumberOfScanning.getValue()));
            scanStarted=true;
            requestScanResults();
        };
        handler.postDelayed(task, scanDelay);
    }

    private void requestScanResults(){
        scanStarted=true;
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
            wifiManager.startScan();
        }else {
        }
    }
    private void registerListeners(){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
            registerScanReceiver();
        }else {
            registerScanCallback();
        }
    }

    private void registerScanReceiver(){
        if (scanResultBR==null){
            scanResultBR = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i("WifiScanner",
                            "SCAN_RESULTS_RECEIVED__successful");
                    scanStarted=false;

                    // прекращение сканирований
                    if (currentTypeOfRequestSource.equals(TYPE_NO_SCAN) || scanResultKits==null) return;

                    scanResultKits.add(wifiManager.getScanResults());
                    startScanningWithDelay(-1);
                }
            };
        }
        context.registerReceiver(scanResultBR, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
    private void unregisterScanReceiver(){
        context.unregisterReceiver(scanResultBR);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void registerScanCallback(){
        if (scanResultsCallback==null){
            scanResultsCallback=new WifiManager.ScanResultsCallback() {
                @Override
                public void onScanResultsAvailable() {
                    Log.println(Log.INFO, "WifiScanner",
                            "SCAN_RESULTS_CALLBACK__successful");
                    scanStarted=false;
                    scanResultKits.add(wifiManager.getScanResults());
                    startScanningWithDelay(-1);
                }
            };
        }
        wifiManager.registerScanResultsCallback(Runnable::run, scanResultsCallback );
    };
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void unregisterScanCallback(){
        wifiManager.unregisterScanResultsCallback(scanResultsCallback);
    }
}
