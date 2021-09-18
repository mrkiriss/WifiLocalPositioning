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

import java.util.Objects;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements IUpButtonNavHost {

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

        initObservers();

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

        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(
                R.drawable.ic_menu
        );
    }
    private void createFragments(){
        Fragment startFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (startFragment!=null){
            navHostFragment.getChildFragmentManager().beginTransaction().remove(startFragment).commit();
        }

        fragments = viewModel.createFragments();
        fragmentTAGS = viewModel.createFragmentTags();
        typesOfRequestSources = viewModel.createTypesOfRequestSources();
    }
    private void initObservers(){
        // подписываемсян на запрос открытия видео-инструкции
        viewModel.getRequestToOpenInstructionObYouTube().observe(this, id -> viewModel.watchYoutubeVideo(id));
        // подписываемся на данные Toast
        viewModel.getToastContent().observe(this, this::showToastContent);
        // подписываемся на вызов Intent
        viewModel.getRequestToStartIntent().observe(this, intent -> startActivity(intent));
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
                viewModel.notifyAboutLackOfAccess();
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
        }
        for (int i = 0; i < fragments.length; i++) {
            if (i == position) {
                fragmentTransaction.show(fragments[i]);
                // изменяем тип сканирования
                viewModel.setCurrentTypeOfRequestSource(typesOfRequestSources[i]);
            } else {
                if (fm.findFragmentByTag(fragmentTAGS[i]) != null) {
                    fragmentTransaction.hide(fragments[i]);
                }
            }
        }
        fragmentTransaction.commit(); }

    private void checkPermissions(){
        requestPermissions(new String[] {Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    private void showToastContent(String content){
        Toast.makeText(this, content, Toast.LENGTH_LONG).show();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Методы, отвечающие за функционал по созданию фрагмента с upArrow
    private boolean isUpButton = false;
    @Override
    public void useUpButton() {
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(
                androidx.appcompat.R.drawable.abc_ic_ab_back_material
        );
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        isUpButton = true;
    }
    @Override
    public void useHamburgerButton() {
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(
                R.drawable.ic_menu
        );
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        isUpButton = false;

        hideKeyboard(this);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isUpButton && item.getItemId() == android.R.id.home) {
            onBackPressed();
            useHamburgerButton();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void navigateTo(Fragment current, Fragment target, String fragmentName) {
        navHostFragment.getChildFragmentManager().beginTransaction()
                .hide(current)
                .add(R.id.nav_host_fragment, target, fragmentName)
                .addToBackStack(fragmentName)
                .commit();
    }
    @Override
    public void navigateBack(Fragment current, Fragment target) {
        onBackPressed();
        navHostFragment.getChildFragmentManager().beginTransaction()
                .show(target)
                .commit();
    }
}