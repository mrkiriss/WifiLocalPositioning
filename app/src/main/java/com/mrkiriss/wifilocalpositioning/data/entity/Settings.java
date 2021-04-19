package com.mrkiriss.wifilocalpositioning.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

@Entity
@Data
public class Settings {
    @PrimaryKey
    private Long id;

    private long scanInterval;
    private int numberOfScans;
}
