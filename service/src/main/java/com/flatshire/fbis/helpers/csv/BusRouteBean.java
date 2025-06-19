package com.flatshire.fbis.helpers.csv;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BusRouteBean {

    @CsvBindByPosition(position = 0)
    private String description;

    @CsvDate(value = "yyyy-MM-dd'T'HH:mm:ss")
    @CsvBindByPosition(position = 1)
    private LocalDateTime recordedTime;

    @CsvBindByPosition(position = 2)
    private BigDecimal latitude;

    @CsvBindByPosition(position = 3)
    private BigDecimal longitude;

    public String getDescription() {
        return description;
    }

    public BusRouteBean setDescription(String description) {
        this.description = description;
        return this;
    }

    public LocalDateTime getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(LocalDateTime recordedTime) {
        this.recordedTime = recordedTime;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BusRouteBean setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
        return this;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public BusRouteBean setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
        return this;
    }
}
