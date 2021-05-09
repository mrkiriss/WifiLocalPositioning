package com.mrkiriss.wifilocalpositioning.mvvm.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.sources.settings.SettingsManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.di.App;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

    @Inject
    protected WifiScanner wifiScanner;
    @Inject
    protected SettingsManager settingsManager;

    private AppBarConfiguration mAppBarConfiguration;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavController navController;
    private NavHostFragment navHostFragment;

    private Fragment[] fragments;
    private String[] fragmentTAGS;
    private String[] typesOfRequestSources;

    private int currentFragmentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getInstance().getComponentManager().getMainActivitySubcomponent().inject(this);
        checkPermissions();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_training, R.id.nav_definition, R.id.nav_settings, R.id.nav_training2)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        createFragments();
        setBottomNavigationListener(navigationView);

        if (savedInstanceState!=null){
            currentFragmentIndex=savedInstanceState.getInt("currentFragmentIndex");
            Log.i("MainActivityInfo", "currentFragmentIndex from savedInstance = "+currentFragmentIndex);
        }
        changeFragment(currentFragmentIndex);
        navigationView.setCheckedItem(navigationView.getMenu().getItem(currentFragmentIndex));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void createFragments(){

        Fragment startFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (startFragment!=null){
            navHostFragment.getChildFragmentManager().beginTransaction().remove(startFragment).commit();
        }
        fragments=new Fragment[]{new LocationDetectionFragment(), new TrainingScanFragment(), new TrainingMapFragment(), new SettingsFragment()};
        fragmentTAGS=new String[]{"Определение", "Тренировка сканированием", "Тренировка картой", "Настройки"};
        typesOfRequestSources = new String[]{WifiScanner.TYPE_DEFINITION, WifiScanner.TYPE_TRAINING, WifiScanner.TYPE_TRAINING, WifiScanner.TYPE_NO_SCAN};
    }
    private int defineFragmentIndex(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_training_map:
                return 2;
            case R.id.nav_settings:
                return 3;
            case R.id.nav_training:
                return 1;
            case R.id.nav_definition:
                return 0;
        }
        return -1;
    }
    private void setBottomNavigationListener(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(item -> {
            int fragmentIndex = defineFragmentIndex(item);

            // проверка наличия доступа
            if (!isPresentAccessPermission(typesOfRequestSources[fragmentIndex])) {
                notifyAboutLackOfAccess();
                drawer.close();
                return false;
            }

            item.setChecked(true);
            changeFragment(fragmentIndex);
            currentFragmentIndex=fragmentIndex;

            drawer.close();

            return true;
        });
    }
    public void changeFragment(int position) {
        toolbar.setTitle(fragmentTAGS[position]);

        Log.i("MainActivityInfo","POS "+position);
        FragmentManager fm = navHostFragment.getChildFragmentManager();

        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (fm.findFragmentByTag(fragmentTAGS[position]) == null) {
            fragmentTransaction.add(R.id.nav_host_fragment, fragments[position], fragmentTAGS[position]);
            Log.i("MainActivityInfo","ADD "+fragments[position].getTag());
        }
        for (int i = 0; i < fragments.length; i++) {
            if (i == position) {
                fragmentTransaction.show(fragments[i]);
                // изменяем тип сканирования
                wifiScanner.setCurrentTypeOfRequestSource(typesOfRequestSources[i]);

                Log.i("MainActivityInfo","SHOW "+fragments[position].getTag());
            } else {
                if (fm.findFragmentByTag(fragmentTAGS[i]) != null) {
                    Log.i("MainActivityInfo","HIDE "+fragments[i].getTag());
                    fragmentTransaction.hide(fragments[i]);
                }
            }
        }
        fragmentTransaction.commit();
        Log.i("MainActivityInfo","commit");
    }

    private void checkPermissions(){
        requestPermissions(new String[] {Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if (checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.i("Permissions","GRANTED");
        }else{
            Log.i("Permissions","NOT GRANTED");
        }
    }

    private boolean isPresentAccessPermission(String type){
        if (type.equals(WifiScanner.TYPE_TRAINING)){
            return settingsManager.getAccessLevel()>0;
        }
        return true;
    }
    private void notifyAboutLackOfAccess(){
        Toast.makeText(this, "Доступ запрещён.\n" +
                "За подробной информацией обратитесь к владельцу приложения",
                    Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("currentFragmentIndex", currentFragmentIndex);
    }
}