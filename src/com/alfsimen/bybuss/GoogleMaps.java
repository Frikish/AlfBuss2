package com.alfsimen.bybuss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.maps.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import no.norrs.busbuddy.pub.api.BusBuddyAPIServiceController;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoogleMaps extends MapActivity {
    private MapView mapView;
    private MapController mapController;
    private MyLocationOverlay myLocOverlay;
    private Button searchButton;
    private AutoCompleteTextView searchBar;
    private static ArrayList<busStop> holdeplasser;
    private OverlayItem overlayItem;
    private MapsOverlay itemizedOverlay;

    private boolean stopFillDone;

    public static OverlayItem fromItem;
    public static OverlayItem toItem;

    private List<Overlay> mapOverlays;
    private AtbBussorakel bussen;
    private InputMethodManager imm;
    private AlertDialog.Builder answerDialog;
    private AlertDialog.Builder aboutDialog;
    public static AlertDialog.Builder internetWarning;

    private DBHelper db;
    private Cursor cursor;
    private ArrayList<String> searches;
    private ArrayAdapter<String> adapter;

    private SharedPreferences prefs;

    public static BusBuddyAPIServiceController realtimeController;

    private Gson gson;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestCustomTitleBar();
        setContentView(R.layout.main);
        //setCustomTitle(getString(R.string.custom_title));

        stopFillDone = false;

        mapView = (MapView) findViewById(R.id.MapView);
        mapView.setBuiltInZoomControls(true);

        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mapController = mapView.getController();
        mapController.setZoom(15);
        GeoPoint point = new GeoPoint((int) (63.4181 * 1E6), (int) (10.4057 * 1E6));
        mapController.setCenter(point);

        myLocOverlay = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocOverlay);
        if(!myLocOverlay.enableMyLocation()) {
            Toast.makeText(getBaseContext(), R.string.toast_turn_on_gps_wifi, Toast.LENGTH_LONG).show();
        }
        myLocOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mapView.getController().animateTo(myLocOverlay.getMyLocation());
                }
        });

        mapOverlays = mapView.getOverlays();
        new mapFillBusStopLoadThread().execute();

        createInternetWarningDialog();
        if(!checkConnection()) {
            internetWarning.show();
        }

        searchButton = (Button) findViewById(R.id.search_button);
        searchBar = (AutoCompleteTextView) findViewById(R.id.search_entry_autocomplete);

        bussen = new AtbBussorakel();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        /*
        *   Searchbaren
         */
        searchBar.setMaxWidth(searchBar.getWidth());    //wait wat?
        searchBar.setOnClickListener(new SearchBarClickListener());
        searchBar.setOnKeyListener(new SearchBarOnKeyListener());
        searchBar.addTextChangedListener(new SearchBarTextChangedListener());
        searchBar.setOnFocusChangeListener(new SearchBarOnFoucusChange());

        searchButton.setOnClickListener(new SearchButtonOnClickListener());

        db = new DBHelper(this);
        cursor = db.getAllHistoryRows();

        if(cursor != null) {
            cursor.moveToFirst();
            searches = new ArrayList<String>();
            for(int i = 0; i < cursor.getCount(); i++) {
                searches.add(cursor.getString(1));
                cursor.moveToNext();
            }

            adapter = new ArrayAdapter<String>(this, R.layout.history_list_item, searches);
            searchBar.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            //searchBar.setOnCreateContextMenuListener(new OnItemLongHold());
        }
        try {
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        realtimeController = new BusBuddyAPIServiceController(getString(R.string.realtimeAPIkey));

        answerDialog();
        aboutDialog();

        if(!prefs.getBoolean("firstTimeUse", false)) {
            aboutDialog.show();
            final SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("firstTimeUse", true);
            edit.commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //saveState();
        myLocOverlay.disableMyLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myLocOverlay.enableMyLocation();
        populateFields();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myLocOverlay.disableMyLocation();
        db.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(holdeplasser != null) {
            outState.putSerializable("busStops", holdeplasser);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Serializable busStops = savedInstanceState.getSerializable("busStops");
        if(busStops != null) {
            holdeplasser = (ArrayList<busStop>) busStops;
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
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
                    //ProgressDialog load = ProgressDialog.show(this, getString(R.string.loading_title), getString(R.string.loading_text), true);
                    getAddressesOfCurrentPos(myLocOverlay.getMyLocation());
                    //load.dismiss();
                    imm.toggleSoftInput(0, 0);
                }
                else
                    Toast.makeText(getApplicationContext(), R.string.no_location_on_geolocate, Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_last_search:
                answerDialog.show();
                return true;
            case R.id.menu_about:
                aboutDialog.show();
                return true;
            case R.id.menu_AtB_schedules:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.AtBSchedulesURL)));
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    *   div functions
     */

    public static busStop getEqualHoldeplass(int locId) {
        String tmpId = Integer.toString(locId);
        Log.d("ALF: locationID", tmpId);
        String newChar;
        if(tmpId.charAt(4) == '1')
            newChar = "0";
        else
            newChar = "1";
        Log.d("ALF: newChar", newChar);
        String all = tmpId.substring(0, 4) + newChar + tmpId.substring(5);
        Log.d("ALF: newString", all);

        for(busStop holdeplass : holdeplasser) {
            if(holdeplass.getLocationId().equalsIgnoreCase(all)) {
                Log.d("ALF: returns new busStop", holdeplass.getLocationId());
                return holdeplass;
            }
        }
        return null;
    }

    public void createInternetWarningDialog() {
        internetWarning = new AlertDialog.Builder(mapView.getContext());
        internetWarning.setTitle("Internet");
        internetWarning.setMessage(R.string.internet_warning_message);
        internetWarning.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info==null || !info.isConnected()) {
            return false;
        }
        return true;
    }

    private void populateFields() {
        answerDialog.setMessage(prefs.getString(getString(R.string.prefs_last_answer), ""));
    }

    private void answerDialog() {
        answerDialog = new AlertDialog.Builder(mapView.getContext());
        answerDialog.setTitle(R.string.dialog_orakel_title);
        answerDialog.setMessage(R.string.dialog_orakel_message_noanswer);
        answerDialog.setNeutralButton(R.string.dialog_orakel_okbutton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        answerDialog.setPositiveButton(R.string.dialog_orakel_refreshbutton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(prefs.getString(getString(R.string.prefs_last_search), null) != null) {
                    if(!checkConnection()) {
                        internetWarning.show();
                    }
                    else
                    {
                        bussen.setQuestion(prefs.getString(getString(R.string.prefs_last_search), ""));

                        new AtbThreadTest(getApplicationContext()).execute();
                    }
                }
            }
        });
    }

    private void aboutDialog() {
        aboutDialog = new AlertDialog.Builder(mapView.getContext());
        aboutDialog.setTitle(getString(R.string.app_name));
        aboutDialog.setMessage(getString(R.string.about_string));
        aboutDialog.setIcon(R.drawable.icon);
        aboutDialog.setPositiveButton(R.string.dialog_orakel_okbutton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public void doSearch() {
        Toast.makeText(getApplicationContext(), R.string.toast_wait_for_oracle, Toast.LENGTH_LONG).show();
        if(searchBar.getText().length() <= 0) {
            Toast.makeText(getApplicationContext(), R.string.toast_empty_question, Toast.LENGTH_LONG).show();
        }
        else {
            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
            searchBar.setText(searchBar.getText().toString().trim().replaceAll("\\s+", " "));
            searchBar.setSelection(searchBar.getText().toString().length());
            bussen.setQuestion(searchBar.getText().toString().trim());
            final SharedPreferences.Editor edit = prefs.edit();
            edit.putString(getString(R.string.prefs_last_search), searchBar.getText().toString().trim());
            edit.commit();

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
            if(words[i].equalsIgnoreCase(getString(R.string.search_separator_nospace))) {
                pos = i;
                til = true;
            }
        }
        if(pos == words.length -1) {
            Toast.makeText(getApplicationContext(), R.string.toast_need_two_stops, Toast.LENGTH_LONG).show();
        }
        else if(searchBar.getText().length() <= 0)
        {
            Toast.makeText(getApplicationContext(), R.string.toast_empty_question, Toast.LENGTH_LONG).show();
        }
        else if(searchBar.getText().toString().equals(getString(R.string.search_field)))
        {
            Toast.makeText(getApplicationContext(), R.string.toast_dummy_text_question, Toast.LENGTH_LONG).show();
        }
        else if(words.length >= 3 && til)
        {
            String temp = "";
            for(int i = pos + 1; i < words.length; i++) {
                temp += words[i] + " ";
            }
            temp += getText(R.string.search_separator_nostart).toString();
            for(int i = 0; i < pos; i++) {
                temp += words[i] + " ";
            }

            searchBar.setText(temp);
            searchBar.setSelection(searchBar.getText().toString().length());

            if(!checkConnection()) {
                internetWarning.show();
            }
            else {
                itemizedOverlay.reverseStops();
                doSearch();
            }

            /*String tmp = searchBar.getText().toString();
            String [] parts = tmp.split(getString(R.string.search_separator_nospace));
            if(parts.length == 2)
            {
                parts = tmp.split(getString(R.string.search_separator));
                String newString = parts[1] + getString(R.string.search_separator) + parts[0];
                searchBar.setText(newString);
                searchBar.setSelection(searchBar.getText().toString().length());
                if(!checkConnection()) {
                    internetWarning.show();
                }
                else
                    doSearch();
            }
            else
            {
                Toast.makeText(getApplicationContext(), R.string.toast_need_two_stops, Toast.LENGTH_LONG).show();
            }   */
        }
    }

    private void answerDialogSetText(String answer) {
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString(getString(R.string.prefs_last_answer), answer);
        edit.commit();
        answerDialog.setMessage(answer);
        answerDialog.show();
    }

    public void getAddressesOfCurrentPos(GeoPoint point) {
        Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try{
            if(itemizedOverlay != null)
                itemizedOverlay.blankSearchBar();
            List<Address> addresses = geoCoder.getFromLocation(point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6, 1);

            String add = "";
            if(addresses.size() > 0) {
                for(int i = 0; i < addresses.get(0).getMaxAddressLineIndex() -1; i++) {
                    add += addresses.get(0).getAddressLine(i);
                }
            }
            itemizedOverlay.fra = add;
            searchBar.setText(add + getText(R.string.search_separator));
            searchBar.setSelection(searchBar.getText().toString().length());
        }
        catch (IOException e) {
            e.printStackTrace();
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
        try {
            c.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        _db.close();
        return false;
    }

    /*
    * Listeners
     */
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
                if(!checkConnection()) {
                    internetWarning.show();
                }
                else
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

    private final class SearchBarTextChangedListener implements TextWatcher {
        public void afterTextChanged(Editable s) {
            if(s.length() <= 0 && stopFillDone) {
                itemizedOverlay.blankSearchBar();
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}

    }

    private final class SearchButtonOnClickListener implements View.OnClickListener {
        public void onClick(View v) {
            if(!checkConnection()) {
                internetWarning.show();
            }
            else
                doSearch();
        }
    }

    /*
    * Asynctasks
     */

    class mapFillBusStopLoadThread extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mapOverlays.remove(itemizedOverlay);
            Toast.makeText(getApplicationContext(), R.string.toast_loading_stops, Toast.LENGTH_LONG).show();
            gson = new Gson();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //long startNow;
            //long endNow;
            //startNow = SystemClock.uptimeMillis();

            if(holdeplasser == null || holdeplasser.isEmpty()) {
                InputStream source = getResources().openRawResource(R.raw.busstops);
                Reader reader = new InputStreamReader(source);
                Type jsonStopsType = new TypeToken<jsonStops>() {}.getType();
                //Log.d("ALF: Type: ", jsonStopsType.toString());
                jsonStops stops = gson.fromJson(reader, jsonStopsType);
                holdeplasser = stops.getBusStops();
                try {
                    source.close();
                    reader.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                //endNow = SystemClock.uptimeMillis();
                //Log.d("ALF; HOLDEPLASSER LEST FRA FIL FØRSTE GANG", "time used: " + (endNow - startNow) + " ms");
            }

            Drawable drawable = getApplicationContext().getResources().getDrawable(R.drawable.gps_marker);
            itemizedOverlay = new MapsOverlay(drawable, mapView.getContext(), searchBar, searchButton, mapView);

            for(busStop stop : holdeplasser) {
                //count++;
                overlayItem = new OverlayItem(new GeoPoint((int) (stop.getLatitude() * 1E6), (int) (stop.getLongitude() * 1E6)), stop.getName(), stop.getLocationId());
                itemizedOverlay.addOverlay(overlayItem);
            //endNow = SystemClock.uptimeMillis();
            //Log.d("ALF; HOLDEPLASSER LAGT TIL OVERLAY FØRSTE GANG", "time used: " + (endNow - startNow) + " ms");
            }
            //startNow = SystemClock.uptimeMillis();
            itemizedOverlay.myPopulate();
            //endNow = SystemClock.uptimeMillis();
            //Log.d("ALF; MYPOPULATE", "time used: " + (endNow - startNow) + " ms");
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            mapOverlays.add(itemizedOverlay);
            if(myLocOverlay.getMyLocation() != null) {
                mapController.setCenter(myLocOverlay.getMyLocation());
            }
            else {
                mapController.setCenter(mapView.getMapCenter());
            }
            stopFillDone = true;
            //Toast.makeText(getApplicationContext(), "Loading av bussholdeplasser ferdig", Toast.LENGTH_SHORT).show();
        }
    }

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
            if (bussen.getAnswer().trim().equals("No question supplied"))
            {
                answerDialogSetText(getString(R.string.help_string));
                //answerView.setText(getString(R.string.answer_field));
            }
            else if(bussen.getAnswer().trim().startsWith(getString(R.string.orakel_specify_answer))) {
                answerDialogSetText(getString(R.string.orakel_specify_solution));
            }
            else
            {
                if (!isInDatabase(context, searchBar.getText().toString().trim()))
                {
                    new ListUpdateThread(1).execute();
                }
                answerDialogSetText(bussen.getAnswer());
            }
        }
    }

    class ListUpdateThread extends AsyncTask<Void, Void, Void> {
        private int mode = -1;

        public ListUpdateThread(int mode) {
            this.mode = mode;
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
                adapter.add(searchBar.getText().toString().trim());
                adapter.notifyDataSetChanged();
            }
        }
    }
}