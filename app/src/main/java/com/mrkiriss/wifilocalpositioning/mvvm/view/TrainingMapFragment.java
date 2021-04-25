package com.mrkiriss.wifilocalpositioning.mvvm.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;
import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentTrainingMapBinding;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.LocationDetectionViewModel;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.TrainingMapViewModel;

public class TrainingMapFragment extends Fragment {

    private TrainingMapViewModel viewModel;
    private FragmentTrainingMapBinding binding;
    private PhotoView photoView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel=new ViewModelProvider(this).get(TrainingMapViewModel.class);

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_training_map, container, false);

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
                // потребовать изменения изображения
                viewModel.getInputX().set(String.valueOf(x));
                viewModel.getInputY().set(String.valueOf(y));
                viewModel.collectMapPointFromInput();
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }
        });

        binding.setViewModel(viewModel);


        return binding.getRoot();
    }
}