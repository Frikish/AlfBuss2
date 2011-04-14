package com.alfsimen.bybuss;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.maps.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private Button reverseButton;
    private TextView answerView;
    private EditText searchBar;
    private XmlParser xmlParser;
    private ArrayList<Holdeplass> holdeplasser;
    private OverlayItem overlayItem;
    private MapsOverlay itemizedOverlay;
    private List<Overlay> mapOverlays;
    //private List<Overlay> answerOverlay;
    private AtbBussorakel bussen;
    private InputMethodManager imm;
    //private ArrayList<String> addressList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCustomTitleBar();
        setContentView(R.layout.main);
        setCustomTitle(getString(R.string.custom_title).toString());

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
        reverseButton = (Button) findViewById(R.id.reversebutton);
        searchBar = (EditText) findViewById(R.id.search_entry);
        answerView = (TextView) findViewById(R.id.answer_TV);

        geoButton.setOnClickListener(new GeoButtonClickListener());

        bussen = new AtbBussorakel();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        /*
        *   Searchbaren
         */
        searchBar.setMaxWidth(searchBar.getWidth());    //wait wat?
        searchBar.setOnClickListener(new SearchBarClickListener());
        searchBar.setOnKeyListener(new SearchBarOnKeyListener());
        searchBar.setOnFocusChangeListener(new SearchBarOnFoucusChange());

        reverseButton.setOnClickListener(new ReverseButtonOnClickListener());

        addressButton.setOnClickListener(new AddressButtonOnClickListener());

        searchButton.setOnClickListener((new SearchButtonOnClickListener()));
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

    protected void requestCustomTitleBar()
    {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    }

    protected void setCustomTitle(String msg)
    {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_titlebar);
        TextView tv = (TextView) getWindow().findViewById(R.id.headerTitleTextVw);
        tv.setText(msg);
    }

    /*
    *   div functions
     */

    private void doSearch() {
        answerView.setText("Venter på svar fra bussorakelet");
        if(searchBar.getText().length() <= 0) {
            answerView.setText("Søkefeltet er tomt -_-");
        }
        else {
            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            bussen.setQuestion(searchBar.getText().toString().trim());

            new AtbThreadTest(this).execute();
        }
    }

    private void reverseSearch()
    {
        String text = searchBar.getText().toString();
        String words [] = text.split(" ");
        boolean til = false;
        for(int i = 0; i < words.length; i++) {
            if(words[i].equalsIgnoreCase("til")) {
                til = true;
            }
        }
        if(searchBar.getText().length() <= 0)
        {
            answerView.setText("Søkefeltet er tomt -.-");
        }
        else if(til && words.length < 3) {
            answerView.setText("Trenger 2 holdeplasser og ordet 'til' imellom de");
        }
        else if(searchBar.getText().toString().equals(getString(R.string.search_field)))
        {
            answerView.setText("Du må skrive inn noen holdeplasser, nå er det bare dummytekst i feltet");
        }
        else if(words.length >= 3 && til)
        {
            String tmp = searchBar.getText().toString();
            String [] parts = tmp.split("til");
            if(parts.length == 2)
            {
                parts = tmp.split(" til ");
                String newString = parts[1] + " til " + parts[0];
                searchBar.setText(newString);

                doSearch();
            }
            else
            {
                answerView.setText("Trenger 2 holdeplasser, prøv igjen...");
            }
        }
    }

    public void getAddressesOfCurrentPos(GeoPoint point) {
        Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try{
            List<Address> addresses = geoCoder.getFromLocation(point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6, 1);

            String add = "";
            if(addresses.size() > 0) {
                for(int i = 0; i < addresses.get(0).getMaxAddressLineIndex() -1; i++) {
                    add += addresses.get(0).getAddressLine(i);
                }
            }
            searchBar.setText(add + " til ");
            searchBar.setSelection(searchBar.getText().toString().length());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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

    private final class SearchBarClickListener implements View.OnClickListener {
        public void onClick(View v) {
            if(searchBar.getText().toString().equals(getString(R.string.search_field))) {
                searchBar.setText("");
            }
        }
    }

    private final class SearchBarOnKeyListener implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                doSearch();
            }
            return false;
        }
    }

    private final class SearchBarOnFoucusChange implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if(!hasFocus) {
                imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            }
        }
    }

    private final class ReverseButtonOnClickListener implements View.OnClickListener {
        public void onClick(View v) {
            reverseSearch();
        }
    }

    private final class AddressButtonOnClickListener implements View.OnClickListener {
        public void onClick(View v) {
            if(myLocOverlay.getMyLocation() != null)
                getAddressesOfCurrentPos(myLocOverlay.getMyLocation());
        }
    }

    private final class SearchButtonOnClickListener implements View.OnClickListener {
        public void onClick(View v) {
            doSearch();
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

/*    class getAddressOfCurrentPos extends AsyncTask<GeoPoint, Void, Void> {
        @Override
        protected Void doInBackground(GeoPoint... points) {
            int count = points.length;
            for(int i = 0; i < count; i++) {
                try {
                    Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geoCoder.getFromLocation(points[i].getLatitudeE6() / 1E6, points[i].getLongitudeE6() /1E6, 1);
                    String add = "";
                    if(addresses.size() > 0) {
                        for(int j = 0; j < addresses.get(0).getMaxAddressLineIndex(); j++) {
                            add += addresses.get(0).getAddressLine(j);
                        }
                    }
                    searchBar.setText(add);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }  */

    /**
     * Threading for search query
     *
     * @author tmn
     */
    class AtbThreadTest extends AsyncTask<Void, Void, Void>
    {
        private Context context;

        public AtbThreadTest(Context context)
        {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            bussen.ask();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused)
        {
            if (bussen.getAnswer().trim().equals("No question supplied."))
            {
                answerView.setText(getString(R.string.answer_field));
            } else
            {
                //if (!isInDatabase(context, searchBar.getText().toString().trim()))
                //{
                //    new ListUpdateThread(1, -1).execute();
                //}
                answerView.setText(bussen.getAnswer());
               // new MarkBusStops(getApplicationContext()).execute();
            }
        }
    }
}
