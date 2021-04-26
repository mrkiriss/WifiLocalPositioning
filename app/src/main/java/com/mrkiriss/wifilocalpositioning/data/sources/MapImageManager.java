package com.mrkiriss.wifilocalpositioning.data.sources;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapImageManager {

    private final AssetManager assetManager;
    private final Bitmap pointer;
    private final float dx;
    private final float dy;
    private final Paint paint;

    private final Map<FloorId, Floor> basicFloors;
    // данные о каждом этаже для удаления\добавления еденичной точки без обращения к серверу
    private Map<FloorId, List<MapPoint>> dataOnPointsOnAllFloors;

    public MapImageManager(Context context){
        this.assetManager=context.getAssets();
        pointer=getBitmapFromAsset("img/placeholder.png");
        dx=(float)pointer.getWidth()/2;
        dy=(float)pointer.getHeight();
        paint = new Paint();
        basicFloors=new HashMap<>();
        dataOnPointsOnAllFloors=new HashMap<>();
    }

    public Floor getBasicFloor(FloorId floorId){
        Floor preresult = findBasicFloor(floorId);
        if (preresult ==null){
            preresult =new Floor(floorId, getBitmapFromAsset(defineFilePath(floorId)), pointer);
            basicFloors.put(floorId, preresult);
        }
        return new Floor(floorId, preresult.getFloorSchema().copy(Bitmap.Config.ARGB_8888, true), pointer);
    }
    private Floor findBasicFloor(FloorId floorId){
        if (basicFloors.containsKey(floorId)){
            return basicFloors.get(floorId);
        }else{
            return null;
        }
    }

    public Floor getFloorWithPointer(FloorId floorId, int x, int y){
        Bitmap bitmap = getBasicFloor(floorId).getFloorSchema();

        bitmap = mergePointerAndBitmap(bitmap, x, y);

        if (bitmap==null) Log.e("downloadFloor", "floorWithPointer is null!");

        return new Floor(floorId, bitmap, pointer);
    }
    public Floor getFloorWithPointers(List<MapPoint> mapPoints, FloorId floorId){
        Bitmap bitmap = getBasicFloor(floorId).getFloorSchema();
        Bitmap resultBitmap = mergePointerSAndBitmap(bitmap, mapPoints);
        return new Floor(floorId, resultBitmap, pointer);
    }

    public Bitmap mergePointerAndBitmap(Bitmap floor, int x, int y){
        Bitmap blank = floor.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(blank);
        // редактируем координаты в соотв. с размером маркера
        x-=dx;
        y-=dy;
        canvas.drawBitmap(pointer, x, y, paint);
        return blank;
    }
    private Bitmap mergePointerSAndBitmap(Bitmap floor, List<MapPoint> mapPoints){
        Bitmap blank = floor.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(blank);
        for (MapPoint mapPoint:mapPoints) {
            // редактируем координаты в соотв. с размером маркера
            int x = (int)( mapPoint.getX()-dx);
            int y = (int)( mapPoint.getY()-dy);
            canvas.drawBitmap(pointer, x, y, paint);
        }
        return blank;
    }
    private Bitmap getBitmapFromAsset(String filePath) {

        InputStream istr;
        android.graphics.Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
            //bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
    private static String defineFilePath(FloorId floorId){
        String filePath="";
        switch (floorId){
            case ZERO_FLOOR:
                filePath= "img/floor0.webp";
                break;
            case FIRST_FLOOR:
                filePath= "img/floor1.webp";
                break;
            case SECOND_FLOOR:
                filePath= "img/floor2.webp";
                break;
            case THIRD_FLOOR:
                filePath= "img/floor3.webp";
                break;
            case FOURTH_FLOOR:
                filePath= "img/floor4.webp";
                break;
            default:
                filePath="img/floor2.webp";
                break;
        }
        return filePath;
    }

    public Map<FloorId, Floor> getBasicFloors() {
        return basicFloors;
    }
    public Map<FloorId, List<MapPoint>> getDataOnPointsOnAllFloors() {
        return dataOnPointsOnAllFloors;
    }
    public void setDataOnPointsOnAllFloors(Map<FloorId, List<MapPoint>> dataOnPointsOnAllFloors) {
        this.dataOnPointsOnAllFloors = dataOnPointsOnAllFloors;
    }
}
