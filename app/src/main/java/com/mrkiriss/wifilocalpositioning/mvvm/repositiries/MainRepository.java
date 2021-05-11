package com.mrkiriss.wifilocalpositioning.mvvm.repositiries;

import android.content.Context;

import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.utils.ScanningAbilitiesManager;

import javax.inject.Inject;

public class MainRepository {

    private final WifiScanner wifiScanner;
    private final SettingsManager settingsManager;
    private final ScanningAbilitiesManager abilitiesManager;

    public MainRepository(WifiScanner wifiScanner, SettingsManager settingsManager, ScanningAbilitiesManager abilitiesManager){
        this.wifiScanner=wifiScanner;
        this.settingsManager=settingsManager;
        this.abilitiesManager=abilitiesManager;
    }
    // проверка наличия необходимого разрешения для открытия фрагмента
    public boolean isPresentAccessPermission(String type){
        if (type.equals(WifiScanner.TYPE_TRAINING)){
            return settingsManager.getAccessLevel()>0;
        }
        return true;
    }

    // измененение типа запроса источника сканирования
    public void setCurrentTypeOfRequestSource(String type){
        wifiScanner.setCurrentTypeOfRequestSource(type);
    }
    // проверка ограничений сканирования
    public void checkAndroidVersionForShowingScanningAbilities(Context context){
        abilitiesManager.checkAndroidVersionForShowingScanningAbilities(context);
    }
}
