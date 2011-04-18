package com.alfsimen.bybuss;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.StrikethroughSpan;
import android.view.*;
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
    //private TextView answerView;
    private AutoCompleteTextView searchBar;
    private XmlParser xmlParser;
    private ArrayList<Holdeplass> holdeplasser;
    private OverlayItem overlayItem;
    private MapsOverlay itemizedOverlay;
    private List<Overlay> mapOverlays;
    //private List<Overlay> answerOverlay;
    private AtbBussorakel bussen;
    private InputMethodManager imm;
    //private ArrayList<String> addressList;
    private AlertDialog.Builder dialog;

    private DBHelper db;
    private Cursor cursor;
    private ArrayList<String> searches;
    private ArrayAdapter<String> adapter;

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
        //addressButton = (Button) findViewById(R.id.addressbutton);
        //reverseButton = (Button) findViewById(R.id.reversebutton);
        searchBar = (AutoCompleteTextView) findViewById(R.id.search_entry_autocomplete);
        //answerView = (TextView) findViewById(R.id.answer_TV);

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

        //reverseButton.setOnClickListener(new ReverseButtonOnClickListener());

        //saddressButton.setOnClickListener(new AddressButtonOnClickListener());

        searchButton.setOnClickListener((new SearchButtonOnClickListener()));

        db = new DBHelper(this);
        cursor = db.getAllHistoryRows();

        if(cursor != null) {
            cursor.moveToFirst();
           /* String searchesString = "";
            for(int i = 0; i < cursor.getCount(); i++) {
                searchesString += "\"" + cursor.getString(1) + "\"";
                cursor.moveToNext();
                if(!(i+1 == cursor.getCount())) {
                    searchesString += ", ";
                }
            } */
            searches = new ArrayList<String>();
            for(int i = 0; i < cursor.getCount(); i++) {
                searches.add(cursor.getString(1));
                cursor.moveToNext();
            }

            adapter = new ArrayAdapter<String>(this, R.layout.history_list_item, searches);
            searchBar.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        cursor.close();

        dialogSetup();
    }

    public void onPause(Bundle savedInstanceState) {
        super.onPause();
        myLocOverlay.disableMyLocation();
    }

    public void onResume(Bundle savedInstanceState) {
        super.onResume();
    }

    public void onStop(Bundle bundle) {
        super.onStop();
        myLocOverlay.disableMyLocation();
        db.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO: CHECK THIS OUT
    }

    @Override
    protected boolean isRouteDisplayed() {
        //TODO: implement?
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reverse_search:
                reverseSearch();
                return true;
            case R.id.menu_get_address:
                if(myLocOverlay.getMyLocation() != null) {
                    getAddressesOfCurrentPos(myLocOverlay.getMyLocation());
                    imm.toggleSoftInput(0, 0);
                }
                else
                    Toast.makeText(getApplicationContext(), "Ingen lokasjon funnet, skru på Geolocate", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_last_search:
                dialog.show();
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void dialogSetup() {
        dialog = new AlertDialog.Builder(mapView.getContext());
        dialog.setTitle("Svaret fra bussorakelet");
        dialog.setPositiveButton("Ferdig", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                return;
            }
        });
    }

    private void doSearch() {
        Toast.makeText(getApplicationContext(), "Venter på svar fra bussorakelet", Toast.LENGTH_LONG).show();
        if(searchBar.getText().length() <= 0) {
            Toast.makeText(getApplicationContext(), "Søkefeltet er tomt -_-", Toast.LENGTH_LONG).show();
        }
        else {
            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            bussen.setQuestion(searchBar.getText().toString().trim());

            new AtbThreadTest(getApplicationContext()).execute();
        }
    }

    private void reverseSearch()
    {
        String text = searchBar.getText().toString();
        String words [] = text.split(" ");
        boolean til = false;
        int pos = 0;
        for(int i = 0; i < words.length; i++) {
            if(words[i].equalsIgnoreCase("til")) {
                pos = i;
                til = true;
            }
        }
        if(pos == words.length -1) {
            Toast.makeText(getApplicationContext(), "Trenger 2 holdeplasser og ordet 'til' imellom de", Toast.LENGTH_LONG).show();
        }
        else if(searchBar.getText().length() <= 0)
        {
            Toast.makeText(getApplicationContext(), "Søkefeltet er tomt -.-", Toast.LENGTH_LONG).show();
        }
        else if(searchBar.getText().toString().equals(getString(R.string.search_field)))
        {
            Toast.makeText(getApplicationContext(), "Du må skrive inn noen holdeplasser, nå er det bare dummytekst i feltet", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Trenger 2 holdeplasser, prøv igjen...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void answerDialog(String answer) {
        dialog.setMessage(answer);
        dialog.show();
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
                    Toast.makeText(getBaseContext(), "Skru på Wifi/gps-posisjonering i settings", Toast.LENGTH_LONG).show();
                    myLocOverlay.disableMyLocation();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Geolokasjon skrudd på", Toast.LENGTH_SHORT).show();
                    myLocOverlay.runOnFirstFix(new Runnable() {
                        public void run() {
                            mapView.getController().animateTo(myLocOverlay.getMyLocation());
                           /* if(searchBar.getText().toString().equals(getString(R.string.search_field)) || searchBar.getText().toString().length() == 0) {
                                getAddressesOfCurrentPos(myLocOverlay.getMyLocation());
                            }  */    //TODO: fix so that this autofill of address works
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

    public static boolean isInDatabase(Context context, String search) {
        DBHelper _db = new DBHelper(context);
        Cursor c = _db.getAllHistoryRows();
        if (c != null) {
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                if (c.getString(1).equals(search)) {
                    c.close();
                    _db.close();
                    return true;
                }
                c.moveToNext();
            }
        }
        c.close();
        _db.close();
        return false;
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
                answerDialog(getString(R.string.answer_field));
                //answerView.setText(getString(R.string.answer_field));
            } else
            {
                if (!isInDatabase(context, searchBar.getText().toString().trim()))
                {
                    new ListUpdateThread(1, -1).execute();
                }
                answerDialog(bussen.getAnswer());

                //answerView.setText(bussen.getAnswer());
               // new MarkBusStops(getApplicationContext()).execute();
            }
        }
    }

    class ListUpdateThread extends AsyncTask<Void, Void, Void> {
        private int mode = -1;
        private int item = -1;

        public ListUpdateThread(int mode, int item) {
            this.mode = mode;
            this.item = item;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(mode == 1) {
                db.createHistoryItem(searchBar.getText().toString().trim(), bussen.getAnswer());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if(mode == 1) {
                ((ArrayAdapter<String>) adapter).add(searchBar.getText().toString().trim());
                ((ArrayAdapter<String>) adapter).notifyDataSetChanged();
            }
            /* else if(mode == 2) {
                ((ArrayAdapter<String>) adapter).remove();
            }           */
        }
    }
}
