package com.alfsimen.bybuss;

import android.os.Bundle;
import com.google.android.maps.*;

/**
 * Created by IntelliJ IDEA.
 * User: alf
 * Date: 4/13/11
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleMaps extends MapActivity {
    private MapView mapView;
    private MapController mapController;
    private GeoPoint lastPoint;
    private MyLocationOverlay myLocOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mapView = (MapView) findViewById(R.id.MapView);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(15);
        GeoPoint point = new GeoPoint((int) (63.41667 * 1E6), (int) (10.41667 * 1E6));
        mapController.setCenter(point);

        myLocOverlay = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocOverlay);
    }

    public void onPause(Bundle bundle) {
        myLocOverlay.disableMyLocation();
    }

    @Override
    protected boolean isRouteDisplayed() {
        //TODO: implement?
        return false;
    }
}
