package com.mrkiriss.wifilocalpositioning.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.adapters.MapPointsRVAdapter;
import com.mrkiriss.wifilocalpositioning.adapters.ScanInfoRVAdapter;
import com.mrkiriss.wifilocalpositioning.data.sources.ViewModelFactory;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentTrainingMapBinding;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.viewmodel.SelectedMapPointViewModel;
import com.mrkiriss.wifilocalpositioning.viewmodel.TrainingMapViewModel;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public class TrainingMapFragment extends Fragment {

    @Inject
    protected ViewModelFactory viewModelFactory;

    private TrainingMapViewModel viewModel;
    private SelectedMapPointViewModel selectedMapPointViewModel;

    private FragmentTrainingMapBinding binding;

    private PhotoView photoView;
    private MapPointsRVAdapter mapPointsAdapter;
    private ScanInfoRVAdapter scanInfoAdapter;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        App.getInstance().getComponentManager().getViewModelSubcomponent().inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(TrainingMapViewModel.class);
        selectedMapPointViewModel = new ViewModelProvider(this, viewModelFactory).get(SelectedMapPointViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_training_map, container, false);

        initScanInfoAdapter();
        initMapPointsAdapter();
        initPhotoView();
        initSelectedMapPointView();
        initObservers();

        binding.setViewModel(viewModel);

        return binding.getRoot();
    }

    private void initScanInfoAdapter(){
        scanInfoAdapter=new ScanInfoRVAdapter();
        binding.includeScanningMode.scanInfoRecyclerView.setAdapter(scanInfoAdapter);
        binding.includeScanningMode.scanInfoRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    private void initMapPointsAdapter(){
        mapPointsAdapter=new MapPointsRVAdapter();
        binding.includeNeighboursMode.mapPointsRV.setAdapter(mapPointsAdapter);
        binding.includeNeighboursMode.mapPointsRV.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    private void initSelectedMapPointView() {
        selectedMapPointViewModel.setMapPointWithOtherMoves(viewModel.getSelectedMapPoint());

        binding.includeScanningMode.includeSeelctedMapPoint.setViewModel(selectedMapPointViewModel);
        binding.includeNeighboursMode.includeSeelctedMapPoint.setViewModel(selectedMapPointViewModel);
        binding.includeDeletingMode.includeSeelctedMapPoint.setViewModel(selectedMapPointViewModel);

    }
    private void initPhotoView(){
        photoView=binding.photoViewTraining;
        photoView.setMaximumScale(7);
        photoView.setMinimumScale(1);
        photoView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // определить координаты
                float[] values = new float[9];
                photoView.getImageMatrix().getValues(values);
                int x = (int) ((e.getX() - values[2]) / values[0]);
                int y = (int) ((e.getY() - values[5]) / values[4]);
                // сгладить координаты, чтобы они находились на одной прямой
                x=x-x%5;
                y=y-y%5;
                // потребовать изменения изображения
                viewModel.getInputX().set(String.valueOf(x));
                viewModel.getInputY().set(String.valueOf(y));
                viewModel.processShowSelectedMapPoint(false);
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }
        });
    }
    private void initObservers(){
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();

        // запрос на изменение номера этажа
        viewModel.getChangeFloor().observe(lifecycleOwner, floor -> {
            if (floor==null){
                photoView.setImageBitmap(null);
                return;
            }
            changeBitmap(floor.getFloorSchema());
        });
        // запрос на обновление картинки этажа
        viewModel.getRequestToUpdateFloor().observe(lifecycleOwner, content->viewModel.startFloorChanging());
        // запрос на показ Toast
        viewModel.getToastContent().observe(lifecycleOwner, this::showToast);
        // запрос на изменение позиции камеры на карте
        viewModel.getMoveCamera().observe(lifecycleOwner, this::moveCamera);
        // запрос на обновления ответа сервера в переменной Observable
        viewModel.getServerResponseRequest().observe(lifecycleOwner, s -> viewModel.getServerResponse().set(s));
        // запрос на обработку пакета сканирований
        viewModel.getCompleteKitsOfScansResult().observe(lifecycleOwner, data->viewModel.processCompleteKitsOfScanResults(data));
        // запрос на обновления количества оставшихся сканирований
        viewModel.getRemainingNumberOfScanning().observe(lifecycleOwner, integer -> viewModel.getRemainingNumberOfScanKits().set(integer));
        // обработка событий, связанных с RV для mapPoints при изменении связей
        viewModel.getServerConnectionsResponse().observe(lifecycleOwner, mapPoints -> {
            viewModel.processServerConnectionsResponse(mapPoints);
            mapPointsAdapter.setContent(mapPoints);
        });
        mapPointsAdapter.getRequestToDeleteMapPoint().observe(lifecycleOwner, mapPoint -> {
            mapPointsAdapter.deleteMapPoint(mapPoint);
            viewModel.processDeleteSecondly(mapPoint);
        });
        // запрос на добавление новой точки-соседки в адаптер
        viewModel.getRequestToAddSecondlyMapPointToRV().observe(lifecycleOwner, mapPoint -> mapPointsAdapter.addMapPoint(mapPoint));
        // запрос на обновление списока точек-соседей в адаптере
        viewModel.getRequestToChangeSecondlyMapPointListInRV().observe(lifecycleOwner, mapPoints -> mapPointsAdapter.setContent(mapPoints));
        // вставляем информацию о сканированиях точки в адаптер
        viewModel.getRequestToSetListOfScanInformation().observe(lifecycleOwner, content -> scanInfoAdapter.setContent(content));
        // запрос на обновление информации о состоянии запроса к серверу
        viewModel.getRequestToUpdateInteractionWithServerIsCarriedOut().observe(lifecycleOwner, interaction -> viewModel.getInteractionWithServerIsCarriedOut().set(interaction));
        // запрос на обновление описания запроса к серверу
        viewModel.getRequestToUpdateDescriptionOfInteractionWithServer().observe(lifecycleOwner, desc -> viewModel.getDescriptionOfInteractionWithServer().set(desc));


        viewModel.startFloorChanging();
    }

    private void changeBitmap(Bitmap bitmap){
        Log.i("TrainingMapFragment","request to change bitmap" );
        Matrix matrix = new Matrix();
        photoView.getSuppMatrix(matrix);
        photoView.setImageBitmap(bitmap);
        photoView.setSuppMatrix(matrix);
    }
    private void showToast(String content){
        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
    }
    private void moveCamera(int[] coordinates){
        photoView.setScale(4f, photoView.getRight()*(float)coordinates[0]/coordinates[2],
                photoView.getBottom()*(float)coordinates[1]/coordinates[3] , false);
    }
}