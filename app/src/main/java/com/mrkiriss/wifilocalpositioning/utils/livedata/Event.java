package com.mrkiriss.wifilocalpositioning.utils.livedata;

import androidx.annotation.NonNull;

public class Event<T> {

    private boolean isHandled = false;
    private final T value;

    public Event(@NonNull T value) {
        this.value = value;
    }

    public T getValue() {
        if (isHandled) {
            return null;
        } else {
            isHandled = true;
            return value;
        }
    }

    public boolean isNotHandled() {
        return !isHandled;
    }
}
