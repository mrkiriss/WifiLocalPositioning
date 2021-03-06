package com.mrkiriss.wifilocalpositioning.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mrkiriss.wifilocalpositioning.data.models.server.StringResponse;
import com.mrkiriss.wifilocalpositioning.data.models.settings.AbilitiesScanningData;
import com.mrkiriss.wifilocalpositioning.data.sources.db.AbilitiesDao;
import com.mrkiriss.wifilocalpositioning.data.sources.remote.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.utils.livedata.Event;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ScanningAbilitiesManager {

    private final AbilitiesDao abilitiesDao;
    private final LocationDataApi instructionApi;

    private boolean needShowInformationAboutAbilities = true;

    public MutableLiveData<Event<String>> getRequestToOpenInstructionObYouTube() {
        return requestToOpenInstructionObYouTube;
    }

    private final MutableLiveData<Event<String>> requestToOpenInstructionObYouTube;

    @Inject
    public ScanningAbilitiesManager(AbilitiesDao abilitiesDao, LocationDataApi instructionApi){
        this.abilitiesDao=abilitiesDao;
        this.instructionApi=instructionApi;

        requestToOpenInstructionObYouTube=new MutableLiveData<>();

        requestToUpdateNeedToShowDialogFromBD();
    }

    public void requestToUpdateNeedToShowDialogFromBD(){
        Runnable task = ()->{
            AbilitiesScanningData dataFromBD = abilitiesDao.findFirst(0L);
            if (dataFromBD!=null){
                needShowInformationAboutAbilities=dataFromBD.isNeedShowInformationAboutAbilities();
            }
            Log.i("scanningAbilities", "got result from db: isNeedShowInformationAboutAbilities = " + needShowInformationAboutAbilities);
        };
        Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join(500);
        } catch (InterruptedException e) {
            Log.i("scanningAbilities", "join timeout");
            e.printStackTrace();
        }
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
        Log.i("scanningAbilities", "start show dialog about scanning abilities ");

        // ???????????? 4 ???? 2 ????????????, ?????? ?????????????????????? ????????????????
        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.P){
            showNotificationOfPermanentRestrictions(context);
        }else if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P){ // ???????????? 4 ???? 2 ????????????, ???????????????? ?????????? ?????????? ???????????????????????? ????????????????
            showNotificationOfNotPermanentRestrictions(context);
        }
    }

    // ANDROID v9
    private void showNotificationOfPermanentRestrictions(Context context){
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
        dialog.setCancelable(false);
        dialog.setTitle("?????????????????????? ????????????????????????");

        CheckBox check = new CheckBox(dialog.getContext());
        check.setText("  ???????????? ???? ????????????????????");
        check.setElegantTextHeight(true);
        dialog.setView(check);

        String androidV9MessageContent = "???? ???????????????????? ???????????????????? ???? Android ???????????? 9.\n" +
                "?? ?????????? ?? ?????????????????????????? ???????????? ???????????????????? ?????????? ?????????????????????? ???????????? 4 wi-fi ???????????????????????? ?? 2 ????????????.\n" +
                "????????????????????, ???????????? ?????? ??????????, ?????????????? ?????????????????? ????????????????????????\n\n\n" +
                "???????????????? ?????????????????? ???? ????????????????????.\n\n";
        dialog.setMessage(androidV9MessageContent);
        dialog.setNeutralButton("????????????????????", (dialog12, which) -> {
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
        dialog.setTitle("?????????????????????? ????????????????????????");

        CheckBox check = new CheckBox(dialog.getContext());
        check.setText("  ???????????? ???? ????????????????????");
        check.setElegantTextHeight(true);
        dialog.setView(check);

        String androidV10HMessageContent = "???? ???????????????????? ???????????????????? ???? Android ???????????? 10 ?????? ????????.\n" +
                "?? ?????????? ?? ?????????????????????????? ???????????? ???????????????????? ?????????? ?????????????????????? ???????????? 4 wi-fi ???????????????????????? ?? 2 ????????????.\n\n" +
                "?????? ???????????????????? ?????????????????????? ?????????????????? ?? \"?????????? ????????????????????????\" ?? ???????????????????? ???????????????????? " +
                "?? ?????????????????? \"WI-FI scan throttling\" \n\n" +
                "?????????????????? ???????????????????? ???? ???????????? ?????????????????????? ?? ?????????? ???? ???????????? ????????\n\n\n" +
                "???????????????? ?????????????????? ???? ????????????????????.\n\n";
        dialog.setMessage(androidV10HMessageContent);
        dialog.setNeutralButton("????????????????????", (dialog12, which) -> {
            if (check.isChecked()){
                updateDataAboutNeedToShowDialog();
            }
        });
        dialog.setPositiveButton("?????????????????????? ????????????????????", (dialog1, which) -> startInstructionURL(context));

        dialog.show();
    }

    private void startInstructionURL(Context context){
        instructionApi.getInstructionURL().enqueue(new Callback<StringResponse>() {
            @Override
            public void onResponse(@NonNull Call<StringResponse> call, @NonNull Response<StringResponse> response) {
                if (response.body()==null || response.body().getResponse().isEmpty()){
                    Toast.makeText(context, "????????????????, ???????? ?????????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                }
                requestToOpenInstructionObYouTube.setValue(new Event<>(response.body().getResponse()));
            }

            @Override
            public void onFailure(@NonNull Call<StringResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "????????????????, ???????? ?????????? ???????????? ??????", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
