package com.mrkiriss.wifilocalpositioning.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.mrkiriss.wifilocalpositioning.models.MapPoint;

public class MapWebView extends WebView {

    private Context context;
    private Canvas canvas;

    private Observer<MapPoint> currentPointObserver;

    MutableLiveData<MapPoint> currentPoint;

    public MapWebView(@NonNull Context context) {
        super(context);
        System.out.println("1111111111111111111*");

    }
    public MapWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        System.out.println("1111111111111111111**");
    }
    public MapWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        System.out.println("1111111111111111111***");

    }

    public void initWebView(){
        currentPoint=new MutableLiveData<>();

        currentPointObserver = mapPoint -> drawPoint(currentPoint.getValue().getX(), currentPoint.getValue().getY());

        initObservers();
    }

    private void drawPoint(float x, float y){
        System.out.println("DRAWING started");
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(100+x,50+y,100,paint);
    }

    public void changeLocation(MapPoint newLocation){
        currentPoint.setValue(newLocation);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        this.canvas=canvas;

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getWidth()/2,getHeight()/2,10,paint);
    }

    private void initObservers(){
        currentPoint.observeForever(currentPointObserver);
    }
    public void removeObservers(){
        currentPoint.removeObserver(currentPointObserver);
    }
}
