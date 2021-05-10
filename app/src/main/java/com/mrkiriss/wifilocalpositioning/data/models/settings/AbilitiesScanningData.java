package com.mrkiriss.wifilocalpositioning.data.models.settings;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

@Entity
@Data
public class AbilitiesScanningData {
    @PrimaryKey
    private Long id;
    private boolean needShowInformationAboutAbilities;
}
