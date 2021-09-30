package com.mrkiriss.wifilocalpositioning.view;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.sources.ViewModelFactory;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.viewmodel.MainViewModel;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

    @Inject
    protected ViewModelFactory viewModelFactory;

    private MainViewModel viewModel;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getInstance().getComponentManager().getViewModelSubcomponent().inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        createNavigationView();

        initObservers();

        // проверка разрешений для сканирования
        checkPermissions();
        // проверка ограничений сканирования
        viewModel.checkAndroidVersionForShowingScanningAbilities(this);
    }

    private void createNavigationView(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_definition, R.id.nav_training, R.id.nav_training2, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            hideKeyboard(this);

            // изменяем тип сканирования
            viewModel.setCurrentTypeOfRequestSource(destination.getId());

            // проверяем разрешение доступа к только что открытому фрагменту
            if (!viewModel.isPresentAccessPermission(destination.getId())) navController.navigateUp();

            Log.i("checkNavigation", "destination changed to " + destination.getId() + "definition id = " + R.id.nav_definition);
        });
    }

    private void initObservers(){
        // подписываемсян на запрос открытия видео-инструкции
        viewModel.getRequestToOpenInstructionObYouTube().observe(this, id -> viewModel.watchYoutubeVideo(id));
        // подписываемся на данные Toast
        viewModel.getToastContent().observe(this, this::showToastContent);
        // подписываемся на вызов Intent
        viewModel.getRequestToStartIntent().observe(this, this::startActivity);
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
}