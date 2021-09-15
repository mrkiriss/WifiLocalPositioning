package com.mrkiriss.wifilocalpositioning.data.models.search;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity
public class PreviousNameInput {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String inputName;
    private long inputDate;
}
