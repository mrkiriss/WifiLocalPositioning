package com.mrkiriss.wifilocalpositioning.utils;


import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.CheckBox;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.reflect.Method;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ConnectionManager {
    private long delay_start_time ;

    private final ConnectivityManager connectivityManager;
    private NetworkInfo netInfo;
    private final WifiManager wifiManager;
    private final LocationManager locationManager;

    private boolean showNow;
    private Method mobileDataCheckMethod;

    private boolean wifiEnabledDialogFlag = true;
    private boolean mobileEnabledDialogFlag = true;
    private boolean locationEnabledDialogFlag = true;

    private final String messageContent= "Отсутствует подключение к сети Интернет или доступ к данным о местоположении.\n\n" +
            "Функционал приложения ограничен.\n\n";


    @Inject
    public ConnectionManager(ConnectivityManager connectivityManager, WifiManager wifiManager, LocationManager locationManager){
        this.connectivityManager=connectivityManager;
        this.wifiManager=wifiManager;
        this.locationManager = locationManager;

        // получение метода для проверки мобильного соединения
        try {
            Class cmClass = Class.forName(connectivityManager.getClass().getName());
            mobileDataCheckMethod = cmClass.getDeclaredMethod("getMobileDataEnabled");
            mobileDataCheckMethod.setAccessible(true);
        } catch (Exception e) {
            Log.e("ConnectionManager", e.getMessage());
        }
    }

    public boolean checkWifiEnabled() {
        if (connectivityManager ==null || wifiManager==null) {
            return false;
        }
        netInfo = connectivityManager.getActiveNetworkInfo();

        // проверка включения мобильной передачи данных
        boolean mobileDataEnabled=false;
        if (mobileDataCheckMethod!=null) {
            try {
                mobileDataEnabled= (Boolean) mobileDataCheckMethod.invoke(connectivityManager);
            } catch (Exception e) {
                Log.e("ConnectionManager", e.getMessage());
            }
        }

        // проверка доступа к данным о местоположении
        boolean locationManagerProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // обновление данных для актуализации диалогов
        wifiEnabledDialogFlag = wifiManager.isWifiEnabled();
        mobileEnabledDialogFlag = mobileDataEnabled;
        locationEnabledDialogFlag = locationManagerProviderEnabled;

        // интерент есть и есть одно из видов соединения И доступ к данным о местоположении
        return  (netInfo != null && netInfo.isConnected() && (wifiManager.isWifiEnabled() || mobileDataEnabled) && locationManagerProviderEnabled);
    }
    public void showOfferSetting(Context context) {
        // проверить, показывается ли уже
        if (showNow) return;

        Date date = new Date();
        // проверить время последнего откладывания
        long current_time = date.getTime();
        if (current_time-delay_start_time<=1000*60*10) return;

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
        dialog.setCancelable(false);
        dialog.setTitle("Требуется соединение");
        CheckBox check = new CheckBox(dialog.getContext());
        check.setText("Не показывать следующие 10 минут");
        dialog.setView(check);

        dialog.setMessage(messageContent);
        dialog.setNeutralButton("Продолжить", (dialog1, which) -> {
            showNow=false;
            if (check.isChecked()){
                delay_start_time=date.getTime();
            }
        });
        if (!mobileEnabledDialogFlag && !wifiEnabledDialogFlag)
            dialog.setPositiveButton("Настроки Интернет-соединения", (dialog1, which) -> {
                showNow=false;
                context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            });

        if (!locationEnabledDialogFlag)
            dialog.setNegativeButton("Настройки местоположения", (dialog1, which) -> {
                showNow=false;
                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            });

        showNow=true;
        dialog.show();
    }
}
