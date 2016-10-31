/*
 * This file is a part of Budget with Envelopes.
 * Copyright 2013 Michael Howell <michael@notriddle.com>
 *
 * Budget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Budget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Budget. If not, see <http://www.gnu.org/licenses/>.
 */

package com.srima.bb;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context cntx, AppWidgetManager manager,
                                   int[] widgetIds) {
        final int l = widgetIds.length;
        for (int i = 0; i != l; ++i) {
            int widgetId = widgetIds[i];
            Log.d("Budget", "WidgetProvider.id="+widgetId);
            RemoteViews views = new RemoteViews(
                cntx.getPackageName(),
                R.layout.widget
            );
            Intent srv = new Intent(cntx, WidgetService.class);
            srv.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetIds[i]);
            srv.setData(Uri.parse(srv.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(widgetIds[i], R.id.grid, srv);
            views.setEmptyView(R.id.grid, R.id.empty);
            Intent act = new Intent(cntx, EnvelopesActivity.class);
            act.setData(Uri.parse("fragment://"+EnvelopeDetailsFragment.class.getName()+"/"+widgetIds[i]));
            act.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetIds[i]);
            PendingIntent actPending = PendingIntent.getActivity(
                cntx, 0, act, PendingIntent.FLAG_UPDATE_CURRENT
            );
            views.setPendingIntentTemplate(R.id.grid, actPending);
            manager.updateAppWidget(widgetIds[i], views);
        }
        super.onUpdate(cntx, manager, widgetIds);
    }
}

