package com.mrkiriss.wifilocalpositioning.data.models;

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

    public boolean isHandled() {
        return isHandled;
    }
}
