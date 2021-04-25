package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

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

    private ObservableField<String> inputY;
    private ObservableField<String> inputX;
    private ObservableField<String> inputCabId;
    private ObservableField<String> selectedFloorId;
    private ObservableBoolean showMapPoints;
    private ObservableInt floorNumber;
    private ObservableField<MapPoint> selectedMapPoint;

    private LiveData<Floor> changeFloor;

    private MutableLiveData<String> toastContent;

    public TrainingMapViewModel(){

        App.getInstance().getComponentManager().getTrainingMapSubcomponent().inject(this);

        changeFloor= repository.getChangeFloor();
        toastContent=repository.getToastContent();

        inputX=new ObservableField<>("");
        inputY=new ObservableField<>("");
        inputCabId=new ObservableField<>("");
        selectedFloorId=new ObservableField<>();
        showMapPoints=new ObservableBoolean(false);
        floorNumber = new ObservableInt(2);
        selectedMapPoint=new ObservableField<>();
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
        repository.changeFloor(floorNumber.get(), showMapPoints.get());
    }
    // (вызов из-за выбора точки на карте)
    public void startFloorChanging(MapPoint mapPoint){
        repository.changeFloor(floorNumber.get(), showMapPoints.get(), mapPoint);
    }

    // собирает данные о введённой в поля точке и вызывает менеджер выбора
    public void collectMapPointFromInput(){
        String inputX=this.inputX.get(), inputY=this.inputY.get(), inputCabId=this.inputCabId.get();
        if (inputX.isEmpty() || inputY.isEmpty() || inputCabId.isEmpty()){
            toastContent.setValue("Не всё поля заполнены");
            return;
        }
        MapPoint mapPoint = new MapPoint(Integer.parseInt(inputX), Integer.parseInt(inputY), inputCabId);
        selectedMapPoint.set(mapPoint);
        startFloorChanging(mapPoint);
    }

    // запрос на обновление списков всех точек на этажах
    public void startUpdatingMapPointLists(){
        repository.startDownloadingDataOnPointsOnAllFloors();
    }
}
