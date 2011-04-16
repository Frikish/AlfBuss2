package com.alfsimen.bybuss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.widget.EditText;
import com.google.android.maps.GeoPoint;
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
    protected boolean onTap(int index) {
        final OverlayItem item = mOverlays.get(index);

             AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
             dialog.setTitle(item.getTitle());
             if(fra == null) {
                dialog.setMessage("Reise fra " + item.getTitle());
             }
             else {
                dialog.setMessage("Reise til " + item.getTitle());
             }

            dialog.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                //@Override
                public void onClick(DialogInterface dialog, int which) {
                    if(fra == null) {
                        fra = item.getTitle();
                        searchBar.setText(item.getTitle() + " til ");
                        searchBar.setSelection(searchBar.getText().toString().length());
                        //Drawable draw = mContext.getResources().getDrawable(R.drawable.gps_marker_red);
                        //OverlayItem temp = new OverlayItem(item.getPoint(), item.getTitle(), "Punktet du reiser ifra");
                        //temp.setMarker(draw);
                        //mOverlays.remove(item);
                        //mOverlays.add(temp);
                    }
                    else {
                        til = item.getTitle();
                        searchBar.setText(fra + " til " + til);
                        searchBar.setSelection(searchBar.getText().toString().length());
                        fra = til = null;
                        //TODO: fix nullsetting of red overlay?
                    }
                    return;
                }
            });

            dialog.setNegativeButton("Nei", new DialogInterface.OnClickListener() {
                //@Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });

          dialog.show();
          return true;
    }
}