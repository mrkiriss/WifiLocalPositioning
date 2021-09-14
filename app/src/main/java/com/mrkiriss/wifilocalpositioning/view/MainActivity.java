package com.mrkiriss.wifilocalpositioning.view;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.search.SearchData;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.search.TypeOfSearchRequester;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.viewmodel.MainViewModel;

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

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements INameChoosingNavHost {

    @Inject
    protected MainViewModel viewModel;

    private NavigationView navigationView;
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

        // подписываемсян на запрос открытия инструкции
        viewModel.getRequestToOpenInstructionObYouTube().observe(this, this::watchYoutubeVideo);

        // проверка разрешений для сканирования
        checkPermissions();
        // проверка ограничений сканирования
        viewModel.checkAndroidVersionForShowingScanningAbilities(this);

        createNavigationView();
        createFragments();

        // установка начального местопоолжения
        changeFragment(currentFragmentIndex);
        navigationView.setCheckedItem(navigationView.getMenu().getItem(currentFragmentIndex));

        // установка режима клавиатуры
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    private void createNavigationView(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_training, R.id.nav_definition, R.id.nav_settings, R.id.nav_training2)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        setBottomNavigationListener(navigationView);
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
            if (!viewModel.isPresentAccessPermission(typesOfRequestSources[fragmentIndex])) {
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
        // change top title
        toolbar.setTitle(fragmentTAGS[position]);
        // hide keyboard
        hideKeyboard(this);

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
                viewModel.setCurrentTypeOfRequestSource(typesOfRequestSources[i]);

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

    private void notifyAboutLackOfAccess(){
        Toast.makeText(this, "Доступ запрещён.\n" +
                "За подробной информацией обратитесь к владельцу приложения",
                    Toast.LENGTH_LONG).show();
    }

    public void watchYoutubeVideo(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }catch (Exception e){
            Toast.makeText(this, "Извините, пока здесь ничего нет", Toast.LENGTH_SHORT).show();
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void navigateToFindFragment(Fragment current, SearchData searchData) {
        getSupportFragmentManager().beginTransaction()
                .hide(current)
                .add(R.id.nav_host_fragment, LocationNameChoosingFragment.newInstance(current, searchData), "LocationFindFragment")
                .addToBackStack("LocationFindFragment")
                .commit();
    }

    @Override
    public void navigateToDefinitionFragment(Fragment current, Fragment target, TypeOfSearchRequester typeOfRequester, MapPoint selectedLocation) {
        getSupportFragmentManager().beginTransaction()
                .remove(current)
                .show(target)
                .commit();
        ((IProcessingSelectedByFindLocation) target).processSelectedByFindLocation(typeOfRequester, selectedLocation);
    }
}