package com.mrkiriss.wifilocalpositioning.ui.viewmodel;

import android.net.wifi.ScanResult;

import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.repositiries.LocationDetectionRepository;

import java.util.List;

import javax.inject.Inject;

import lombok.Data;

@Data
public class LocationDetectionViewModel extends ViewModel {

    @Inject
    protected LocationDetectionRepository locationDetectionRepository;

    private final LiveData<List<List<ScanResult>>> completeKitsOfScansResult;
    private LiveData<MapPoint> resultOfDefinition;
    private LiveData<Floor> changeFloor;

    private ObservableInt floorNumber;

    public LocationDetectionViewModel(){

        App.getInstance().getComponentManager().getLocationDetectionSubcomponent().inject(this);

        completeKitsOfScansResult= locationDetectionRepository.getCompleteKitsOfScansResult();
        resultOfDefinition=locationDetectionRepository.getResultOfDefinition();
        changeFloor=locationDetectionRepository.getChangeFloor();

        floorNumber = new ObservableInt(2);
    }

    public void startProcessingCompleteKitsOfScansResult(List<List<ScanResult>> scanResults){
        locationDetectionRepository.startProcessingCompleteKitsOfScansResult(scanResults);
    }

    public void arrowInc(){
        if (floorNumber.get()<4){
            floorNumber.set(floorNumber.get()+1);
            startFloorChanging();
        }
    }
    public void arrowDec(){
        if (floorNumber.get()>0){
            floorNumber.set(floorNumber.get()-1);
            startFloorChanging();
        }
    }
    public void startFloorChanging(){
        locationDetectionRepository.changeFloor(floorNumber.get());
    }
}
