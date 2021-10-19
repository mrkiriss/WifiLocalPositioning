package com.mrkiriss.wifilocalpositioning.data.sources;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.utils.livedata.Event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Data;

@Data
@Singleton
public class MapImageManager {

    private final AssetManager assetManager;

    private final Bitmap pointer;
    private final float dx;
    private final float dy;
    private final Bitmap ladder;
    private final float dxLadder;
    private final float dyLadder;
    private final Bitmap departure;
    private final float dxDeparture;
    private final float dyDeparture;
    private final Bitmap destination;
    private final float dxDestination;
    private final float dyDestination;
    private final Bitmap pointerAccepted;
    private final float dxPointerAccepted;
    private final float dyPointerAccepted;
    private final Paint paint;

    private final Map<FloorId, Floor> basicFloors;
    // данные о каждом этаже для удаления\добавления еденичной точки без обращения к серверу
    private Map<FloorId, List<MapPoint>> dataOnPointsOnAllFloors;

    // данные о каждом этаже с прорисованным маршрутом
    private final Map<FloorId, Floor> routeFloors;

    private final MutableLiveData<Event<String>> requestToRefreshFloor;

    @Inject
    public MapImageManager(Context context){
        this.assetManager=context.getAssets();

        pointer=getBitmapFromAsset("img/placeholder.png");
        dx=(float)pointer.getWidth()/2;
        dy=(float)pointer.getHeight();
        ladder=getBitmapFromAsset("img/ladder.png");
        dxLadder=(float)ladder.getWidth()/2;
        dyLadder=(float)ladder.getHeight()/2;
        departure=getBitmapFromAsset("img/departure.png");
        dxDeparture=(float)departure.getWidth()/2;
        dyDeparture=(float)departure.getHeight()/2;
        destination=getBitmapFromAsset("img/destination.png");
        dxDestination=(float)destination.getWidth()/2;
        dyDestination=(float)destination.getHeight()/2;
        pointerAccepted=getBitmapFromAsset("img/placeholder_accepted.png");
        dxPointerAccepted=(float)pointerAccepted.getWidth()/2;
        dyPointerAccepted=(float)pointerAccepted.getHeight();

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);

        basicFloors=new HashMap<>();
        dataOnPointsOnAllFloors=new HashMap<>();
        routeFloors=new HashMap<>();

