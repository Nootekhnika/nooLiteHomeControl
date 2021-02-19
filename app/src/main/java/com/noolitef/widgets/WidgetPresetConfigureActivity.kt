package com.noolitef.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EdgeEffect
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.noolitef.HomeActivity
import com.noolitef.PRF64
import com.noolitef.R
import com.noolitef.presets.Preset
import com.noolitef.settings.Settings

class WidgetPresetConfigureActivity : Activity(), View.OnClickListener {
    companion object {
        private const val PREFERENCES_NAME = "nooLiteWidgets"
        private const val PREFERENCE_PREFIX = "widget_preset_"

        internal fun loadSettingsJson(context: Context): String {
            val preferences = context.getSharedPreferences("nooLite", Context.MODE_PRIVATE)
            Settings.setIP(preferences.getString("URL", "192.168.0.170"))
            Settings.setDNS(preferences.getString("DNS", "noolite.nootech.dns.by:80"))
            Settings.useDNS(preferences.getBoolean("useDNS", false))
            Settings.setLogin(preferences.getString("Login", "admin"))
            Settings.setPassword(preferences.getString("Password", "admin"))
            return Gson().toJson(SettingsUnit(Settings.URL(), Settings.login(), Settings.password()))
        }

        internal fun saveWidgetPresetJson(context: Context, widgetID: Int, presetIndex: Int, presetName: String, presetRunTime: Long) {
            val widgetPresetUnit = Gson().toJson(WidgetPresetUnit(presetIndex, presetName, presetRunTime))
            val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            preferences.putString(PREFERENCE_PREFIX + widgetID, widgetPresetUnit)
            preferences.apply()
        }

        internal fun loadWidgetPresetJson(context: Context, widgetID: Int): String {
            val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            return preferences.getString(PREFERENCE_PREFIX + widgetID, "{\"index\":32,\"name\":\"Сценарий\",\"runTime\":1000}")!!
        }

        internal fun deleteWidgetPresetJson(context: Context, widgetID: Int) {
            val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            preferences.remove(PREFERENCE_PREFIX + widgetID)
            preferences.apply()
        }
    }

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var nooLitePRF64: PRF64
    private lateinit var presets: ArrayList<Preset>

    private lateinit var buttonCancel: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var presetListAdapter: PresetListAdapter

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        val extras = intent.extras
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        nooLitePRF64 = PRF64(this@WidgetPresetConfigureActivity)
        presets = nooLitePRF64.getPresets()

        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.widget_configure_activity_preset)

        buttonCancel = findViewById(R.id.widget_configure_activity_button_cancel)
        buttonCancel.setOnClickListener(this)
        recyclerView = findViewById(R.id.widget_configure_activity_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this@WidgetPresetConfigureActivity, RecyclerView.VERTICAL, false)
        presetListAdapter = PresetListAdapter(this@WidgetPresetConfigureActivity, presets)
        presetListAdapter.setHasStableIds(true)
        recyclerView.adapter = presetListAdapter

        // custom over scroll color for API >= 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                    return EdgeEffect(view.context).apply { color = Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(this@WidgetPresetConfigureActivity, R.color.grey_light_alpha))) }
                }
            }
        } else {
            recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.widget_configure_activity_button_cancel -> finish()
        }
    }

    internal fun runHomeActivity() {
        startActivity(Intent(this@WidgetPresetConfigureActivity, HomeActivity::class.java))
        finish()
    }

    internal fun setupWidgetPreset(presetIndex: Int, presetName: String, presetRunTime: Long) {
        val context = this@WidgetPresetConfigureActivity

        saveWidgetPresetJson(context, widgetID, presetIndex, presetName, presetRunTime)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        WidgetPreset.updateWidgetPreset(context, appWidgetManager, widgetID)

        val intent = Intent()
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}