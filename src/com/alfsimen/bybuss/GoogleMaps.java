package com.alfsimen.bybuss;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
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
    private ToggleButton geoButton;
    private Button searchButton;
    private Button addressButton;
    private EditText searchBar;

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

        geoButton = (ToggleButton) findViewById(R.id.togglebutton_geo);
        searchButton = (Button) findViewById(R.id.search_button);
        addressButton = (Button) findViewById(R.id.addressbutton);
        searchBar = (EditText) findViewById(R.id.search_entry);

        geoButton.setOnClickListener(new GeoButtonClickListener());
    }

    public void onPause(Bundle bundle) {
        myLocOverlay.disableMyLocation();
    }

    @Override
    protected boolean isRouteDisplayed() {
        //TODO: implement?
        return false;
    }

    /*
    * Listeners
     */

    private final class GeoButtonClickListener implements View.OnClickListener {
        public void onClick(View v) {
            if(geoButton.isChecked()) {
                if(!myLocOverlay.enableMyLocation()) {
                    Toast.makeText(getBaseContext(), "Skru på Wifi/gps-posisjonering i settings", Toast.LENGTH_LONG);
                    myLocOverlay.disableMyLocation();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Geolokasjon skrudd på", Toast.LENGTH_SHORT).show();
                    myLocOverlay.runOnFirstFix(new Runnable() {
                        public void run() {
                            mapView.getController().animateTo(myLocOverlay.getMyLocation());
                        }
                    });
                }
            }
            else {
                myLocOverlay.disableMyLocation();
                Toast.makeText(getApplicationContext(), "Geolokasjon skrudd av", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
