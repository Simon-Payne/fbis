package com.flatshire.fbis.messages;

public class BusPositionResponse {

    private String lineRef;
    private String latitude;
    private String longitude;

    public BusPositionResponse() {
    }

    public BusPositionResponse(String lineRef, String latitude, String longitude) {
        this.lineRef = lineRef;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLineRef() {
        return lineRef;
    }

    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
