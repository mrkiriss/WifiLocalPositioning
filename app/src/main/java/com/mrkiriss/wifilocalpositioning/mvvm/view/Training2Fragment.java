package com.mrkiriss.wifilocalpositioning.mvvm.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

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
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;


public class Training2Fragment extends Fragment {

    private MapImageManager downloader;
    private Bitmap floor;
    private PhotoView photoView;

    private float x;
    private float y;
    private float z;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        downloader=new MapImageManager(requireContext());
        floor=downloader.getBasicFloor(FloorId.FOURTH_FLOOR).getFloorSchema();

        View r = inflater.inflate(R.layout.fragment_training2, container, false);
        photoView =  r.findViewById(R.id.photo_view);
        photoView.setImageBitmap(floor);
        photoView.setMaximumScale(7);
        photoView.setMinimumScale(1);

        photoView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.i("dddddd", "111");

                return true;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.i("dddddd", "222");
                // определить координаты
                float[] values = new float[9];
                photoView.getImageMatrix().getValues(values);
                x = (e.getX() - values[2]) / values[0];
                y = (e.getY() - values[5]) / values[4];
                // скачать изображение
                Floor f = downloader.getFloorWithPointer(FloorId.FOURTH_FLOOR, (int) x, (int) y);
                Bitmap b = f.getFloorSchema();
                // сохранение местоположения камеры
                Matrix matrix = new Matrix();
                photoView.getSuppMatrix(matrix);
                photoView.setImageBitmap(b);
                photoView.setSuppMatrix(matrix);

                Log.i("double_tap_coords", "X: "+x+" Y: "+y+" dX: "+f.getPointer().getWidth()+" dY: "+f.getPointer().getHeight());
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                Log.i("dddddd", "333");

                return true;
            }
        });


        return r;
    }
}