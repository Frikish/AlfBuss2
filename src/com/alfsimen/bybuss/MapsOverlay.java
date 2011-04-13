package com.alfsimen.bybuss;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
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

    public MapsOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
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

    public MapsOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
    }

    @Override
    protected boolean onTap(int index) {
    /*  OverlayItem item = mOverlays.get(index);
      AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
      dialog.setTitle(item.getTitle());
      dialog.setMessage("lol");
      dialog.show(); */
      return true;
    }
}