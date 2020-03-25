package com.noolitef.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.google.gson.Gson
import com.noolitef.R
import com.noolitef.services.PresetService

class WidgetPreset : AppWidgetProvider() {
    companion object {
        internal fun updateWidgetPreset(context: Context, appWidgetManager: AppWidgetManager, widgetID: Int) {
            val widgetPresetJson = WidgetPresetConfigureActivity.loadWidgetPresetJson(context, widgetID)

            val intent = Intent(context, PresetService::class.java)
            intent.action = System.nanoTime().toString()
            intent.putExtra("widgetID", widgetID)
            intent.putExtra("widgetPresetJson", widgetPresetJson)
            var pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                pendingIntent = PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            }

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout_preset)
            remoteViews.setInt(R.id.widget_preset_layout_clickable, "setBackgroundResource", R.drawable.widget_preset_background)
            remoteViews.setTextViewText(R.id.widget_preset_text_name, Gson().fromJson(widgetPresetJson, WidgetPresetUnit::class.java).name)
            remoteViews.setOnClickPendingIntent(R.id.widget_preset_layout_clickable, pendingIntent)
            remoteViews.setViewVisibility(R.id.widget_preset_progress_bar, View.GONE)

            appWidgetManager.updateAppWidget(widgetID, remoteViews)
        }

        internal fun updateWidgetPreset(context: Context, appWidgetManager: AppWidgetManager, widgetID: Int, activated: Boolean, updating: Boolean) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout_preset)
            if (activated) {
                remoteViews.setInt(R.id.widget_preset_layout_clickable, "setBackgroundResource", R.drawable.widget_background_activated)
            } else {
                remoteViews.setInt(R.id.widget_preset_layout_clickable, "setBackgroundResource", R.drawable.widget_preset_background)
            }
            if (updating) {
                remoteViews.setViewVisibility(R.id.widget_preset_progress_bar, View.VISIBLE)
            } else {
                remoteViews.setViewVisibility(R.id.widget_preset_progress_bar, View.GONE)
            }

            appWidgetManager.updateAppWidget(widgetID, remoteViews)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetID in appWidgetIds) {
            updateWidgetPreset(context, appWidgetManager, widgetID)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (widgetID in appWidgetIds) {
            WidgetPresetConfigureActivity.deleteWidgetPresetJson(context, widgetID)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}