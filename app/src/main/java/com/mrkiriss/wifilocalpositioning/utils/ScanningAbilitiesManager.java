package com.mrkiriss.wifilocalpositioning.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.style.BackgroundColorSpan;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.settings.AbilitiesScanningData;
import com.mrkiriss.wifilocalpositioning.data.sources.db.AbilitiesDao;

public class ScanningAbilitiesManager {

    private final AbilitiesDao abilitiesDao;

    private boolean needShowInformationAboutAbilities=true;


    private final String androidV9MessageContent = "На устройстве обнаружена ОС Android версии 9.\n" +
            "В связи с ограничениями версии приложение может запрашивать только 4 wi-fi сканирования в 2 минуты.\n" +
            "Пожалуйста, имейте это ввиду, задавая настройки сканирования\n\n\n" +
            "Приносим извенения за неудобства";

    private final String androidV10HMessageContent = "На устройстве обнаружена ОС Android версии 10 или выше.\n" +
            "В связи с ограничениями версии приложение может запрашивать только 4 wi-fi сканирования в 2 минуты.\n\n" +
            "Для отключения ограничений перейдите в \"Режим разработчика\" в настройках устройства " +
            "и снимите галочку с \"WI-FI scan throttling\" \n\n" +
            "Подробную инструкцию вы можете просмотреть в видео по ссылке ниже\n\n\n"+
            "Приносим извенения за неудобства";

    public ScanningAbilitiesManager(AbilitiesDao abilitiesDao){
        this.abilitiesDao=abilitiesDao;

        requestToUpdateNeedToShowDialogFromBD();
    }

    public void requestToUpdateNeedToShowDialogFromBD(){
        Runnable task = ()->{
            AbilitiesScanningData dataFromBD = abilitiesDao.findFirst(0L);
            if (dataFromBD!=null){
                needShowInformationAboutAbilities=dataFromBD.isNeedShowInformationAboutAbilities();
            }
        };
        new Thread(task).start();
    }
    private void updateDataAboutNeedToShowDialog(){
        Runnable task = ()->{
            AbilitiesScanningData data = new AbilitiesScanningData();
            data.setNeedShowInformationAboutAbilities(false);
            data.setId(0L);
            abilitiesDao.insert(data);
        };
        new Thread(task).start();
    }

    public void checkAndroidVersionForShowingScanningAbilities(Context context){
        if (!needShowInformationAboutAbilities) return;

        // только 4 на 2 минуты, без возможности изменить
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.P){
            showNotificationOfPermanentRestrictions(context);
        }else if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P){ // только 4 на 2 минуты, изменить через режим разработчика возможно
            showNotificationOfNotPermanentRestrictions(context);
        }
    }

    // ANDROID v9
    private void showNotificationOfPermanentRestrictions(Context context){
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
        dialog.setCancelable(false);
        dialog.setTitle("Ограничения сканирования");

        CheckBox check = new CheckBox(dialog.getContext());
        check.setText("Больше не показывать");
        check.setElegantTextHeight(true);
        dialog.setView(check);

        dialog.setMessage(androidV9MessageContent);
        dialog.setNeutralButton("Продолжить", (dialog12, which) -> {
            if (check.isChecked()){
                updateDataAboutNeedToShowDialog();
            }
        });

        dialog.show();
    }

    // ANDROID v10 or higher
    private void showNotificationOfNotPermanentRestrictions(Context context){
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
        dialog.setCancelable(false);
        dialog.setTitle("Ограничения сканирования");

        CheckBox check = new CheckBox(dialog.getContext());
        check.setText("Больше не показывать");
        check.setElegantTextHeight(true);
        dialog.setView(check);

        dialog.setMessage(androidV10HMessageContent);
        dialog.setNeutralButton("Продолжить", (dialog12, which) -> {
            if (check.isChecked()){
                updateDataAboutNeedToShowDialog();
            }
        });
        dialog.setPositiveButton("Просмотреть инструкцию", (dialog1, which) -> {
        });

        dialog.show();
    }
}
