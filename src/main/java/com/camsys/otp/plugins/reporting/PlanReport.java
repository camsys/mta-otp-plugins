/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camsys.otp.plugins.reporting;


import java.util.Date;

// {"type_id":1, "from_lat":40.754910,"from_lng":-73.994102,"to_lat":40.754006,"to_lng":-73.988301,"request_time":"2018-11-01T00:00:00","server_time":"2018-11-01T00:00:00","arrive_by":1}
public class PlanReport {

    private static final int PLAN_TYPE = 1;

    private int typeId = PLAN_TYPE;

    private double fromLat;

    private double fromLon;

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

    public boolean isArriveBy() {
        return arriveBy;
    }

    public void setArriveBy(boolean arriveBy) {
        this.arriveBy = arriveBy;
    }
}
