package com.mrkiriss.wifilocalpositioning.data.sources.settings;

import android.content.Context;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import lombok.Data;

@Data
public class UUIDManager {
    private String mUUID;

    public UUIDManager(Context context){
        this.mUUID=generateUUID(context);
    }
    private String generateUUID(Context context){
        String androidId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        UUID uuid = null;
        if (androidId!= null && !androidId.isEmpty()) {
            uuid = UUID.nameUUIDFromBytes(androidId.getBytes(StandardCharsets.UTF_8));
        }

        if (uuid==null){
            uuid=UUID.randomUUID();
        }

        return uuid.toString();
    }
}
