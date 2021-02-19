package com.noolitef.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import android.widget.Toast
import com.google.gson.Gson
import com.noolitef.NooLiteF
import com.noolitef.R
import com.noolitef.widgets.SettingsUnit
import com.noolitef.widgets.WidgetPreset
import com.noolitef.widgets.WidgetPresetConfigureActivity
import com.noolitef.widgets.WidgetPresetUnit
import okhttp3.*
import java.io.IOException


class PresetService : Service() {

    private lateinit var context: Context
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var settingsUnit: SettingsUnit
    private lateinit var client: OkHttpClient

    override fun onCreate() {

        super.onCreate()

        //startForeground("")

        context = this@PresetService
        appWidgetManager = AppWidgetManager.getInstance(context)

        settingsUnit = Gson().fromJson(WidgetPresetConfigureActivity.loadSettingsJson(context), SettingsUnit::class.java)

        client = OkHttpClient().newBuilder()
                //.connectTimeout(Settings.connectTimeout().toLong(), TimeUnit.MILLISECONDS)
                //.writeTimeout(Settings.connectTimeout().toLong(), TimeUnit.MILLISECONDS)
                //.readTimeout(Settings.connectTimeout().toLong(), TimeUnit.MILLISECONDS)
                .authenticator(Authenticator { _, response ->
                    if (response.request().header("Authorization") != null) {
                        showToast("Необходима авторизация в приложении")
                        return@Authenticator null
                    }
                    val credential = Credentials.basic(settingsUnit.login, settingsUnit.password)
                    return@Authenticator response.request().newBuilder()
                            .header("Authorization", credential)
                            .build()
                })
                .build()
    }

    override fun onBind(intent: Intent): IBinder? {

        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Thread(Runnable {
            val widgetID = intent.getIntExtra("widgetID", AppWidgetManager.INVALID_APPWIDGET_ID)
            val widgetPresetUnit = Gson().fromJson(intent.getStringExtra("widgetPresetJson"), WidgetPresetUnit::class.java)

            startForeground(widgetPresetUnit.name)

            Handler(Looper.getMainLooper()).post {
                WidgetPreset.updateWidgetPreset(context, appWidgetManager, widgetID, activated = false, updating = true)
            }

            val request = Request.Builder()
                    .url("${settingsUnit.uri}send.htm?sd=010A${NooLiteF.getHexString(widgetPresetUnit.index)}000000000000000000000000")
                    .post(RequestBody.create(null, ""))
                    .build()

            //Log.d("_PresetService", "\nsettingsUnit.uri: ${settingsUnit.uri}\nsettingsUnit.login: ${settingsUnit.login}\nsettingsUnit.password: ${settingsUnit.password}\nwidgetPresetUnit.index: ${NooLiteF.getHexString(widgetPresetUnit.index)}")

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    call.cancel()
                    showToast(context.getString(R.string.no_connection))
                    Handler(Looper.getMainLooper()).post {
                        WidgetPreset.updateWidgetPreset(context, appWidgetManager, widgetID)
                        stopSelf()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        call.cancel()
                        if (response.isSuccessful) {
                            Handler(Looper.getMainLooper()).post {
                                WidgetPreset.updateWidgetPreset(context, appWidgetManager, widgetID, activated = true, updating = false)
                            }
                            showToast("Сценарий ''${widgetPresetUnit.name}'' запущен")
                            Thread.sleep(widgetPresetUnit.runTime)
                            Handler(Looper.getMainLooper()).post {
                                WidgetPreset.updateWidgetPreset(context, appWidgetManager, widgetID)
                                stopSelf()
                            }
                        } else {
                            showToast(context.getString(R.string.connection_error).plus(response.code()))
                            Handler(Looper.getMainLooper()).post {
                                WidgetPreset.updateWidgetPreset(context, appWidgetManager, widgetID)
                                stopSelf()
                            }
                        }
                    } catch (e: Exception) {
                        call.cancel()
                        showToast(context.getString(R.string.some_thing_went_wrong))
                        Handler(Looper.getMainLooper()).post {
                            WidgetPreset.updateWidgetPreset(context, appWidgetManager, widgetID)
                            stopSelf()
                        }
                    }
                }
            })
        }).start()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground(presetName: String) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            val channelId = "nooLiteHomeControl"
            val channelName = "nooLite Home Control"

            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(notificationChannel)
            val notification: Notification = NotificationCompat.Builder(this@PresetService, channelId)
                    .setContentTitle("Выполняется сценарий")
                    .setContentText(presetName)
                    .build()

            Handler(Looper.getMainLooper()).post {
                startForeground(1, notification)
            }
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}