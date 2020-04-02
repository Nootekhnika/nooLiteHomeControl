package com.noolitef.automatics

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.noolitef.PRF64
import com.noolitef.R
import com.noolitef.Room
import com.noolitef.Thermostat
import com.noolitef.ftx.PowerSocketF
import com.noolitef.ftx.PowerUnitF
import com.noolitef.presets.Preset
import com.noolitef.rx.*
import com.noolitef.settings.Settings
import com.noolitef.tx.PowerUnit

class AutomationUnitListAdapter(
        nooLitePRF64: PRF64,
        private val showTriggerList: Boolean,
        private val showPresets: Boolean,
        private val showPowerUnits: Boolean,
        private val iAutomationUnit: IAutomationUnit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ROOM = 0
        private const val UNIT = 1
        private const val INFO = Int.MAX_VALUE
    }

    private val automationUnitList: ArrayList<Any> = ArrayList()

    init {
        setHasStableIds(true)
        update(nooLitePRF64, automationUnitList)
    }


    override fun getItemId(position: Int): Long {

        return position.toLong()
    }

    override fun getItemCount(): Int {

        return automationUnitList.size
    }

    override fun getItemViewType(position: Int): Int {

        return when (automationUnitList[position]) {
            is Room -> ROOM
            is Info -> INFO
            else -> UNIT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        if (Settings.isNightMode()) {
            return when (viewType) {
                ROOM -> AutomationRoomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_view_room, parent, false))
                INFO -> AutomationInfoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_view_automation_info_dark, parent, false))
                else -> AutomationUnitViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_view_automation_unit, parent, false))
            }
        } else {
            return when (viewType) {
                ROOM -> AutomationRoomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_view_room, parent, false))
                INFO -> AutomationInfoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_view_automation_info, parent, false))
                else -> AutomationUnitViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_view_automation_unit, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            ROOM -> bindRoom(holder, position)
            INFO -> bindInfo(holder)
            else -> bindUnit(holder, position)
        }
    }


    private fun update(nooLitePRF64: PRF64, automationUnitList: ArrayList<Any>) {

        Thread(Runnable {
            val rooms: List<Room> = nooLitePRF64.getHome().rooms
            val presets: List<Preset> = nooLitePRF64.getPresets()
            // Не все устройства, работающие по протоколу nooLite TX, выполняют команды автоматики
            val powerUnits: ArrayList<PowerUnit> =
                    if (showPowerUnits) {
                        ArrayList(nooLitePRF64.getTXUnits())
                    } else {
                        ArrayList()
                    }
            var powerUnitCount: Int = powerUnits.size
            val remoteControllers: ArrayList<RemoteController> = ArrayList(nooLitePRF64.getRemoteControllers())
            var remoteControllerCount: Int = remoteControllers.size
            val temperatureSensors: ArrayList<TemperatureSensor> = ArrayList(nooLitePRF64.getTemperatureSensors())
            var temperatureSensorCount: Int = temperatureSensors.size
            val humidityTemperatureSensors: ArrayList<HumidityTemperatureSensor> = ArrayList(nooLitePRF64.getHumidityTemperatureSensors())
            var humidityTemperatureSensorCount: Int = humidityTemperatureSensors.size
            val motionSensors: ArrayList<MotionSensor> = ArrayList(nooLitePRF64.getMotionSensors())
            var motionSensorCount: Int = motionSensors.size
            val lightSensors: ArrayList<LightSensor> = ArrayList(nooLitePRF64.getLightSensors())
            var lightSensorCount: Int = lightSensors.size
            val openCloseSensors: ArrayList<OpenCloseSensor> = ArrayList(nooLitePRF64.getOpenCloseSensors())
            var openCloseSensorCount: Int = openCloseSensors.size
            val leakDetectors: ArrayList<LeakDetector> = ArrayList(nooLitePRF64.getLeakDetectors())
            var leakDetectorCount: Int = leakDetectors.size
            val powerUnitsF: ArrayList<PowerUnitF> = ArrayList(nooLitePRF64.getPowerUnitsF())
            var powerUnitFCount: Int = powerUnitsF.size
            val powerSocketsF: ArrayList<PowerSocketF> = ArrayList(nooLitePRF64.getPowerSocketsF())
            var powerSocketFCount: Int = powerSocketsF.size
            // Термостаты как и сценарии корректно работают только в событийном типе автоматики
            val thermostats: ArrayList<Thermostat> =
                    if (showPresets) {
                        ArrayList(nooLitePRF64.getThermostats())
                    } else {
                        ArrayList()
                    }
            var thermostatsCount: Int = thermostats.size

            automationUnitList.clear()
            if (showPresets) {
                automationUnitList.add(Room(-0, "Сценарии"))
                automationUnitList.addAll(presets)
                if (automationUnitList[automationUnitList.size - 1] is Room) {
                    automationUnitList.remove(automationUnitList.size - 1)
                }
            }
            for (room in rooms) {
                automationUnitList.add(room)
                if (showTriggerList) {
                    for (i in remoteControllers.indices) {
                        if (i >= remoteControllers.size) break
                        if (remoteControllers[i].roomID == room.id) {
                            automationUnitList.add(remoteControllers[i])
                            remoteControllers.removeAt(i)
                            remoteControllerCount--
                        }
                    }
                    for (i in temperatureSensors.indices) {
                        if (i >= temperatureSensors.size) break
                        if (temperatureSensors[i].roomID == room.id) {
                            automationUnitList.add(temperatureSensors[i])
                            temperatureSensors.removeAt(i)
                            temperatureSensorCount--
                        }
                    }
                    for (i in humidityTemperatureSensors.indices) {
                        if (i >= humidityTemperatureSensors.size) break
                        if (humidityTemperatureSensors[i].roomID == room.id) {
                            automationUnitList.add(humidityTemperatureSensors[i])
                            humidityTemperatureSensors.removeAt(i)
                            humidityTemperatureSensorCount--
                        }
                    }
                    for (i in motionSensors.indices) {
                        if (i >= motionSensors.size) break
                        if (motionSensors[i].roomID == room.id) {
                            automationUnitList.add(motionSensors[i])
                            motionSensors.removeAt(i)
                            motionSensorCount--
                        }
                    }
                    for (i in lightSensors.indices) {
                        if (i >= lightSensors.size) break
                        if (lightSensors[i].roomID == room.id) {
                            automationUnitList.add(lightSensors[i])
                            lightSensors.removeAt(i)
                            lightSensorCount--
                        }
                    }
                    for (i in openCloseSensors.indices) {
                        if (i >= openCloseSensors.size) break
                        if (openCloseSensors[i].roomID == room.id) {
                            automationUnitList.add(openCloseSensors[i])
                            openCloseSensors.removeAt(i)
                            openCloseSensorCount--
                        }
                    }
                    for (i in leakDetectors.indices) {
                        if (i >= leakDetectors.size) break
                        if (leakDetectors[i].roomID == room.id) {
                            automationUnitList.add(leakDetectors[i])
                            leakDetectors.removeAt(i)
                            leakDetectorCount--
                        }
                    }
                } else {
                    for (i in powerUnits.indices) {
                        if (i >= powerUnits.size) break
                        if (powerUnits[i].roomID == room.id) {
                            when (powerUnits[i].type) {
                                PowerUnit.RELAY, PowerUnit.DIMMER, PowerUnit.RGB_CONTROLLER
                                -> automationUnitList.add(powerUnits[i])
                            }
                            powerUnits.removeAt(i)
                            powerUnitCount--
                        }
                    }
                    for (i in powerUnitsF.indices) {
                        if (i >= powerUnitsF.size) break
                        if (powerUnitsF[i].roomID == room.id) {
                            automationUnitList.add(powerUnitsF[i])
                            powerUnitsF.removeAt(i)
                            powerUnitFCount--
                        }
                    }
                    for (i in powerSocketsF.indices) {
                        if (i >= powerSocketsF.size) break
                        if (powerSocketsF[i].roomID == room.id) {
                            automationUnitList.add(powerSocketsF[i])
                            powerSocketsF.removeAt(i)
                            powerSocketFCount--
                        }
                    }
                    for (i in thermostats.indices) {
                        if (i >= thermostats.size) break
                        if (thermostats[i].roomID == room.id) {
                            automationUnitList.add(thermostats[i])
                            thermostats.removeAt(i)
                            thermostatsCount--
                        }
                    }
                }
                if (automationUnitList[automationUnitList.size - 1] is Room) {
                    automationUnitList.removeAt(automationUnitList.size - 1)
                }
            }
            automationUnitList.add(Room(-1, "Нераспределенные"))
            if (showTriggerList) {
                for (i in remoteControllers.indices) {
                    automationUnitList.add(remoteControllers[i])
                }
                for (i in temperatureSensors.indices) {
                    automationUnitList.add(temperatureSensors[i])
                }
                for (i in humidityTemperatureSensors.indices) {
                    automationUnitList.add(humidityTemperatureSensors[i])
                }
                for (i in motionSensors.indices) {
                    automationUnitList.add(motionSensors[i])
                }
                for (i in lightSensors.indices) {
                    automationUnitList.add(lightSensors[i])
                }
                for (i in openCloseSensors.indices) {
                    automationUnitList.add(openCloseSensors[i])
                }
                for (i in leakDetectors.indices) {
                    automationUnitList.add(leakDetectors[i])
                }
            } else {
                for (i in powerUnits.indices) {
                    when (powerUnits[i].type) {
                        PowerUnit.RELAY, PowerUnit.DIMMER, PowerUnit.RGB_CONTROLLER
                        -> automationUnitList.add(powerUnits[i])
                    }
                }
                for (i in powerUnitsF.indices) {
                    automationUnitList.add(powerUnitsF[i])
                }
                for (i in powerSocketsF.indices) {
                    automationUnitList.add(powerSocketsF[i])
                }
                for (i in thermostats.indices) {
                    automationUnitList.add(thermostats[i])
                }
            }
            if (automationUnitList[automationUnitList.size - 1] is Room) {
                automationUnitList.removeAt(automationUnitList.size - 1)
            }

            if (!showTriggerList && !showPowerUnits && nooLitePRF64.getTXUnits().size > 0) {
                automationUnitList.add(Room(-1, "Информация"))
                automationUnitList.add(Info())
            }

            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        }).start()
    }


    private fun bindRoom(holder: RecyclerView.ViewHolder, position: Int) {

        val automationRoomViewHolder: AutomationRoomViewHolder = holder as AutomationRoomViewHolder
        val automationRoom: Room = automationUnitList[position] as Room

        automationRoomViewHolder.tvRoomName.text = automationRoom.name
    }

    private fun bindUnit(holder: RecyclerView.ViewHolder, position: Int) {

        val automationUnitViewHolder: AutomationUnitViewHolder = holder as AutomationUnitViewHolder

        when (val automationUnit: Any = automationUnitList[position]) {
            is PowerUnit -> bindPowerUnit(automationUnit, automationUnitViewHolder)
            is RemoteController -> bindRemoteController(automationUnit, automationUnitViewHolder)
            is TemperatureSensor -> bindTemperatureSensor(automationUnit, automationUnitViewHolder)
            is HumidityTemperatureSensor -> bindHumidityTemperatureSensor(automationUnit, automationUnitViewHolder)
            is MotionSensor -> bindMotionSensor(automationUnit, automationUnitViewHolder)
            is LightSensor -> bindLightSensor(automationUnit, automationUnitViewHolder)
            is OpenCloseSensor -> bindOpenCloseSensor(automationUnit, automationUnitViewHolder)
            is LeakDetector -> bindLeakDetector(automationUnit, automationUnitViewHolder)
            is PowerUnitF -> bindPowerUnitF(automationUnit, automationUnitViewHolder)
            is PowerSocketF -> bindPowerSocketF(automationUnit, automationUnitViewHolder)
            is Thermostat -> bindThermostat(automationUnit, automationUnitViewHolder)
            is Preset -> bindPreset(automationUnit, automationUnitViewHolder)
        }
    }

    private fun bindInfo(holder: RecyclerView.ViewHolder) {

        val automationInfoViewHolder: AutomationInfoViewHolder = holder as AutomationInfoViewHolder

        automationInfoViewHolder.bCreatePreset.setOnClickListener {}
    }

    private fun bindPowerUnit(powerUnit: PowerUnit, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(powerUnit)
        }
        automationUnitViewHolder.ivIcon.setImageResource(
                when (powerUnit.type) {
                    PowerUnit.RGB_CONTROLLER -> R.drawable.ic_rgb_controller
                    else -> R.drawable.ic_bulb
                }
        )
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = powerUnit.name
    }

    private fun bindRemoteController(remoteController: RemoteController, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(remoteController)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_remote_controller)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = remoteController.name
    }

    private fun bindTemperatureSensor(temperatureSensor: TemperatureSensor, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(temperatureSensor)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_temperature_)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = temperatureSensor.name
    }

    private fun bindHumidityTemperatureSensor(humidityTemperatureSensor: HumidityTemperatureSensor, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(humidityTemperatureSensor)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_temperature_humidity)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = humidityTemperatureSensor.name
    }

    private fun bindMotionSensor(motionSensor: MotionSensor, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(motionSensor)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_motion)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = motionSensor.name
    }

    private fun bindLightSensor(lightSensor: LightSensor, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(lightSensor)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_sun_moon)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = lightSensor.name
    }

    private fun bindOpenCloseSensor(openCloseSensor: OpenCloseSensor, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(openCloseSensor)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_door)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = openCloseSensor.name
    }

    private fun bindLeakDetector(leakDetector: LeakDetector, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(leakDetector)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_water)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = leakDetector.name
    }

    private fun bindPowerUnitF(powerUnitF: PowerUnitF, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(powerUnitF)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_bulb)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = powerUnitF.name
    }

    private fun bindPowerSocketF(powerSocketF: PowerSocketF, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(powerSocketF)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_power_socket)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = powerSocketF.name
    }

    private fun bindThermostat(thermostat: Thermostat, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(thermostat)
        }
        automationUnitViewHolder.ivIcon.setImageResource(R.drawable.ic_thermostat)
        automationUnitViewHolder.ivIcon.visibility = View.VISIBLE
        automationUnitViewHolder.tvUnitName.text = thermostat.name
    }

    private fun bindPreset(preset: Preset, automationUnitViewHolder: AutomationUnitViewHolder) {

        automationUnitViewHolder.llAutomationUnit.setOnClickListener {
            iAutomationUnit.setUnit(preset)
        }
        automationUnitViewHolder.ivIcon.visibility = View.GONE
        automationUnitViewHolder.tvUnitName.text = preset.name
    }


    private inner class AutomationRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvRoomName: TextView = itemView.findViewById(R.id.card_view_room_name)
    }

    private inner class AutomationUnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val llAutomationUnit: LinearLayout = itemView.findViewById(R.id.card_view_automation_unit_layout)
        val ivIcon: ImageView = itemView.findViewById(R.id.card_view_automation_unit_icon)
        val tvUnitName: TextView = itemView.findViewById(R.id.card_view_automation_unit_name)
    }

    private inner class Info

    private inner class AutomationInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val bCreatePreset: Button = itemView.findViewById(R.id.card_view_automation_info_create_preset_button)
    }
}