        requestToRefreshFloor=new MutableLiveData<>();
    }

    // возвращает этаж с прорисованным маршрутом или, при его отсутсвии, обычный этаж
    public Floor getRouteFloor(FloorId floorId){
        if (routeFloors.containsKey(floorId)){
            return routeFloors.get(floorId);
        }else{
            return getBasicFloor(floorId);
        }
    }

    // запускает создание объектов этажей с маршрутами
    public void startCreatingFloorsWithRout(List<LocationPointInfo> info){
        routeFloors.clear();
        Map<FloorId, List<float[]>> requiredStrokes = createRequiredStrokes(info);
        createFloorsAccordingToRequirements(requiredStrokes);
        requestToRefreshFloor.setValue(new Event<>("Маршрут построен"));
    }
    // создаёт требования для рисования на этажах
    private Map<FloorId, List<float[]>> createRequiredStrokes(List<LocationPointInfo> info){
        Map<FloorId, List<float[]>> requiredStrokes = new HashMap<>();

        if (info.size()>0){ // требуем нарисовать изображения начала
            FloorId floorId = Floor.convertFloorIdToEnum(info.get(0).getFloorId());
            // проверка на наличие требования в карте
            requiredStrokes.put(floorId, new ArrayList<>());

            // добавление начала в требования этажа
            float[] departure = new float[]{info.get(0).getX(), info.get(0).getY(), -2, -2};
            Objects.requireNonNull(requiredStrokes.get(floorId)).add(departure);
        }

        for (int i=0;i<info.size()-1;i++){
            if (info.get(i).getFloorId()==info.get(i+1).getFloorId()){ // совпадает этаж - рисовать линию
                FloorId floorId = Floor.convertFloorIdToEnum(info.get(i).getFloorId());
                // проверка на наличие требования в карте
                if (!requiredStrokes.containsKey(floorId)) requiredStrokes.put(floorId, new ArrayList<>());

                // добавление отрезка в требования этажа
                float[] segment = new float[]{info.get(i).getX(), info.get(i).getY(), info.get(i+1).getX(), info.get(i+1).getY()};
                Objects.requireNonNull(requiredStrokes.get(floorId)).add(segment);
            }else{ // не совпадает этаж - требовать рисовать лестницы на двух этажах
                FloorId floorId1 = Floor.convertFloorIdToEnum(info.get(i).getFloorId());
                FloorId floorId2 = Floor.convertFloorIdToEnum(info.get(i + 1).getFloorId());

                // проверка на наличие требования в карте
                if (!requiredStrokes.containsKey(floorId1)) requiredStrokes.put(floorId1, new ArrayList<>());
                if (!requiredStrokes.containsKey(floorId2)) requiredStrokes.put(floorId2, new ArrayList<>());

                // добавление лестниц в требования этажа
                float[] ladder1 = new float[]{info.get(i).getX(), info.get(i).getY(), -1, -1};
                float[] ladder2 = new float[]{info.get(i+1).getX(), info.get(i+1).getY(), -1, -1};
                Objects.requireNonNull(requiredStrokes.get(floorId1)).add(ladder1);
                Objects.requireNonNull(requiredStrokes.get(floorId2)).add(ladder2);
            }
        }

        if (info.size()>0){ // требуем нарисовать изображения конца
            FloorId floorId = Floor.convertFloorIdToEnum(info.get(info.size()-1).getFloorId());
            // проверка на наличие требования в карте
            if (!requiredStrokes.containsKey(floorId)) requiredStrokes.put(floorId, new ArrayList<>());

            // добавление конца в требования этажа
            float[] destination = new float[]{info.get(info.size()-1).getX(), info.get(info.size()-1).getY(), -3, -3};
            requiredStrokes.get(floorId).add(destination);
        }

        return requiredStrokes;
    }
    private void createFloorsAccordingToRequirements(Map<FloorId, List<float[]>> requiredStrokes){
        for (FloorId floorId : requiredStrokes.keySet()){
            routeFloors.put(floorId, createSingleFloorAccordingToRequirement(floorId, requiredStrokes.get(floorId)));
        }
    }
    private Floor createSingleFloorAccordingToRequirement(FloorId floorId, List<float[]> requiredStrokes){
        Bitmap blank = getBasicFloor(floorId).getFloorSchema().copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(blank);
        // редактируем координаты в соотв. с размером маркера
        for (float[] requirement : requiredStrokes){
            if (requirement[2]==-1 && requirement[3]==-1){ // нужно нарисовать лестницу
                canvas.drawBitmap(ladder, requirement[0]-dxLadder, requirement[1]-dyLadder, paint);
            }else if (requirement[2]==-2 && requirement[3]==-2) { // отрисовать начало
                canvas.drawBitmap(departure, requirement[0]-dxDeparture, requirement[1]-dxDeparture, paint);
            }else if (requirement[2]==-3 && requirement[3]==-3) { // отрисовать конец
                canvas.drawBitmap(destination, requirement[0]-dxDestination, requirement[1]-dyDestination, paint);
            }else{
                canvas.drawLines(requirement ,paint);
            }
        }
        return new Floor(floorId, blank);
    }


    // возвращает, причём если сейчас нет в наличии - создаёт
    public Floor getBasicFloor(FloorId floorId){
        Floor preresult = findBasicFloor(floorId);
        if (preresult ==null){
            preresult =new Floor(floorId, getBitmapFromAsset(defineFilePath(floorId)), pointer);
            basicFloors.put(floorId, preresult);
        }
        return new Floor(floorId, preresult.getFloorSchema().copy(Bitmap.Config.ARGB_8888, true), pointer);
    }
    // проверяет наличие и возращает объекти или null
    private Floor findBasicFloor(FloorId floorId){
        if (basicFloors.containsKey(floorId)){
            return basicFloors.get(floorId);
        }else{
            return null;
        }
    }

    // возвращает этаж с единственным указателем
    public Floor getFloorWithPointer(Floor floor, int x, int y){
        Bitmap bitmap = floor.getFloorSchema();

        bitmap = mergePointerAndBitmap(bitmap, x, y);

        if (bitmap==null) Log.e("downloadFloor", "floorWithPointer is null!");

        return new Floor(floor.getFloorId(), bitmap, pointer);
    }
    // возвращает этаж с отображенными указателями из списка
    public Floor getFloorWithPointers(List<MapPoint> mapPoints, FloorId floorId){
        Bitmap bitmap = getBasicFloor(floorId).getFloorSchema();
        Bitmap resultBitmap = mergePointerSAndBitmap(bitmap, mapPoints);
        return new Floor(floorId, resultBitmap, pointer);
    }

    // возвращает картинку этажа с указателем по координатам
    public Bitmap mergePointerAndBitmap(Bitmap floor, int x, int y){
        Bitmap blank = floor.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(blank);
        // редактируем координаты в соотв. с размером маркера
        x-=dx;
        y-=dy;
        canvas.drawBitmap(pointer, x, y, paint);
        return blank;
    }
    // возвращает картинку этажа с указателями по координатам
    private Bitmap mergePointerSAndBitmap(Bitmap floor, List<MapPoint> mapPoints){
        Bitmap blank = floor.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(blank);
        for (MapPoint mapPoint:mapPoints) {
            // редактируем координаты в соотв. с размером маркера
            int x = (int)( mapPoint.getX()-dxPointerAccepted);
            int y = (int)( mapPoint.getY()-dyPointerAccepted);
            canvas.drawBitmap(pointerAccepted, x, y, paint);
        }
        return blank;
    }
    // загружает картинку из assets
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

}
