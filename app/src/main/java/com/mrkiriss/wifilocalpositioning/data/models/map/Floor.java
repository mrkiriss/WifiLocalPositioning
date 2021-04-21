package com.mrkiriss.wifilocalpositioning.data.models.map;

import android.graphics.Bitmap;

import java.io.Serializable;

import lombok.Data;

@Data
public class Floor implements Serializable {

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
    public static int convertEnumToFloorId(FloorId floorId){
        switch (floorId){
            case ZERO_FLOOR:
                return 0;
            case FIRST_FLOOR:
                return 1;
            case SECOND_FLOOR:
                return 2;
            case THIRD_FLOOR:
                return 3;
            case FOURTH_FLOOR:
                return 4;
        }
        return 2;
    }
}