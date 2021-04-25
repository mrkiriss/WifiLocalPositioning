package com.mrkiriss.wifilocalpositioning.mvvm.view.adapters;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.mvvm.view.LocationDetectionFragment;
import com.mrkiriss.wifilocalpositioning.mvvm.view.TrainingMapFragment;
import com.mrkiriss.wifilocalpositioning.mvvm.view.TrainingScanFragment;

import java.util.Objects;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.nav_training, R.string.nav_training};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        final String chosenTitle=Objects.requireNonNull(getPageTitle(position)).toString();
        if (chosenTitle.equals(mContext.getResources().getString(R.string.nav_training))){
            fragment=new TrainingScanFragment();
        }else{
            fragment=new TrainingMapFragment();
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}