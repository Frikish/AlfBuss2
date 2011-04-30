package com.alfsimen.bybuss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.EditText;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
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

    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
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
    protected boolean onTap(int index) {  //TODO: fix translation here!!!
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
                    if(fra == null) {
                        fra = item.getTitle();
                        searchBar.setText(item.getTitle() + mContext.getText(R.string.search_separator).toString());
                        searchBar.setSelection(searchBar.getText().toString().length());
                        //Drawable draw = mContext.getResources().getDrawable(R.drawable.gps_marker_red);
                        //OverlayItem temp = new OverlayItem(item.getPoint(), item.getTitle(), "Punktet du reiser ifra");
                        //temp.setMarker(draw);
                        //mOverlays.remove(item);
                        //mOverlays.add(temp);
                    }
                    else {
                        til = item.getTitle();
                        searchBar.setText(fra + mContext.getText(R.string.search_separator).toString() + til);
                        searchBar.setSelection(searchBar.getText().toString().length());
                        fra = til = null;
                        //TODO: fix nullsetting of red overlay?
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