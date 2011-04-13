package com.alfsimen.bybuss;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.maps.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
    private XmlParser xmlParser;
    private ArrayList<Holdeplass> holdeplasser;
    private OverlayItem overlayItem;
    private MapsOverlay itemizedOverlay;
    private List<Overlay> mapOverlays;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mapView = (MapView) findViewById(R.id.MapView);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(15);
        GeoPoint point = new GeoPoint((int) (63.4181 * 1E6), (int) (10.4057 * 1E6));
        mapController.setCenter(point);

        myLocOverlay = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocOverlay);

        mapOverlays = mapView.getOverlays();

        new mapFillBusStopLoadThread().execute();

        geoButton = (ToggleButton) findViewById(R.id.togglebutton_geo);
        searchButton = (Button) findViewById(R.id.search_button);
        addressButton = (Button) findViewById(R.id.addressbutton);
        searchBar = (EditText) findViewById(R.id.search_entry);

        geoButton.setOnClickListener(new GeoButtonClickListener());
    }

    public void onPause(Bundle bundle) {
        myLocOverlay.disableMyLocation();
    }

    public void onStop(Bundle bundle) {
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

    /*
    * Asynctasks
     */

    class mapFillBusStopLoadThread extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            InputStream in = getResources().openRawResource(R.raw.holdeplasser);
            xmlParser = new XmlParser(in);
            holdeplasser = xmlParser.getHoldeplasser();

            Drawable drawable = getApplicationContext().getResources().getDrawable(R.drawable.gps_marker);
            itemizedOverlay = new MapsOverlay(drawable, mapView.getContext(), searchBar);

            for(Holdeplass plass : holdeplasser) {
                overlayItem = new OverlayItem(new GeoPoint((int) (plass.getLat() * 1E6), (int) (plass.getLon() * 1E6)), plass.getName(), "");
                itemizedOverlay.addOverlay(overlayItem);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "Loader bussholdeplasser", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            mapOverlays.add(itemizedOverlay);
            if(myLocOverlay.getMyLocation() != null) {
                mapController.setCenter(myLocOverlay.getMyLocation());
            }
            else {
                mapController.setCenter(new GeoPoint((int) (63.4181 * 1E6), (int) (10.4057 * 1E6)));
            }
            Toast.makeText(getApplicationContext(), "Loading av bussholdeplasser ferdig", Toast.LENGTH_SHORT).show();
        }
    }
}
