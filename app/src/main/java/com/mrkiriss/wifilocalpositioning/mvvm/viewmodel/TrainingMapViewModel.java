package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.TrainingMapRepository;

import javax.inject.Inject;

import lombok.Data;

@Data
public class TrainingMapViewModel extends ViewModel {

    @Inject
    protected TrainingMapRepository repository;

    private ObservableInt selectedMod;
    private ObservableField<String> inputY;
    private ObservableField<String> inputX;
    private ObservableField<String> inputCabId;
    private ObservableInt selectedFloorId; // изменяется при изменении spinner
    private ObservableBoolean showMapPoints;
    private ObservableInt floorNumber; // изменяеться через стрелки
    private ObservableField<MapPoint> selectedMapPoint;
    private ObservableField<String> serverResponse;

    private LiveData<Floor> changeFloor;

    private MutableLiveData<String> toastContent;
    private MutableLiveData<int[]> moveCamera;

    public TrainingMapViewModel(){

        App.getInstance().getComponentManager().getTrainingMapSubcomponent().inject(this);

        changeFloor= repository.getChangeFloor();
        toastContent=repository.getToastContent();
        moveCamera=new MutableLiveData<>();

        selectedMod=new ObservableInt(0);
        inputX=new ObservableField<>("");
        inputY=new ObservableField<>("");
        inputCabId=new ObservableField<>("");
        selectedFloorId=new ObservableInt(2);
        showMapPoints=new ObservableBoolean(false);
        floorNumber = new ObservableInt(2);
        selectedMapPoint=new ObservableField<>();
        serverResponse=new ObservableField<>("");
    }

    public void processShowSelectedMapPoint(boolean afterButton){
        String inputX=this.inputX.get(), inputY=this.inputY.get(), inputCabId=this.inputCabId.get();
        int intX, intY;
        if (inputX.isEmpty() || inputY.isEmpty() || !inputX.matches("\\d+") || !inputY.matches("\\d+")
                || (intX=Integer.parseInt(inputX))<=0 || (intY=Integer.parseInt(inputY))<=0){
            toastContent.setValue("Коодринаты некорректны");
            return;
        }
        if (afterButton) {
            floorNumber.set(selectedFloorId.get());
            try {
                moveCamera.setValue(new int[]{intX, intY,
                        changeFloor.getValue().getFloorSchema().getWidth(), changeFloor.getValue().getFloorSchema().getHeight()});
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            selectedFloorId.set(floorNumber.get());
        }
        MapPoint mapPoint = new MapPoint(intX, intY, inputCabId);
        selectedMapPoint.set(mapPoint);
        startFloorChanging(mapPoint);
    }


    // функции изменения номера этажа, вызывают менеджер выбора этажа в зависимости от факторов
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

    // запускает менеджер выбора конечного состояния этажа (вызов из-за стрелок)
    public void startFloorChanging(){
        Log.i("TrainingMapViewModel","startFloorChanging with showMapPoints="+showMapPoints.get());
        repository.changeFloor(floorNumber.get(), showMapPoints.get());
    }
    // изменяет этаж, добавляя к треб. рисунку точку mapPoint
    public void startFloorChanging(MapPoint mapPoint){
        Log.i("TrainingMapViewModel","startFloorChanging with selected MapPoint with showMapPoints="+showMapPoints.get());
        repository.changeFloor(floorNumber.get(), showMapPoints.get(), mapPoint);
    }

    // запрос на обновление списков всех точек на этажах
    public void startUpdatingMapPointLists(){
        repository.startDownloadingDataOnPointsOnAllFloors();
    }
}
