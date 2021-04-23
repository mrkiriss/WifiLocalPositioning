package com.mrkiriss.wifilocalpositioning.data.sources.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

@Entity
@Data
public class Settings {
    @PrimaryKey
    private Long id; // always 0L

    private long scanInterval; // in ms
    private int numberOfScans; // {0, 1, 3 ,5}, 0 - система должна выбрать сама, основываясь на скорости сканирования устройства

    public int convertStringResourceToNumber(String resource){
        switch (resource){
            case "Автоматически":
                return 0;
            case "Максимально (5 ск.)":
                return 5;
            case "Достаточно (3 ск.)":
                return 3;
            case "Минимально (1 ск.)":
                return 1;
            default:
                return 1;
        }
    }
}
