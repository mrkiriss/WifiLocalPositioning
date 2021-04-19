package com.mrkiriss.wifilocalpositioning.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;

import java.io.IOException;
import java.io.InputStream;

public class MapView extends View {

    private MapPoint currentLocation;
    private Floor currentFloor;

    private Paint paint;
    private Drawable pointer;

    public MapView(Context context) {
        super(context);

        paint = new Paint();
        paint.setColor(Color.RED);
        pointer= ContextCompat.getDrawable(context, R.drawable.ic_pointer);
    }
    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        //testBySchema(canvas);
        if (currentFloor==null || currentFloor.getFloorSchema()==null) return;

        Bitmap currentSchema = currentFloor.getFloorSchema();
        canvas.drawBitmap(currentSchema, getWidth()/2-currentSchema.getWidth()/2, getHeight()/2-currentSchema.getHeight()/2, paint);

        if (currentLocation==null || currentLocation.getFloorWithPointer()==null) return;

        if (currentLocation.getFloorWithPointer().getFloorId()==currentFloor.getFloorId()){
            int x = currentLocation.getX();
            int y = currentLocation.getY();
            pointer.setBounds(x, y, x+10, y+20);
            pointer.draw(canvas);
        }
    }

    private void testByCirce(Canvas canvas){
        paint = new Paint();
        paint.setColor(Color.RED);
        int color = Color.RED;
        for (int i = 0;i<getWidth();i+=getWidth()/7) {
            paint.setColor(color);
            color+=200;
            canvas.drawCircle(i, getHeight() / 2, 50, paint);
        }
    }
    private void testBySchema(Canvas canvas){
        Bitmap bitmap = getBitmapFromAsset(getContext());
        paint = new Paint();
        canvas.drawBitmap(bitmap, getWidth()/2-bitmap.getWidth()/2, getHeight()/2-bitmap.getHeight()/2, paint);
    }
    private Bitmap getBitmapFromAsset(Context context) {
        String filePath = "img/allf1.png";

        InputStream istr;
        android.graphics.Bitmap bitmap = null;
        try {
            istr = context.getAssets().open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public MapPoint getCurrentLocation() {
        return currentLocation;
    }
    public void setCurrentLocation(MapPoint currentLocation) {
        this.currentLocation = currentLocation;
    }
    public Floor getCurrentFloor() {
        return currentFloor;
    }
    public void setCurrentFloor(Floor currentFloor) {
        this.currentFloor = currentFloor;
    }
}
