package com.mrkiriss.wifilocalpositioning.ui.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentLocationDetectoinBinding;
import com.mrkiriss.wifilocalpositioning.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.ui.viewmodel.LocationDetectionViewModel;
import com.ortiz.touchview.TouchImageView;

public class LocationDetectionFragment extends Fragment {

    private MapView mapView;
    private TouchImageView touchImageView;
    private LocationDetectionViewModel viewModel;
    private FragmentLocationDetectoinBinding binding;

    private MapPoint currentLocation;
    private Floor currentFloor;

    private float eX;
    private float eY;
    float vX;
    float vY ;
    float picX;
    float picY;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel=new ViewModelProvider(this).get(LocationDetectionViewModel.class);
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_location_detectoin, container, false);
        binding.setViewModel(viewModel);

        createAndShowMapView();
        initObservers();
        viewModel.startFloorChanging();

        return binding.getRoot();
    }

    private void createAndShowMapView(){
        touchImageView=binding.zoomImageView;
        touchImageView.setMinZoom(1f);
        touchImageView.setMaxZoom(7f);
    }

    private void initObservers(){
        // прослушываем получение результата сканирования, вызываем обработчик данных
        viewModel.getKitOfScanResults().observe(getViewLifecycleOwner(), scanResults -> viewModel.startProcessingScanResultKit(scanResults));
        // прослушываем изменение местоположения, обновляем значение в mapView, если экран на этом этаже, перерисовываем
        viewModel.getResultOfDefinition().observe(getViewLifecycleOwner(), mapPoint -> {
            currentLocation=mapPoint;
            drawCurrentLocation(mapPoint);
            Log.d("changeMapPoint", "floorId: "+mapPoint.getFloorWithPointer().getFloorId());
        });
        // прослушываем изменение пола, вызываем перерисовку
        viewModel.getChangeFloor().observe(getViewLifecycleOwner(), floor -> {
            currentFloor=floor;
            drawCurrentFloor(floor);
        });
    }

    private void drawCurrentLocation(MapPoint mapPoint){
        if (mapPoint!=null && mapPoint.getFloorWithPointer()!=null && currentFloor!=null && currentFloor.getFloorSchema()!=null &&
                mapPoint.getFloorWithPointer().getFloorId()==currentFloor.getFloorId()) {
            touchImageView.setImageBitmap(mapPoint.getFloorWithPointer().getFloorSchema());
        }else{
            if (currentFloor==null || currentFloor.getFloorSchema()==null) return;
            touchImageView.setImageBitmap(currentFloor.getFloorSchema());
        }
    }
    private void drawCurrentFloor(Floor floor){
        if (floor==null || floor.getFloorSchema()==null) return;

        if (currentLocation!=null && currentLocation.getFloorWithPointer()!=null &&
                currentLocation.getFloorWithPointer().getFloorId()==floor.getFloorId()){
            touchImageView.setImageBitmap(currentLocation.getFloorWithPointer().getFloorSchema());
            Log.d("drawCurrentFloor", "Совпадение местоположения и этажа");
            Log.d("data", "currentFloor: "+currentFloor.getFloorId()+ " currentLoc: "+currentLocation.getFloorWithPointer().getFloorId());

        }else{
            touchImageView.setImageBitmap(floor.getFloorSchema());
            Log.d("drawCurrentFloor", "Отрисовка этажа без местопложения");
            Log.d("data", "currentFloor: "+currentFloor.getFloorId()+ " currentLoc: "+currentLocation.getFloorWithPointer().getFloorId());
        }
    }
}