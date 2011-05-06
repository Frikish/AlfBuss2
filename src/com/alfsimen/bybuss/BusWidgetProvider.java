package com.alfsimen.bybuss;

import android.app.PendingIntent;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.RemoteViews;

/**
 * Created by IntelliJ IDEA.
 * User: alf
 * Date: 5/3/11
 * Time: 7:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class BusWidgetProvider extends AppWidgetProvider{

    private SharedPreferences prefs;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        for(int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, BusWidget.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.bus_widget_ui);

            String answer = prefs.getString(context.getString(R.string.prefs_last_answer).toString(), "");
            answer.replaceAll("\\s+", " ");
            //answer.replaceAll("\\r\\n", " ");

            remoteView.setTextViewText(R.id.widgetTextView, answer);
            //remoteView.setOnClickPendingIntent(R.id.widgetRefreshButton, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteView);
        }
    }
}
