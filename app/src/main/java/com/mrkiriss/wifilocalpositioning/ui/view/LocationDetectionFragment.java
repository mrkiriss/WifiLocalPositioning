package com.mrkiriss.wifilocalpositioning.ui.view;

import android.graphics.Matrix;
import android.icu.text.UnicodeSetSpanner;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.chrisbanes.photoview.PhotoView;
import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentLocationDetectionBindingImpl;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.ui.viewmodel.LocationDetectionViewModel;
import com.ortiz.touchview.TouchImageView;

public class LocationDetectionFragment extends Fragment {

    private TouchImageView touchImageView;
    private LocationDetectionViewModel viewModel;
    private FragmentLocationDetectionBindingImpl binding;

    private MapPoint currentLocation;
    private Floor currentFloor;
    private boolean isStandardFloor;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //SavedStateViewModelFactory factory = new SavedStateViewModelFactory(App.getInstance(), this);
        viewModel=new ViewModelProvider(this).get(LocationDetectionViewModel.class);

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_location_detection, container, false);
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
        touchImageView.setZoom(2f);

        touchImageView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                /*float[] values = new float[9];
                touchImageView.getMatrix().getValues(values);
                float relativeX = (e.getX() - values[2]) / values[0];
                float relativeY = (e.getY() - values[5]) / values[4];
                Log.i("double_click_coord", "X: "+relativeX+" Y: "+relativeY);*/

                Matrix inverseMatrix = new Matrix();
                touchImageView.getMatrix().invert(inverseMatrix);

                float[] point = new float[2];
                point[0] = e.getX();
                point[1] = e.getY();
                inverseMatrix.mapPoints(point);
                Log.i("double_click_coord", "X: "+point[0]+" Y: "+point[1]);
                return true;
            }
        });
    }

    private void initObservers(){
        // прослушываем получение результата сканирования, вызываем обработчик данных
        viewModel.getCompleteKitsOfScansResult().observe(getViewLifecycleOwner(), scanResults -> viewModel.startProcessingCompleteKitsOfScansResult(scanResults));
        // прослушываем изменение местоположения, обновляем значение в mapView, если экран на этом этаже, перерисовываем
        viewModel.getResultOfDefinition().observe(getViewLifecycleOwner(), mapPoint -> {
            currentLocation=mapPoint;
            drawCurrentLocation(mapPoint);

            Toast.makeText(getContext(), mapPoint.getTag(), Toast.LENGTH_SHORT).show();
            Log.i("changeMapPoint", "получена новая точка с floorId: "+mapPoint.getFloorWithPointer().getFloorId());
        });
        // прослушываем изменение пола, вызываем перерисовку
        viewModel.getChangeFloor().observe(getViewLifecycleOwner(), floor -> {
            currentFloor=floor;
            drawCurrentFloor(floor);
        });
        // прослышиваем кнопку показа текущего местоположения
        viewModel.getShowCurrentLocation().observe(getViewLifecycleOwner(), this::showCurrentLocation);
    }

    private void drawCurrentLocation(MapPoint mapPoint){
        if (mapPoint!=null && mapPoint.getFloorWithPointer()!=null && currentFloor!=null && currentFloor.getFloorSchema()!=null &&
                mapPoint.getFloorWithPointer().getFloorId()==currentFloor.getFloorId()) {
            touchImageView.setImageBitmap(mapPoint.getFloorWithPointer().getFloorSchema());
            Log.i("changeMapPoint", "draw mapPoint's schema");
            isStandardFloor=false;
        }else{
            if (currentFloor==null || currentFloor.getFloorSchema()==null || isStandardFloor) return;
            touchImageView.setImageBitmap(currentFloor.getFloorSchema());
            Log.i("changeMapPoint", "draw basic floor schema");
            isStandardFloor=true;
        }
    }
    private void drawCurrentFloor(Floor floor){
        if (floor==null || floor.getFloorSchema()==null) return;

        if (currentLocation!=null && currentLocation.getFloorWithPointer()!=null &&
                currentLocation.getFloorWithPointer().getFloorId()==floor.getFloorId()){
            touchImageView.setImageBitmap(currentLocation.getFloorWithPointer().getFloorSchema());
            isStandardFloor=false;
            Log.i("drawCurrentFloor", "Совпадение местоположения и этажа, этаж "+currentLocation.getFloorWithPointer().getFloorId());

        }else{
            touchImageView.setImageBitmap(floor.getFloorSchema());
            isStandardFloor=true;
            Log.i("drawCurrentFloor", "Отрисовка этажа без местопложения, этаж "+floor.getFloorId());
        }
    }
    private void showCurrentLocation(MapPoint mapPoint){
        if (mapPoint==null){
            Toast.makeText(getContext(), "Текущее местоположение не определено",Toast.LENGTH_SHORT).show();
        }else {
            viewModel.getFloorNumber().set(Floor.convertEnumToFloorId(mapPoint.getFloorWithPointer().getFloorId()));
            currentFloor=mapPoint.getFloorWithPointer();
            touchImageView.setImageBitmap(mapPoint.getFloorWithPointer().getFloorSchema());
            Log.i("changeMapPoint", "draw current mapPoint's schema");

            float x = (float)mapPoint.getX()/mapPoint.getFloorWithPointer().getFloorSchema().getWidth();
            float y = (float)mapPoint.getY()/mapPoint.getFloorWithPointer().getFloorSchema().getHeight();
            Log.i("changeZoom", "x="+x+" y="+y);
            touchImageView.setZoom(5, x, y);
            isStandardFloor=false;
        }
    }
}