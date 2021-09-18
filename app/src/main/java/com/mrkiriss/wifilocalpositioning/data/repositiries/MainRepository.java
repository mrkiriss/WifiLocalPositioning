package com.mrkiriss.wifilocalpositioning.data.repositiries;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.sources.FragmentsFactory;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.utils.ScanningAbilitiesManager;

import java.util.List;

import javax.inject.Inject;

import lombok.Data;

@Data
public class MainRepository {

    private final WifiScanner wifiScanner;
    private final SettingsManager settingsManager;
    private final ScanningAbilitiesManager abilitiesManager;
    private final FragmentsFactory fragmentsFactory;

    private final LiveData<String> requestToOpenInstructionObYouTube;
    private final MutableLiveData<String> toastContent;
    private final MutableLiveData<Intent> requestToStartIntent;


    public MainRepository(WifiScanner wifiScanner, SettingsManager settingsManager,
                          ScanningAbilitiesManager abilitiesManager, FragmentsFactory fragmentsFactory){
        this.wifiScanner=wifiScanner;
        this.settingsManager=settingsManager;
        this.abilitiesManager=abilitiesManager;
        this.fragmentsFactory=fragmentsFactory;

        requestToOpenInstructionObYouTube=abilitiesManager.getRequestToOpenInstructionObYouTube();
        toastContent = new MutableLiveData<>();
        requestToStartIntent = new MutableLiveData<>();
    }

    // создание данных для фрагментов
    public Fragment[] createFragments() {
        return fragmentsFactory.createFragments();
    }
    public String[] createFragmentTags() {
        return fragmentsFactory.createFragmentTags();
    }
    public String[] createTypesOfRequestSources() {
        return fragmentsFactory.createTypesOfRequestSources();
    }

    //запукает инструкции в приложении или в браузере, иначе вывод сообщение об ошибке
    public void watchYoutubeVideo(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            requestToStartIntent.setValue(appIntent);
        } catch (ActivityNotFoundException ex) {
            requestToStartIntent.setValue(webIntent);
        }catch (Exception e){
            toastContent.setValue("Извините, пока здесь ничего нет");
        }
    }

    // уведомляет об отсутсвии доступа к части приложения
    public void notifyAboutLackOfAccess() {
        toastContent.setValue("Доступ запрещён.\n" +
                "За подробной информацией обратитесь к владельцу приложения");
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
