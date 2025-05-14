package com.flatshire.fbis.domain;

import java.util.Objects;

public class BusInfo {

    private String operatorRef;
    private String lineRef;

    public String getOperatorRef() {
        return operatorRef;
    }

    public String getLineRef() {
        return lineRef;
    }

    public void setOperatorRef(String operatorRef) {
        this.operatorRef = operatorRef;
    }

    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }

    public static BusInfo of(String operatorRef, String lineRef) {
        BusInfo busInfo = new BusInfo();
        busInfo.setLineRef(lineRef);
        busInfo.setOperatorRef(operatorRef);
        return busInfo;
    }

    @Override
    public String toString() {
        return "BusInfo{" +
                "operatorRef='" + operatorRef + '\'' +
                ", lineRef='" + lineRef + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusInfo busInfo = (BusInfo) o;
        return Objects.equals(operatorRef, busInfo.operatorRef) && Objects.equals(lineRef, busInfo.lineRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operatorRef, lineRef);
    }
}
