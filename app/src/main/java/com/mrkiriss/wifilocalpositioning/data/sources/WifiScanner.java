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

import com.mrkiriss.wifilocalpositioning.data.entity.Settings;
import com.mrkiriss.wifilocalpositioning.data.models.server.AccessPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.sources.db.AppDatabase;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingDao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class WifiScanner {

    private Context context;
    private WifiManager wifiManager;
    private SettingDao settingDao;

    private BroadcastReceiver scanResultBR;
    private WifiManager.ScanResultsCallback scanResultsCallback;

    private final Handler handler;
    private boolean scanStarted;
    private long scanDelay;

    private final MutableLiveData<List<List<ScanResult>>> completeScanResults;
    private final MutableLiveData<Integer> remainingNumberOfScanning;
    private List<List<ScanResult>> scanResultKits;

    public WifiScanner(Context context, WifiManager wifiManager, AppDatabase db){
        this.context=context;
        this.wifiManager=wifiManager;
        this.settingDao=db.settingDao();

        this.completeScanResults=new MutableLiveData<>();
        this.remainingNumberOfScanning=new MutableLiveData<>(0);
        this.handler = new Handler(Looper.getMainLooper());
        this.scanStarted=false;

        registerListeners();
    }

    public boolean startTrainingScan(int requiredNumberOfScans){
        scanResultKits=new LinkedList<>();
        scanDelay=1000;
        remainingNumberOfScanning.setValue(requiredNumberOfScans);

        startScanningWithDelay();

        return true;
    }
    public boolean startDefiningScan(){
        scanResultKits=new LinkedList<>();

        Runnable task = () -> {
            Settings currentSettings = settingDao.findById(0L);
            scanDelay = currentSettings.getScanInterval();
            remainingNumberOfScanning.postValue(currentSettings.getNumberOfScans());

            startScanningWithDelay();
        };
        new Thread(task).start();

        return true;
    }

    private void startScanningWithDelay(){
        if (remainingNumberOfScanning.getValue()>0 && !scanStarted){
            remainingNumberOfScanning.postValue(remainingNumberOfScanning.getValue()-1);
        }else{
            completeScanResults.postValue(scanResultKits);
            return;
        }
        Runnable task = () -> {

            Log.println(Log.INFO, "START_ONE_SCANNING",
                    String.format("Parameters: remaining number of scans=%s", remainingNumberOfScanning));
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
                    Log.println(Log.INFO, "SCAN_RESULTS_RECEIVED",
                            "successful");
                    scanStarted=false;
                    scanResultKits.add(wifiManager.getScanResults());
                    startScanningWithDelay();
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
                    Log.println(Log.INFO, "SCAN_RESULTS_CALLBACK",
                            "successful");
                    scanStarted=false;
                    scanResultKits.add(wifiManager.getScanResults());
                    startScanningWithDelay();
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
