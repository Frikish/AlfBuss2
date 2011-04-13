package com.alfsimen.bybuss;

import android.os.Bundle;
import com.google.android.maps.MapActivity;

/**
 * Created by IntelliJ IDEA.
 * User: alf
 * Date: 4/13/11
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleMaps extends MapActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_maps);
    }

    @Override
    protected boolean isRouteDisplayed() {
        //TODO: implement?
        return false;
    }
}
