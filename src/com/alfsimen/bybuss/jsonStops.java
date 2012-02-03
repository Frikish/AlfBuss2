package com.alfsimen.bybuss;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alf
 * Date: 7/3/11
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class jsonStops implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<busStop> busStops;

    public ArrayList<busStop> getBusStops() {
        return busStops;
    }

    public void setBusStops(ArrayList<busStop> busStops) {
        this.busStops = busStops;
    }
}
