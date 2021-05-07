package com.mrkiriss.wifilocalpositioning.data.models.preference;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

@Entity
@Data
public class Settings {
    @PrimaryKey
    private Long id; // always 0L

    private int scanInterval;
    private int numberOfScans;
}
