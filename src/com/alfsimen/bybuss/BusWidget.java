package com.alfsimen.bybuss;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Created by IntelliJ IDEA.
 * User: alf
 * Date: 5/3/11
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class BusWidget extends Activity{

    private AtbBussorakel bussen;
    private SharedPreferences prefs;
    public void onCreate(Bundle lol) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        bussen = new AtbBussorakel();
        bussen.setQuestion(prefs.getString(getString(R.string.prefs_last_search), ""));
        new AtbThreadTest().execute();

        finish();
    }

    class AtbThreadTest extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            //TODO: Show waiting-spinner-thingy
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

            }
            else
            {

            }

        }
    }
}
