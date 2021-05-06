package com.mrkiriss.wifilocalpositioning.data.models.general;

import java.util.List;

import lombok.Data;

@Data
public class ListOfHints {
    public static final String TARGET_ROUTE_START="route start";
    public static final String TARGET_ROUTE_END="route end";
    public static final String TARGET_FIND="find";

    private final List<String> hints;
    private final String target;

    public ListOfHints(List<String> hints, String target){
        this.hints = hints;
        this.target = target;
    }
}
