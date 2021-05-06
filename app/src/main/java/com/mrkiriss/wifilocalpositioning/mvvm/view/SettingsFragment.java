package com.mrkiriss.wifilocalpositioning.mvvm.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.preference.DefaultSettings;
import com.mrkiriss.wifilocalpositioning.data.models.preference.Settings;
import com.mrkiriss.wifilocalpositioning.data.sources.db.SettingsDao;

import javax.inject.Inject;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Inject
    SettingsDao settingsDao;
    private PreferenceManager preferenceManager;
    private SharedPreferences sharedPreferences;

    private final String defaultScanInterval= DefaultSettings.defaultScanInterval; // in seconds
    private final String defaultVarious = DefaultSettings.defaultVariousValue;

    private final String[] keys = DefaultSettings.keys;

    private EditTextPreference scanIntervalEditText;
    private ListPreference variousNumbersList;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.root_preferences);

        // find views
        scanIntervalEditText=findPreference(keys[0]);
        variousNumbersList = findPreference(keys[1]);

        // create preference objects
        preferenceManager=getPreferenceManager();
        sharedPreferences=preferenceManager.getSharedPreferences();

        // register preference change listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this::processEditPreferences);

        // set summaries
        scanIntervalEditText.setSummary(preferenceManager.getSharedPreferences().getString(keys[0], defaultScanInterval));
        variousNumbersList.setSummary(preferenceManager.getSharedPreferences().getString(keys[1], defaultVarious));

        // set selected values
        if (scanIntervalEditText.getText()==null || scanIntervalEditText.getText().isEmpty()) scanIntervalEditText.setText(defaultScanInterval);
        if (variousNumbersList.getValue()==null || variousNumbersList.getValue().isEmpty()) variousNumbersList.setValue(defaultVarious);
    }

    private void processEditPreferences(SharedPreferences sharedPreferences, String key){
        switch (key){
            case "scanInterval":
                processScanIntervalKey(sharedPreferences, key);
                break;
            case "variousOfNumberScans":
                processVariousOfNumbersScans(sharedPreferences, key);
                break;
        }


    }
    private void processScanIntervalKey(SharedPreferences sharedPreferences, String key){
        try {
            String input = sharedPreferences.getString(key, defaultScanInterval);
            if (!input.matches("\\d+")){
                throw new Exception();
            }
            scanIntervalEditText.setSummary(String.valueOf(input));
            Log.i("SettingsFragments", "new value for scanInterval="+input);
        }catch (Exception e){
            scanIntervalEditText.setText(scanIntervalEditText.getSummary().toString());
            showToastContent("Неккоректные данные");
            e.printStackTrace();
        }
    }
    private void processVariousOfNumbersScans(SharedPreferences sharedPreferences, String key){
        try {
            String input =  sharedPreferences.getString(key, defaultVarious);
            if (input.isEmpty()) throw new Exception();
            variousNumbersList.setSummary(sharedPreferences.getString(key, defaultVarious));
            Log.i("SettingsFragments", "new value for variousOfNumberScans="+input);
        }catch (Exception e){
            sharedPreferences.edit().putString(key, defaultVarious).apply();
            variousNumbersList.setValue(defaultVarious);
            variousNumbersList.setSummary(defaultVarious);
            showToastContent("Неккоректные данные");
            e.printStackTrace();
        }
    }

    private void showToastContent(String content){
        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
    }
}