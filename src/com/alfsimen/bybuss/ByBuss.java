package com.alfsimen.bybuss;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class ByBuss extends Activity
{
    private GoogleMaps googleMaps;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestCustomTitleBar();
        setContentView(R.layout.main);
        setCustomTitle(getString(R.string.custom_title));
    }

    protected void requestCustomTitleBar() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    }

    protected void setCustomTitle(String title) {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_titlebar);
        TextView tv = (TextView) getWindow().findViewById(R.id.headerTitleTextVw);
        tv.setText(title);
    }
}
