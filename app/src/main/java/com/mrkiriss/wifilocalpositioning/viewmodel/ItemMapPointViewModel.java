package com.mrkiriss.wifilocalpositioning.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ItemMapPointViewModel extends ViewModel {
    private final MapPoint mapPoint;

    private final MutableLiveData<MapPoint> requestToDelete;

    public ItemMapPointViewModel(MapPoint mapPoint, MutableLiveData<MapPoint> requestToDelete){
        this.mapPoint=mapPoint;
        this.requestToDelete=requestToDelete;
    }

    public void onDeleteClick(){
        requestToDelete.setValue(mapPoint);
    }
}
