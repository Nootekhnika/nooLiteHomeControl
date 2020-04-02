package com.noolitef.ftx

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.noolitef.*
import com.noolitef.settings.Settings
import okhttp3.*
import java.lang.StringBuilder
import java.net.ConnectException
import java.util.*
import java.util.concurrent.TimeoutException

class ThermostatSensorSelectorFragment : DialogFragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private lateinit var homeActivity: HomeActivity
    private lateinit var client: OkHttpClient
    private lateinit var thermostat: Thermostat

    private lateinit var thermostatSettings: StringBuilder
    private var previousExternalSensorBit = -1
    private var previousSensorBit = -1
    private var externalSensorBit = -1
    private var sensorBit = -1
    private var modeBit = -1

    private lateinit var buttonBack: Button
    private lateinit var buttonSave: Button
    private lateinit var textSensorSelectionNotAvailable: TextView
    private lateinit var groupSensors: RadioGroup
    private lateinit var radioAirSensor: RadioButton
    private lateinit var radioFloorSensor: RadioButton
    private lateinit var radioExternalSensor: RadioButton

    private var guiBlockFragment: GUIBlockFragment? = null

    fun send(client: OkHttpClient, thermostat: Thermostat) {
        this.client = client
        this.thermostat = thermostat
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        isCancelable = true
        retainInstance = true

        homeActivity = activity as HomeActivity
        thermostatSettings = StringBuilder("00000000")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.setCanceledOnTouchOutside(true)

        val dialogView = if (Settings.isNightMode()) {
            inflater.inflate(R.layout.fragment_settings_thermostat_sensors_dark, null)
        } else {
            inflater.inflate(R.layout.fragment_settings_thermostat_sensors, null)
        }
        buttonBack = dialogView.findViewById(R.id.fragment_settings_thermostat_sensors_button_back)
        buttonBack.setOnClickListener(this)
        buttonSave = dialogView.findViewById(R.id.fragment_settings_thermostat_sensors_button_save)
        buttonSave.setOnClickListener(this)
        textSensorSelectionNotAvailable = dialogView.findViewById(R.id.fragment_settings_thermostat_sensors_text_not_available)
        groupSensors = dialogView.findViewById(R.id.fragment_settings_thermostat_sensors_group)
        radioAirSensor = dialogView.findViewById(R.id.fragment_settings_thermostat_sensor_air)
        radioFloorSensor = dialogView.findViewById(R.id.fragment_settings_thermostat_sensor_floor)
        radioExternalSensor = dialogView.findViewById(R.id.fragment_settings_thermostat_sensor_external)
        groupSensors.setOnCheckedChangeListener(this)

        getSelectedSensor()

        return dialogView
    }

    override fun onStart() {
        super.onStart()

        val fragmentWindow = dialog.window
        val dialogParams = fragmentWindow.attributes
        dialogParams.dimAmount = 0.75f
        fragmentWindow.attributes = dialogParams
        fragmentWindow.setBackgroundDrawableResource(R.color.transparent)

        val display = DisplayMetrics()
        homeActivity.windowManager.defaultDisplay.getMetrics(display)
        val displayWidth = display.widthPixels
        val displayHeight = display.heightPixels
        if (displayWidth < displayHeight) {
            fragmentWindow.setLayout(displayWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        } else {
            fragmentWindow.setLayout(displayHeight, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onDestroyView() {
        val dialog = dialog
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }

        super.onDestroyView()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fragment_settings_thermostat_sensors_button_back -> dismiss()
            R.id.fragment_settings_thermostat_sensors_button_save -> selectSensor()
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            -1 -> {
                externalSensorBit = -1
                sensorBit = -1
            }
            R.id.fragment_settings_thermostat_sensor_floor -> {
                externalSensorBit = 0
                sensorBit = 0
            }
            R.id.fragment_settings_thermostat_sensor_air -> {
                externalSensorBit = 0
                sensorBit = 1
            }
            R.id.fragment_settings_thermostat_sensor_external -> externalSensorBit = 1
        }
    }

    private fun getSelectedSensor() {
        Thread(Runnable {
            var request: Request
            var call: Call? = null
            var response: Response
            var hex: String
            try {
                request = Request.Builder()
                        .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", thermostat.id))
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    call.cancel()
                    Thread.sleep(100)
                    request = Request.Builder()
                            .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801000000000%s", thermostat.id))
                            .build()
                    call = client.newCall(request)
                    response = call.execute()
                    if (response.isSuccessful) {
                        call.cancel()
                        Thread.sleep(100)
                        request = Request.Builder()
                                .url(Settings.URL() + "rxset.htm")
                                .build()
                        call = client.newCall(request)
                        response = call.execute()
                        if (response.isSuccessful) {
                            hex = response.body()!!.string()
                            call.cancel()
                            if (hex.substring(22, 30) == thermostat.id) {
                                thermostatSettings = StringBuilder(String.format("%8s", Integer.toBinaryString(Integer.parseInt(hex.substring(14, 16), 16))).replace(' ', '0'))
                                modeBit = thermostatSettings.substring(7).toInt()
                                if (modeBit == 1) {
                                    externalSensorBit = thermostatSettings.substring(6, 7).toInt()
                                    previousExternalSensorBit = externalSensorBit
                                    sensorBit = thermostatSettings.substring(4, 5).toInt()
                                    previousSensorBit = sensorBit
                                    setSelection(externalSensorBit, sensorBit)
                                    return@Runnable
                                } else {
                                    clearSelection()
                                    return@Runnable
                                }
                            } else {
                                call.cancel()
                                showNotAvailable()
                                showToast("Силовой блок не отвечает")
                                return@Runnable
                            }
                        }
                    }
                }
                call.cancel()
                showNotAvailable()
                showToast(homeActivity.getString(R.string.connection_error).plus(" ").plus(response.code()))
            } catch (e: ConnectException) {
                call?.cancel()
                showToast(homeActivity.getString(R.string.no_connection))
            } catch (e: TimeoutException) {
                call?.cancel()
                showToast(homeActivity.getString(R.string.no_connection))
            } catch (e: Exception) {
                call?.cancel()
                showToast(homeActivity.getString(R.string.some_thing_went_wrong))
            }
        }).start()
    }

    private fun selectSensor() {
        if ((externalSensorBit == -1) || (sensorBit == -1)) {
            showToast("Выберите датчик")
            return
        }
        blockUI()
        Thread(Runnable {
            if ((externalSensorBit != previousExternalSensorBit) || (sensorBit != previousSensorBit)) {
                if (setSensor()) {
                    showToast("Настройки сохранены")
                } else {
                    return@Runnable
                }
            }
            dismiss()
        }).start()
    }

    private fun setSensor(): Boolean {
        var request: Request
        var call: Call? = null
        var response: Response
        var hex: String
        try {
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", thermostat.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(100)
                thermostatSettings.replace(4, 5, sensorBit.toString())
                thermostatSettings.replace(6, 7, externalSensorBit.toString())
                thermostatSettings.replace(7, 8, "1")  // MODE: TEMPERATURE
                request = Request.Builder()
                        .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008110%s007F00%s", NooLiteF.getHexString(thermostatSettings.toString().toInt(2)), thermostat.id))
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    call.cancel()
                    Thread.sleep(100)
                    request = Request.Builder()
                            .url(Settings.URL() + "rxset.htm")
                            .build()
                    call = client.newCall(request)
                    response = call.execute()
                    if (response.isSuccessful) {
                        hex = response.body()!!.string()
                        call.cancel()
                        if (hex.substring(22, 30) == thermostat.id) {
                            if (String.format("%8s", Integer.toBinaryString(hex.substring(14, 16).toInt(16))).replace(' ', '0') != thermostatSettings.toString()) {
                                showToast("Настройка датчика не сохранена")
                                return false
                            }
                            return true
                        } else {
                            call.cancel()
                            showToast("Силовой блок не отвечает")
                            return false
                        }
                    }
                }
            }
            call.cancel()
            showToast(homeActivity.getString(R.string.connection_error).plus(" ").plus(response.code()))
            return false
        } catch (e: ConnectException) {
            call?.cancel()
            showToast(homeActivity.getString(R.string.no_connection))
            return false
        } catch (e: TimeoutException) {
            call?.cancel()
            showToast(homeActivity.getString(R.string.no_connection))
            return false
        } catch (e: Exception) {
            call?.cancel()
            showToast(homeActivity.getString(R.string.some_thing_went_wrong))
            return false
        }
    }

    private fun blockUI() {
        if (!isAdded) return
        guiBlockFragment = childFragmentManager.findFragmentByTag("GUI_BLOCK_FRAGMENT") as GUIBlockFragment?
                ?: GUIBlockFragment()
        if (guiBlockFragment!!.isAdded) return
        childFragmentManager.beginTransaction().add(guiBlockFragment!!, "GUI_BLOCK_FRAGMENT").show(guiBlockFragment!!).commit()
    }

    private fun unblockUI() {
        if (!isAdded) return
        guiBlockFragment?.dismiss()
    }

    private fun setSelection(externalSensorBit: Int, sensorBit: Int) {
        homeActivity.runOnUiThread {
            groupSensors.visibility = View.VISIBLE
            when (sensorBit) {
                -1 -> groupSensors.clearCheck()
                0 -> {
                    radioFloorSensor.isChecked = true
                }
                1 -> {
                    radioAirSensor.isChecked = true
                }
            }
            if (externalSensorBit == 1) {
                radioExternalSensor.isChecked = true
            }
        }
    }

    private fun clearSelection() {
        homeActivity.runOnUiThread {
            groupSensors.clearCheck()
        }
    }

    private fun showNotAvailable() {
        homeActivity.runOnUiThread {
            textSensorSelectionNotAvailable.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            unblockUI()
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
