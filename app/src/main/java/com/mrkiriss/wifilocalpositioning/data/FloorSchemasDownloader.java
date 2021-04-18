package com.mrkiriss.wifilocalpositioning.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.models.map.FloorId;

import java.io.IOException;
import java.io.InputStream;

public class FloorSchemasDownloader {

    private AssetManager assetManager;
    private Bitmap pointer;
    private Paint paint;

    public FloorSchemasDownloader(Context context){
        this.assetManager=context.getAssets();
        pointer=getBitmapFromAsset("img/placeholder.png");
        paint = new Paint();
    }

    public Floor downloadFloor(FloorId floorId){
        Bitmap bitmap = getBitmapFromAsset(defineFilePath(floorId));
        return new Floor(floorId, bitmap, pointer);
    }

    public Floor downloadFloor(FloorId floorId, int x, int y){
        Bitmap bitmap = getBitmapFromAsset(defineFilePath(floorId));
        bitmap = mergePointerAndFloor(bitmap, x, y);

        if (bitmap==null)Log.e("downloadFloor", "floorWithPointer is null!");

        return new Floor(floorId, bitmap, pointer);
    }

    private Bitmap mergePointerAndFloor(Bitmap floor, int x, int y){
        Bitmap blank = floor.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(blank);
        canvas.drawBitmap(pointer, x, y, paint);
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
}
