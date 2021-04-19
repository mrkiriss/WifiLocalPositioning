package com.mrkiriss.wifilocalpositioning.models.map;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

import lombok.Data;

@Data
public class Floor {

    private Bitmap floorSchema;
    private FloorId floorId;
    private Bitmap pointer;


    public Floor(FloorId floorId, Bitmap floorSchema, Bitmap pointer){
        this.floorId=floorId;
        this.floorSchema=floorSchema;
        this.pointer=pointer;
    }

    public static FloorId convertFloorIdToEnum(int floorId){
        switch (floorId){
            case 0:
                return FloorId.ZERO_FLOOR;
            case 1:
                return FloorId.FIRST_FLOOR;
            case 2:
                return FloorId.SECOND_FLOOR;
            case 3:
                return FloorId.THIRD_FLOOR;
            case 4:
                return FloorId.FOURTH_FLOOR;
        }
        return FloorId.SECOND_FLOOR;
    }
}