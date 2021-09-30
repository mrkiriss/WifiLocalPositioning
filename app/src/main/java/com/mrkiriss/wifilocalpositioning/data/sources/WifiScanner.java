package com.mrkiriss.wifilocalpositioning.data.sources;

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

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.utils.LiveData.Event;
import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.utils.ConnectionManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Data;

@Data
@Singleton
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
    private final MutableLiveData<Event<CompleteKitsContainer>> completeScanResults;

    private final MutableLiveData<Integer> remainingNumberOfScanning;
    private List<List<ScanResult>> scanResultKits;

    private TypeOfScanning currentTypeOfRequestSource;
    public enum TypeOfScanning {
        DEFINITION, TRAINING, NO_SCAN;

        private static final int[] fragmentsOfDefinition = {R.id.nav_definition};
        private static final int[] fragmentsOfTraining = {R.id.nav_training, R.id.nav_training2};
        private static final int[] fragmentsOfNoScan = {R.id.nav_search, R.id.nav_settings};

        public static TypeOfScanning defineTypeOfLocationByID(int id) {
            if (contains(fragmentsOfDefinition, id)){
                return DEFINITION;
            } else if (contains(fragmentsOfTraining, id)){
                return TRAINING;
            } else {
                return NO_SCAN;
            }
        }
        private static boolean contains(int[] arr, int id) {
            for (int i : arr) if (i == id) return true;
            return false;
        }
    }

    private final MutableLiveData<Event<Boolean>> wifiEnabledState; // для отправки состояния включение wifi

    @Inject
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

        Log.i("checkDI", "created WifiScanner");
    }

    public void setCurrentTypeOfRequestSource(int newLocationId){
        this.currentTypeOfRequestSource = TypeOfScanning.defineTypeOfLocationByID(newLocationId);

        scanStarted = false;
        Log.i("WifiScanner", "Change source type to " + currentTypeOfRequestSource);
        // оправялем пустой ответ для запуска бесконечного цикла сканирований для карты через запрос от DefinitionRepository
        if (currentTypeOfRequestSource == TypeOfScanning.DEFINITION){
            CompleteKitsContainer container = new CompleteKitsContainer();
            container.setCompleteKits(new ArrayList<>());
            container.setRequestSourceType(currentTypeOfRequestSource);
            completeScanResults.setValue(new Event<>(container));
        }
    }

    // проверяет состояние wifi и наличия интернет соединения
    private void checkWifiEnabled(){
        wifiEnabledState.postValue(new Event<>(connectionManager.checkWifiEnabled()));
    }

    public void startTrainingScan(int requiredNumberOfScans, TypeOfScanning typeOfRequestSource){
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
    public void startDefiningScan(TypeOfScanning typeOfRequestSource){
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
        return settingsManager.getScanInterval()* 1000L;
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
                completeScanResults.postValue(new Event<>(container));
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
                    if (currentTypeOfRequestSource == TypeOfScanning.NO_SCAN || uncompleteKitsContainer.getCompleteKits()==null) return;

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
                    if (currentTypeOfRequestSource == TypeOfScanning.NO_SCAN || uncompleteKitsContainer.getCompleteKits()==null) return;

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
