package com.camsys.otp.plugins.reporting;


import java.util.Date;

// {"type_id":1, "from_lat":40.754910,"from_lng":-73.994102,"to_lat":40.754006,"to_lng":-73.988301,"request_time":"2018-11-01T00:00:00","server_time":"2018-11-01T00:00:00","arrive_by":1}
public class PlanReport {

    private static final int PLAN_TYPE = 1;

    private int typeId = PLAN_TYPE;

    private String fromPlace;

    private double fromLat;

    private double fromLon;

    private String toPlace;

    private double toLat;

    private double toLon;

    private Date requestTime;

    private Date serverTime;

    private boolean arriveBy;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getFromPlace() {
        return fromPlace;
    }

    public void setFromPlace(String fromPlace) {
        this.fromPlace = fromPlace;
    }

    public double getFromLat() {
        return fromLat;
    }

    public void setFromLat(double fromLat) {
        this.fromLat = fromLat;
    }

    public double getFromLon() {
        return fromLon;
    }

    public void setFromLon(double fromLon) {
        this.fromLon = fromLon;
    }

    public String getToPlace() {
        return toPlace;
    }

    public void setToPlace(String toPlace) {
        this.toPlace = toPlace;
    }

    public double getToLat() {
        return toLat;
    }

    public void setToLat(double toLat) {
        this.toLat = toLat;
    }

    public double getToLon() {
        return toLon;
    }

    public void setToLon(double toLon) {
        this.toLon = toLon;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public boolean isArriveBy() {
        return arriveBy;
    }

    public void setArriveBy(boolean arriveBy) {
        this.arriveBy = arriveBy;
    }
}
