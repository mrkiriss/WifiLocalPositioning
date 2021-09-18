package com.mrkiriss.wifilocalpositioning.data.sources;

import androidx.fragment.app.Fragment;

import com.mrkiriss.wifilocalpositioning.view.LocationDetectionFragment;
import com.mrkiriss.wifilocalpositioning.view.SettingsFragment;
import com.mrkiriss.wifilocalpositioning.view.TrainingMapFragment;
import com.mrkiriss.wifilocalpositioning.view.TrainingScanFragment;

import java.util.List;

public class FragmentsFactory {

    public Fragment[] createFragments() {
        return new Fragment[]{new LocationDetectionFragment(),
                new TrainingScanFragment(),
                new TrainingMapFragment(),
                new SettingsFragment()};
    }

    public String[] createFragmentTags() {
        return new String[]{"Определение", "Тренировка сканированием", "Тренировка картой", "Настройки"};
    }

    public String[] createTypesOfRequestSources() {
        return new String[]{WifiScanner.TYPE_DEFINITION, WifiScanner.TYPE_TRAINING, WifiScanner.TYPE_TRAINING, WifiScanner.TYPE_NO_SCAN};
    }
}
