package com.alfsimen.bybuss;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.format.Time;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import no.norrs.busbuddy.pub.api.model.Departure;
import no.norrs.busbuddy.pub.api.model.DepartureContainer;
import org.joda.time.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapsOverlay extends ItemizedOverlay {
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private Context mContext;

    public String fra = null;
    public String til = null;
    private EditText searchBar;
    private Button searchButton;
    private MapView mapView;

    private DepartureContainer departures;
    private Boolean realtimeDone;

    private AlertDialog.Builder realtime;

    public MapsOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
    }

    public MapsOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
    }

    public MapsOverlay(Drawable defaultMarker, Context context, EditText searchbar) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
        searchBar = searchbar;
    }

    public MapsOverlay(Drawable defaultMarker, Context context, EditText searchbar, Button searchbutton) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
        searchBar = searchbar;
        searchButton = searchbutton;
    }

     public MapsOverlay(Drawable defaultMarker, Context context, EditText searchbar, Button searchbutton, MapView mapView) {
        super(boundCenterBottom(defaultMarker));
        this.mContext = context;
        this.searchBar = searchbar;
        this.searchButton = searchbutton;
        this.mapView = mapView;
    }

    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
    }

    public void setMarker(OverlayItem overlay, Drawable marker) {
        overlay.setMarker(boundCenterBottom(marker));
    }

    public void myPopulate() {
        populate();
    }

    public void blankSearchBar() {
        fra = null;
        til = null;
        Drawable defaultMarker = mContext.getResources().getDrawable(R.drawable.gps_marker);
        if(GoogleMaps.fromItem != null) {
            setMarker(GoogleMaps.fromItem, defaultMarker);
            GoogleMaps.fromItem = null;
        }
        if(GoogleMaps.toItem != null) {
            setMarker(GoogleMaps.toItem, defaultMarker);
            GoogleMaps.toItem = null;
        }
        mapView.invalidate();
    }

    public void reverseStops() {
        String tmp = fra;
        fra = til;
        til = tmp;

        OverlayItem tmpOI = GoogleMaps.fromItem;
        GoogleMaps.fromItem = GoogleMaps.toItem;
        GoogleMaps.toItem = tmpOI;

        if(GoogleMaps.fromItem != null) {
            Drawable from = mContext.getResources().getDrawable(R.drawable.gps_marker_green);
            setMarker(GoogleMaps.fromItem, from);
        }
        if(GoogleMaps.toItem != null) {
            Drawable to = mContext.getResources().getDrawable(R.drawable.gps_marker_red);
            setMarker(GoogleMaps.toItem, to);
        }

        mapView.invalidate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }

    @Override
    public int size() {
        return mOverlays.size();
    }

    @Override
    protected boolean onTap(int index) {
        final OverlayItem item = mOverlays.get(index);
        new loadRealtimeInfoThread().execute(Integer.parseInt(item.getSnippet()));  //start loading the realtime departures

        String words [] = searchBar.getText().toString().split(" ");
        int pos = 0;
        for(int i = 0; i < words.length; i++) {
            if(words[i].equalsIgnoreCase(mContext.getText(R.string.search_separator_nospace).toString())) {
                fra = "";
                if(i < words.length-1) {
                    fra = null;
                    break;
                }
                for(int j = 0; j < i; j++) {
                    fra += words[j] + " ";
                }
            }
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(item.getTitle());

        if(fra == null) {
            dialog.setMessage(mContext.getText(R.string.reise_fra).toString() + " " + item.getTitle() + "?");
        }
        else {
            dialog.setMessage(mContext.getText(R.string.reise_til).toString() + " " + item.getTitle() + "?");
        }

        dialog.setPositiveButton(mContext.getText(R.string.ja).toString(), new DialogInterface.OnClickListener() {
            //@Override
            public void onClick(DialogInterface dialog, int which) {
                Drawable defaultMarker = mContext.getResources().getDrawable(R.drawable.gps_marker);
                if(fra == null) {
                    if(GoogleMaps.fromItem != null)
                        setMarker(GoogleMaps.fromItem, defaultMarker);
                    if(GoogleMaps.toItem != null)
                        setMarker(GoogleMaps.toItem, defaultMarker);

                    fra = item.getTitle();
                    searchBar.setText(item.getTitle() + mContext.getText(R.string.search_separator).toString());
                    searchBar.setSelection(searchBar.getText().toString().length());
                    Drawable from = mContext.getResources().getDrawable(R.drawable.gps_marker_green);
                    GoogleMaps.fromItem = item;
                    setMarker(item, from);
                    mapView.invalidate();
                }
                else {
                    til = item.getTitle();
                    searchBar.setText(fra + mContext.getText(R.string.search_separator).toString() + til);
                    searchBar.setSelection(searchBar.getText().toString().length());
                    Drawable to = mContext.getResources().getDrawable(R.drawable.gps_marker_red);
                    GoogleMaps.toItem = item;
                    setMarker(item, to);
                    mapView.invalidate();
                    searchButton.performClick();
                    fra = til = null;
                }
            }
        });

        dialog.setNeutralButton(mContext.getText(R.string.nei).toString(), new DialogInterface.OnClickListener() {
            //@Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.setNegativeButton(mContext.getText(R.string.realtime_button).toString(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();
                realtime = new AlertDialog.Builder(mContext);
                String listen = "";
                List<Departure> departureList;

                //TODO: fixe timer som oppdaterer hvert minutt/halvminutt mens dialogen er oppe?

                if(!realtimeDone) {
                    //ProgressDialog load = ProgressDialog.show(mContext, "", mContext.getString(R.string.realtime_loading), true);
                    try {
                        departures = GoogleMaps.realtimeController.getBusStopForecasts(Integer.parseInt(item.getSnippet()));
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    //load.dismiss();
                }

                if(departures != null)
                {
                    departureList = departures.getDepartures();

                    if(departureList != null)
                    {
                        if(departureList.size() > 0) {
                            if(departures.isGoingTowardsCentrum()) {
                                realtime.setTitle(item.getTitle() + " " + mContext.getString(R.string.realtime_mot_sentrum));
                            }
                            else {
                                realtime.setTitle(item.getTitle() + " " + mContext.getString(R.string.realtime_fra_sentrum));
                            }

                            Iterator<Departure> iterator = departureList.iterator();

                            Departure dep;
                            int count = 0;
                            LocalDateTime now = new LocalDateTime();

                            while(iterator.hasNext()) {
                                count++;
                                if(count > 1)
                                    listen += "\n\n";
                                dep = iterator.next();
                                if(dep.isRealtimeData()) {
                                    listen += mContext.getString(R.string.realtime_linje) + dep.getLine() + mContext.getString(R.string.realtime_mot) + dep.getDestination() + "\n";
                                    Minutes m = Minutes.minutesBetween(now, dep.getRegisteredDepartureTime());
                                    if(m.getMinutes() == 0)
                                        listen += mContext.getString(R.string.realtime_NOW);
                                    else
                                        listen += Integer.toString(m.getMinutes()) + "min";
                                }
                                else {
                                    Minutes m = Minutes.minutesBetween(new LocalTime(now.getHourOfDay(), now.getMinuteOfHour()), new LocalTime(dep.getScheduledDepartureTime().getHourOfDay(), dep.getScheduledDepartureTime().getMinuteOfHour()));
                                    if(m.getMinutes() == 0) {
                                        listen += mContext.getString(R.string.realtime_linje) + dep.getLine() + mContext.getString(R.string.realtime_mot) + dep.getDestination() + "\n";
                                        listen += "ca " + mContext.getString(R.string.realtime_NOW);
                                    }
                                    else if(m.getMinutes() > 59) {
                                        Hours h = m.toStandardHours();
                                        int minus = h.getHours() * 60;
                                        listen += mContext.getString(R.string.realtime_linje) + dep.getLine() + mContext.getString(R.string.realtime_mot) + dep.getDestination() + "\n";
                                        listen += "ca " + Integer.toString(h.getHours()) + mContext.getString(R.string.realtime_hour) + " " + Integer.toString(m.getMinutes() - minus) + "min";
                                    }
                                    else {
                                        listen += mContext.getString(R.string.realtime_linje) + dep.getLine() + mContext.getString(R.string.realtime_mot) + dep.getDestination() + "\n";
                                        listen += "ca " + Integer.toString(m.getMinutes()) + "min";
                                    }
                                }
                            }
                        }
                        else
                            listen = mContext.getString(R.string.realtime_ingen_info);
                    }
                    else
                        listen = mContext.getString(R.string.realtime_ingen_info);
                }

                realtime.setMessage(listen);

                realtime.setPositiveButton(mContext.getText(R.string.dialog_orakel_okbutton).toString(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //TODO: kill timer og oppdateringer
                    }
                });

               /* realtime.setNeutralButton(mContext.getText(R.string.realtime_snu_retning).toString(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: 0/1 id shift?
                        directionChange = true;
                    }
                });           */
                realtime.show();
            }
        });

        dialog.show();
        return true;
    }

    class loadRealtimeInfoThread extends AsyncTask<Integer, Void, Void> {
        @Override
        protected void onPreExecute() {
            realtimeDone = false;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                departures = GoogleMaps.realtimeController.getBusStopForecasts(params[0]);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            realtimeDone = true;
        }
    }
}