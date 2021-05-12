package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.MainRepository;

public class MainViewModel extends ViewModel {

    private final MainRepository repository;

    private final LiveData<String> requestToOpenInstructionObYouTube;
    public LiveData<String> getRequestToOpenInstructionObYouTube() {
        return requestToOpenInstructionObYouTube;
    }

    public MainViewModel(MainRepository repository){
        this.repository=repository;

        requestToOpenInstructionObYouTube=repository.getRequestToOpenInstructionObYouTube();
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
