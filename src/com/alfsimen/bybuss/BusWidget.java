package com.alfsimen.bybuss;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * Created by IntelliJ IDEA.
 * User: alf
 * Date: 5/3/11
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class BusWidget extends Activity {

    private AtbBussorakel bussen;

    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);

        Intent i = getIntent();

        bussen = new AtbBussorakel();
        bussen.setQuestion(i.getStringExtra(SearchManager.QUERY).toString());
        new AtbThreadTest(getApplicationContext()).execute();
    }

    class AtbThreadTest extends AsyncTask<Void, Void, Void>
    {
        private Context context;

        public AtbThreadTest(Context context)
        {
            this.context = context;
        }

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
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(getString(R.string.dialog_orakel_title));
            dialog.setPositiveButton(getString(R.string.dialog_orakel_okbutton).toString(), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });

            if (bussen.getAnswer().trim().equals("No question supplied"))
            {
                dialog.setMessage(getString(R.string.help_string));
            }
            else
            {
                dialog.setMessage(bussen.getAnswer());
            }

            dialog.show();
        }
    }
}
