package com.flatshire.fbis.components;

import java.util.Arrays;

public enum ServiceMode {

    DATAFEED("datafeed"),
    OFFLINE("localfile");

    private final String modeDesc;

    ServiceMode(String modeDesc) {
        this.modeDesc = modeDesc;
    }

    public String getModeDesc() {
        return modeDesc;
    }

    public static ServiceMode fromProperties(String modeDesc) {
        return Arrays.stream(values()).filter(m -> m.getModeDesc().equals(modeDesc))
                .findAny().orElseThrow(() ->
                        new IllegalArgumentException("Invalid service mode '%s'".formatted(modeDesc)));
    }

}
