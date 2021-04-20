package com.mrkiriss.wifilocalpositioning.ui.view;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;

import com.google.android.material.navigation.NavigationView;
import com.mrkiriss.wifilocalpositioning.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.Navigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;
    private NavHostFragment navHostFragment;

    private Fragment[] fragments;
    private String[] fragmentTAGS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_training, R.id.nav_definition)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        createFragments();
        setBottomNavigationListener(navigationView);

        onNavigationDrawerItemSelected(0);
        navigationView.setCheckedItem(navigationView.getMenu().getItem(0));
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
        fragments=new Fragment[]{new LocationDetectionFragment(), new TrainingFragment()};
        fragmentTAGS=new String[]{"fDefinition", "fTraining"};
    }
    private int defineFragmentIndex(MenuItem item) {
        switch (item.getItemId()){
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
            item.setChecked(true);
            onNavigationDrawerItemSelected(fragmentIndex);

            drawer.close();

            return true;
        });
    }
    public void onNavigationDrawerItemSelected(int position) {
        Log.i("changeFragments","POS "+position);
        FragmentManager fm = navHostFragment.getChildFragmentManager();

        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (fm.findFragmentByTag(fragmentTAGS[position]) == null) {
            fragmentTransaction.add(R.id.nav_host_fragment, fragments[position], fragmentTAGS[position]);
            Log.i("changeFragments","ADD "+fragments[position].getTag());
        }
        for (int i = 0; i < fragments.length; i++) {
            if (i == position) {
                fragmentTransaction.show(fragments[i]);
                Log.i("changeFragments","SHOW "+fragments[position].getTag());
            } else {
                if (fm.findFragmentByTag(fragmentTAGS[i]) != null) {
                    Log.i("changeFragments","HIDE "+fragments[i].getTag());
                    fragmentTransaction.hide(fragments[i]);
                }
            }
        }
        fragmentTransaction.commit();
        Log.i("changeFragments","commit");
    }
}