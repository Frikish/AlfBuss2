package com.alfsimen.bybuss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: alf
 * Date: 4/9/11
 * Time: 2:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapsOverlay extends ItemizedOverlay {
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private Context mContext;

    private String fra = null;
    private String til = null;
    private EditText searchBar;
    private Button searchButton;

    public MapsOverlay(Drawable defaultMarker) {
        super(boundCenter(defaultMarker));
    }

    public MapsOverlay(Drawable defaultMarker, Context context) {
        super(boundCenter(defaultMarker));
        mContext = context;
    }

    public MapsOverlay(Drawable defaultMarker, Context context, EditText searchbar) {
        super(boundCenter(defaultMarker));
        mContext = context;
        searchBar = searchbar;
    }

    public MapsOverlay(Drawable defaultMarker, Context context, EditText searchbar, Button searchbutton) {
        super(boundCenter(defaultMarker));
        mContext = context;
        searchBar = searchbar;
        searchButton = searchbutton;
    }

    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

    public void setMarker(OverlayItem overlay, Drawable marker) {
        overlay.setMarker(boundCenter(marker));
        populate();
    }

    public void myPopulate() {

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
             if(item.getTitle() == "ukjent") {
                 dialog.setMessage(mContext.getText(R.string.ukjent_busstop_name).toString());
             }
             else
                dialog.setMessage(mContext.getText(R.string.reise_fra).toString() + " " + item.getTitle());
         }
         else {
             if(item.getTitle() == "ukjent") {
                dialog.setMessage(mContext.getText(R.string.ukjent_busstop_name).toString());
             }
             else
                 dialog.setMessage(mContext.getText(R.string.reise_til).toString() + " " + item.getTitle());
         }

        if(item.getTitle() != "ukjent") {
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
                        populate();
                    }
                    else {
                        til = item.getTitle();
                        searchBar.setText(fra + mContext.getText(R.string.search_separator).toString() + til);
                        searchBar.setSelection(searchBar.getText().toString().length());
                        Drawable to = mContext.getResources().getDrawable(R.drawable.gps_marker_red);
                        GoogleMaps.toItem = item;
                        setMarker(item, to);
                        populate();
                        searchButton.performClick();
                        fra = til = null;
                    }
                    return;
                }
            });

            dialog.setNegativeButton(mContext.getText(R.string.nei).toString(), new DialogInterface.OnClickListener() {
                //@Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
        }
         else {
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
        }

        dialog.show();
        return true;
    }
}