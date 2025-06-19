package com.flatshire.fbis.messages;

import java.time.LocalDateTime;

public class BusPositionResponse {

    private String lineRef;
    private LocalDateTime recordedTime;
    private String latitude;
    private String longitude;

    public BusPositionResponse() {
    }

    public BusPositionResponse(String lineRef, LocalDateTime recordedTime, String latitude, String longitude) {
        this.lineRef = lineRef;
        this.recordedTime = recordedTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLineRef() {
        return lineRef;
    }

    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }

    public LocalDateTime getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(LocalDateTime recordedTime) {
        this.recordedTime = recordedTime;
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

    @Override
    public String toString() {
        return "BusPositionResponse{" +
                "lineRef='" + lineRef + '\'' +
                ", recordedTime='" + recordedTime + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
