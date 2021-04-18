package com.mrkiriss.wifilocalpositioning.managers;

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

import java.util.List;

import lombok.Data;

@Data
public class WifiScanner {

    private WifiManager wifiManager;
    private Context context;

    private BroadcastReceiver scanResultBR;
    private WifiManager.ScanResultsCallback scanResultsCallback;

    private long scanDelay=4000;
    private int numberOfScanning;
    private boolean isInfinityScanning=false;
    final Handler handler;
    private boolean scanStarted;

    private final MutableLiveData<List<ScanResult>> scanResults;

    public WifiScanner(Context context){
        this.context=context;
        this.wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        this.scanResults=new MutableLiveData<>();
        this.handler = new Handler(Looper.getMainLooper());
        this.scanStarted=false;

        registerListeners();
    }

    public void startScanningWithParameters(int numberOfScanning){
        if (this.numberOfScanning>0 || scanStarted) return;

        this.numberOfScanning=numberOfScanning;
        startScanningWithDelay();
    }
    public void startInfinityScanning(){
        isInfinityScanning=true;
        startScanningWithDelay();
    }
    private void startScanningWithDelay(){
        Runnable task = () -> {

            if (numberOfScanning>0){
                numberOfScanning--;
            }else if (!isInfinityScanning){
                return;
            }

            Log.println(Log.INFO, "START_ONE_SCANNING",
                    String.format("Parameters: remaining number of scans=%s", numberOfScanning));
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
                    scanResults.setValue(wifiManager.getScanResults());
                    scanStarted=false;
                    startScanningWithDelay();
                }
            };
        }
        context.registerReceiver(scanResultBR, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
    private void unregisterScanReceiver(){
        context.unregisterReceiver(scanResultBR);
    }

    private void registerScanCallback(){
        if (scanResultsCallback==null){
            scanResultsCallback=new WifiManager.ScanResultsCallback() {
                @Override
                public void onScanResultsAvailable() {
                    scanResults.postValue(wifiManager.getScanResults());
                    unregisterScanCallback();
                }
            };
        }
        wifiManager.registerScanResultsCallback(Runnable::run, scanResultsCallback );
    };
    public void unregisterScanCallback(){
        wifiManager.unregisterScanResultsCallback(scanResultsCallback);
    }
}
