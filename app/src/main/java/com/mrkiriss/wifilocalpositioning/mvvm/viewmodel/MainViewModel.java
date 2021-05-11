package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.MainRepository;

public class MainViewModel extends ViewModel {

    private final MainRepository repository;

    public MainViewModel(MainRepository repository){
        this.repository=repository;
    }

    public boolean isPresentAccessPermission(String type){
        return repository.isPresentAccessPermission(type);
    }
    public void setCurrentTypeOfRequestSource(String type){
        repository.setCurrentTypeOfRequestSource(type);
    }
    public void checkAndroidVersionForShowingScanningAbilities(Context context){
        repository.checkAndroidVersionForShowingScanningAbilities(context);
    }
}
