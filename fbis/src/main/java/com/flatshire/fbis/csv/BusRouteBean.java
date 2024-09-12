package com.flatshire.fbis.csv;

import com.opencsv.bean.CsvBindByPosition;

import java.math.BigDecimal;

public class BusRouteBean {

    @CsvBindByPosition(position = 0)
    private String description;

    @CsvBindByPosition(position = 1)
    private BigDecimal latitude;

    @CsvBindByPosition(position = 2)
    private BigDecimal longitude;

    public String getDescription() {
        return description;
    }

    public BusRouteBean setDescription(String description) {
        this.description = description;
        return this;
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
