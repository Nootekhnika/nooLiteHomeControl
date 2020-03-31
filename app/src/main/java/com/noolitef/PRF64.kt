package com.noolitef

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.noolitef.automatics.Automation
import com.noolitef.ftx.FTXUnits
import com.noolitef.ftx.PowerSocketF
import com.noolitef.ftx.PowerUnitF
import com.noolitef.ftx.RolletUnitF
import com.noolitef.presets.Preset
import com.noolitef.rx.*
import com.noolitef.settings.Settings
import com.noolitef.tx.PowerUnit
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class PRF64(private val activity: Activity) {
    // upgrade to Enum with value
    companion object {
        const val COMMAND_OFF = 0
        const val COMMAND_ON = 2
        const val COMMAND_TEMPERATURE_AND_HUMIDITY = 21
    }

    // updating flags
    private var mainUpdating: Boolean = false
    private var timersUpdating: Boolean = false
    private var autosUpdating: Boolean = false
    private var logUpdating: Boolean = false

    private var updatedTemperatureSensors: IntArray? = null
    private var updatedHumidityTemperatureSensors: IntArray? = null
    private var updatedOpenCloseSensors: IntArray? = null
    private var updatedLeakDetectors: IntArray? = null
    private var updatedLightSensors: IntArray? = null

    // file streams
    private var sharedPreferences: SharedPreferences
    private lateinit var fileInputStream: FileInputStream
    private lateinit var fileOutputStream: FileOutputStream

    // PRF-64 API files
    private var device: ByteArray
    private var preset: ByteArray
    private var user: ByteArray
    private var timer: ByteArray
    private var auto: ByteArray
    private lateinit var settings: ByteArray
    private var log: ByteArray
    private var lastUpdateTimestamp: String // sec|min|hour|weekday|date|month|year in HEX

    // nooLite TX units
    private lateinit var powerUnits: ArrayList<PowerUnit>

    // nooLite RX units
    private lateinit var remoteControllers: ArrayList<RemoteController>
    private lateinit var temperatureSensors: ArrayList<TemperatureSensor>
    private lateinit var humidityTemperatureSensors: ArrayList<HumidityTemperatureSensor>
    private lateinit var motionSensors: ArrayList<MotionSensor>
    private lateinit var openCloseSensors: ArrayList<OpenCloseSensor>
    private lateinit var leakDetectors: ArrayList<LeakDetector>
    private lateinit var lightSensors: ArrayList<LightSensor>

    // nooLite-F TX units
    private lateinit var powerUnitsF: ArrayList<PowerUnitF>
    private lateinit var powerSocketsF: ArrayList<PowerSocketF>
    private lateinit var thermostats: ArrayList<Thermostat>
    private lateinit var rolletUnitsF: ArrayList<RolletUnitF>
    private var ftxCount: Int = 0

    // nooLite-F TX state
    private lateinit var state: String // state of nooLite-F units in HEX

    // home & rooms
    private lateinit var home: Home

    // all units
    private lateinit var homeDevices: Any
    private lateinit var homeSensors: Any

    // units sorted by rooms
    private lateinit var roomDevices: Any
    private lateinit var roomSensors: Any

    // favorite units
    //private lateinit var favoriteDevices: Any #work_in_progress
    //private lateinit var favoriteSensors: Any #work_in_progress

    // units sorted by features
    //

    // common features
    private lateinit var presets: ArrayList<Preset>

    //private lateinit var timers: ArrayList<Timer> #work_in_progress
    private lateinit var automatics: ArrayList<Automation>

    init {
        var time = System.currentTimeMillis()
        sharedPreferences = activity.getSharedPreferences("nooLite", Context.MODE_PRIVATE)
        lastUpdateTimestamp = loadLastUpdateTimestamp()
        device = loadDevice()
        preset = loadPreset()
        user = loadUser()
        timer = loadTimer()
        auto = loadAuto()

        log = loadLog()
        Log.d("nooLiteF", "PRF64: loadFiles() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: loadFiles() - " + (System.currentTimeMillis() - time) + " ms")

        time = System.currentTimeMillis()
        initPRF64(device, preset, user, auto)
        Log.d("nooLiteF", "PRF64: init() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: init() - " + (System.currentTimeMillis() - time) + " ms")

        time = System.currentTimeMillis()
        parseRecentLog(log, temperatureSensors, humidityTemperatureSensors, openCloseSensors, leakDetectors, lightSensors)
        Log.d("nooLiteF", "PRF64: parseSavedLog() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: parseSavedLog() - " + (System.currentTimeMillis() - time) + " ms")
    }

    // getters

    fun getLastUpdateTimestamp(): String {
        return lastUpdateTimestamp
    }

    fun getDevice(): ByteArray {
        return device
    }

    fun getPreset(): ByteArray {
        return preset
    }

    fun getUser(): ByteArray {
        return user
    }

    fun getTimer(): ByteArray {
        return timer
    }

    fun getAuto(): ByteArray {

        return auto
    }

    fun getLog(): ByteArray {
        return log
    }

    fun getFTXCount(): Int {
        return ftxCount
    }

    fun getHome(): Home {
        return home
    }

    fun getPresets(): ArrayList<Preset> {
        return presets
    }

    fun getTXUnits(): ArrayList<PowerUnit> {
        return powerUnits
    }

    fun getRemoteControllers(): ArrayList<RemoteController> {
        return remoteControllers
    }

    fun getTemperatureSensors(): ArrayList<TemperatureSensor> {
        return temperatureSensors
    }

    fun getHumidityTemperatureSensors(): ArrayList<HumidityTemperatureSensor> {
        return humidityTemperatureSensors
    }

    fun getMotionSensors(): ArrayList<MotionSensor> {
        return motionSensors
    }

    fun getOpenCloseSensors(): ArrayList<OpenCloseSensor> {
        return openCloseSensors
    }

    fun getLeakDetectors(): ArrayList<LeakDetector> {
        return leakDetectors
    }

    fun getLightSensors(): ArrayList<LightSensor> {
        return lightSensors
    }

    fun getPowerUnitsF(): ArrayList<PowerUnitF> {
        return powerUnitsF
    }

    fun getPowerSocketsF(): ArrayList<PowerSocketF> {
        return powerSocketsF
    }

    fun getThermostats(): ArrayList<Thermostat> {
        return thermostats
    }

    fun getRolletUnitsF(): ArrayList<RolletUnitF> {
        return rolletUnitsF
    }

    fun getAutomatics(): ArrayList<Automation> {

        return automatics
    }

    // setters

    fun setState(state: String) {
        this.state = state

        parseState(state, powerUnitsF, powerSocketsF, thermostats, rolletUnitsF)
    }

    fun setLastUpdateTimestamp(state: String) {
        if (lastUpdateTimestamp != state.substring(255)) {
            saveLastUpdateTimestamp(state)
        }
    }

    fun setDevice(device: ByteArray) {
        this.device = device
    }

    fun setPreset(preset: ByteArray) {
        this.preset = preset
    }

    fun setUser(user: ByteArray) {
        this.user = user

        var time = System.currentTimeMillis()
        initPRF64(device, preset, user, auto)
        Log.d("nooLiteF", "PRF64: initPRF64() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: initPRF64() - " + (System.currentTimeMillis() - time) + " ms")

        time = System.currentTimeMillis()
        saveFiles(device, preset, user)
        Log.d("nooLiteF", "PRF64: saveFiles() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: saveFiles() - " + (System.currentTimeMillis() - time) + " ms")
    }

    fun setTimer(timer: ByteArray) {
        this.timer = timer

        var time = System.currentTimeMillis()
        saveTimer(timer)
        Log.d("nooLiteF", "PRF64: saveTimer() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: saveTimer() - " + (System.currentTimeMillis() - time) + " ms")
    }

    fun setAuto(auto: ByteArray) {

        this.auto = auto

        var time = System.currentTimeMillis()
        saveAuto(auto)
        Log.d("nooLiteF", "PRF64: saveAuto() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: saveAuto() - " + (System.currentTimeMillis() - time) + " ms")

        time = System.currentTimeMillis()
        this.automatics = parseAutomatics(device, user, auto)
        Log.d("nooLiteF", "PRF64: parseAutomatics() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: parseAutomatics() - " + (System.currentTimeMillis() - time) + " ms")
    }

    fun setLog(log: ByteArray) {
        var time = System.currentTimeMillis()
        parseRecentLog(log, temperatureSensors, humidityTemperatureSensors, openCloseSensors, leakDetectors, lightSensors)
        Log.d("nooLiteF", "PRF64: parseRecentLog() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: parseRecentLog() - " + (System.currentTimeMillis() - time) + " ms")

        if (log.size == this.log.size) return

        this.log = log

        time = System.currentTimeMillis()
        saveLog(log)
        Log.d("nooLiteF", "PRF64: saveLog() - " + (System.currentTimeMillis() - time) + " ms")
        if (activity is HomeActivity) activity.writeAppLog("PRF64: saveLog() - " + (System.currentTimeMillis() - time) + " ms")
    }

    // functions

    private fun initPRF64(device: ByteArray, preset: ByteArray, user: ByteArray, auto: ByteArray) {
        this.home = parseHome(user)
        loadSettings(home)

        presets = parsePresets(preset, user)

        powerUnits = parseTXUnits(device, user, home)

        val (remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors) = parseRXUnits(device, user, home)
        this.remoteControllers = remoteControllers
        this.temperatureSensors = temperatureSensors
        this.humidityTemperatureSensors = humidityTemperatureSensors
        this.motionSensors = motionSensors
        this.openCloseSensors = openCloseSensors
        this.leakDetectors = leakDetectors
        this.lightSensors = lightSensors
        //rxUnits = parseRXUnits(device, user, home)
        // homeSensors = parseRXUnits(device, user, home)

        val (powerUnitsF, powerSocketsF, thermostats, rolletUnitsF) = parseFTXUnits(device, user, home)
        this.powerUnitsF = powerUnitsF
        this.powerSocketsF = powerSocketsF
        this.thermostats = thermostats
        this.rolletUnitsF = rolletUnitsF
        //ftxUnits = parseFTXUnits(device, user, home)
        //homeDevices = parseFTXUnits(device, user, home)

        if (auto[0].toInt() != -1) {
            this.automatics = parseAutomatics(device, user, auto)
        }
    }

    private fun toUnsignedByte(byte: Byte): Int {
        return byte.toInt() and 0xFF
    }

    private fun toByteObjectArray(byteArray: ByteArray): Array<Byte> {
        var byteObjectArray = Array<Byte>(byteArray.size) { -1 }

        for (b in byteArray.indices) {
            byteObjectArray[b] = byteArray[b]
        }

        return byteObjectArray
    }

    private fun loadSettings(home: Home) {
        // get IP & DNS
        Settings.setDNS(sharedPreferences.getString("DNS", "noolite.nootech.dns.by:80"))
        Settings.setIP(sharedPreferences.getString("URL", "192.168.0.170"))
        Settings.useDNS(sharedPreferences.getBoolean("useDNS", false))

        // get login & password
        Settings.setLogin(sharedPreferences.getString("Login", Settings.login()))
        Settings.setPassword(sharedPreferences.getString("Password", Settings.password()))
        Settings.useAuthorization(sharedPreferences.getBoolean("Authorization", false))

        // set home's settings
        home.ip = Settings.getIP()
        home.dns = Settings.getDNS()
        home.useDNS(Settings.useDNS())
        home.login = Settings.login()
        home.password = Settings.password()
        home.useAuthorization(Settings.isAuthorization())
    }

    private fun loadLastUpdateTimestamp(): String {
        return try {
            fileInputStream = activity.openFileInput("cache_timestamp.bin")
            val timestamp = ByteArray(14)
            fileInputStream.read(timestamp)
            fileInputStream.close()
            timestamp.toString(Charset.forName("cp1251"))
        } catch (e: Exception) {
            "00000000000000"
        }
    }

    private fun loadDevice(): ByteArray {
        var device = ByteArray(4102)
        Arrays.fill(device, -1)
        return try {
            fileInputStream = activity.openFileInput("cached_device.bin")
            fileInputStream.read(device)
            fileInputStream.close()
            device
        } catch (e: Exception) {
            device
        }
    }

    private fun loadPreset(): ByteArray {
        var preset = ByteArray(32774)
        Arrays.fill(preset, -1)
        return try {
            fileInputStream = activity.openFileInput("cached_preset.bin")
            fileInputStream.read(preset)
            fileInputStream.close()
            preset
        } catch (e: Exception) {
            preset
        }
    }

    private fun loadUser(): ByteArray {
        var user = ByteArray(12294)
        Arrays.fill(user, -1)
        return try {
            fileInputStream = activity.openFileInput("cached_user.bin")
            fileInputStream.read(user)
            fileInputStream.close()
            user
        } catch (e: Exception) {
            user
        }
    }

    private fun loadTimer(): ByteArray {
        var timer = ByteArray(8198)
        Arrays.fill(timer, -1)
        return try {
            fileInputStream = activity.openFileInput("cached_timer.bin")
            fileInputStream.read(timer)
            fileInputStream.close()
            timer
        } catch (e: Exception) {
            timer
        }
    }

    private fun loadAuto(): ByteArray {

        val auto = ByteArray(12294)
        Arrays.fill(auto, -1)

        return try {
            fileInputStream = activity.openFileInput("cached_auto.bin")
            fileInputStream.read(auto)
            fileInputStream.close()

            auto
        } catch (e: Exception) {
            if (activity is HomeActivity) activity.writeAppLog("Exception in PRF64.kt : loadAuto(auto.bin)\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e))

            auto
        }
    }

    private fun loadLog(): ByteArray {
        return try {
            fileInputStream = activity.openFileInput("cached_log.bin")
            val buffer = ByteArray(21)
            val arrayLog: ArrayList<Array<Byte>> = ArrayList()

            while ((fileInputStream.read(buffer)) != -1) {
                arrayLog.add(toByteObjectArray(buffer))
            }
            fileInputStream.close()

            var log = ByteArray(21 * arrayLog.size)

            for (i in arrayLog.indices) {
                for (b in 0..20) {
                    log[21 * i + b] = arrayLog[i][b]
                }
            }

            log
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    private fun saveFiles(device: ByteArray, preset: ByteArray, user: ByteArray) {
        try {
            fileOutputStream = activity.openFileOutput("cached_device.bin", Context.MODE_PRIVATE)
            fileOutputStream.write(device)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            if (activity is HomeActivity) activity.writeAppLog("Exception in PRF64.kt : saveFiles(device.bin)\n" + NooLiteF.getStackTrace(e))
        }

        try {
            fileOutputStream = activity.openFileOutput("cached_preset.bin", Context.MODE_PRIVATE)
            fileOutputStream.write(preset)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            if (activity is HomeActivity) activity.writeAppLog("Exception in PRF64.kt : saveFiles(preset.bin)\n" + NooLiteF.getStackTrace(e))
        }

        try {
            fileOutputStream = activity.openFileOutput("cached_user.bin", Context.MODE_PRIVATE)
            fileOutputStream.write(user)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            if (activity is HomeActivity) activity.writeAppLog("Exception in PRF64.kt : saveFiles(user.bin)\n" + NooLiteF.getStackTrace(e))
        }

        val timer = ByteArray(8198)
        Arrays.fill(timer, -1)
        setTimer(timer)

        val auto = ByteArray(12294)
        Arrays.fill(auto, -1)
        setAuto(auto)
    }

    private fun saveTimer(timer: ByteArray) {
        try {
            fileOutputStream = activity.openFileOutput("cached_timer.bin", Context.MODE_PRIVATE)
            fileOutputStream.write(timer)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            if (activity is HomeActivity) activity.writeAppLog("Exception in PRF64.kt : saveTimer(timer.bin)\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e))
        }
    }

    private fun saveAuto(auto: ByteArray) {
        try {
            fileOutputStream = activity.openFileOutput("cached_auto.bin", Context.MODE_PRIVATE)
            fileOutputStream.write(auto)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            if (activity is HomeActivity) activity.writeAppLog("Exception in PRF64.kt : saveAuto(auto.bin)\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e))
        }
    }

    private fun saveLastUpdateTimestamp(state: String) {
        try {
            lastUpdateTimestamp = state.substring(255)
            fileOutputStream = activity.openFileOutput("cache_timestamp.bin", Context.MODE_PRIVATE)
            fileOutputStream.write(lastUpdateTimestamp.toByteArray(Charset.forName("cp1251")))
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            if (activity is HomeActivity) activity.writeAppLog("Exception in PRF64.kt : saveLastUpdateTimestamp()\n" + NooLiteF.getStackTrace(e))
        }
    }

    private fun saveLog(log: ByteArray) {
        try {
            fileOutputStream = activity.openFileOutput("cached_log.bin", Context.MODE_PRIVATE)
            fileOutputStream.write(log)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            if (activity is HomeActivity) activity.writeAppLog("Exception in PRF64.kt : saveLog()\n" + NooLiteF.getStackTrace(e))
        }
    }

    private fun parseUnitName(user: ByteArray, userNameByte: Int): String {
        // parse user.bin
        // get unit's name

        if (userNameByte == -1) return ""

        var name: String

        if (toUnsignedByte(user[userNameByte + 31]) != 255) {
            val nameBytes = ByteArray(32)
            for (nameByte in 0..31) {
                if (toUnsignedByte(user[userNameByte + nameByte]) != 0) {
                    nameBytes[nameByte] = user[userNameByte + nameByte]
                } else {
                    nameBytes[nameByte] = 32
                }
            }
            name = nameBytes.toString(Charset.forName("cp1251")).trim()
        } else {
            name = ""
        }

        return name
    }

    private fun getRoomName(home: Home, roomID: Int): String {
        // get unit's room name

        var id = roomID
        var roomName = ""

        if (id != 255) {
            home.rooms?.let {
                for (room in home.rooms) {
                    if (room.id == id) {
                        roomName = room.name
                        break
                    }
                }
            }
        }

        return roomName
    }

    private fun parseHome(user: ByteArray): Home {
        // get Home
        // parse user.bin

        // get home's name

        var homeName: String

        val userHomeByte = 6 + 8704
        if (toUnsignedByte(user[userHomeByte + 62]) != 255) {
            val nameBytes = ByteArray(63)
            for (nameByte in 0..62) {
                if (toUnsignedByte(user[userHomeByte + nameByte]) != 0) {
                    nameBytes[nameByte] = user[userHomeByte + nameByte]
                } else {
                    nameBytes[nameByte] = 32
                }
            }
            homeName = nameBytes.toString(Charset.forName("cp1251")).trim()
        } else {
            homeName = ""
        }

        // get Rooms

        var roomID = -1
        var roomName: String

        var rooms: ArrayList<Room> = ArrayList()

        for (userRoomByte in 8774..10469 step 53) {
            roomID++
            if (toUnsignedByte(user[userRoomByte + 31]) != 255) {
                val nameBytes = ByteArray(32)
                for (nameByte in 0..31) {
                    if (toUnsignedByte(user[userRoomByte + nameByte]) != 0) {
                        nameBytes[nameByte] = user[userRoomByte + nameByte]
                    } else {
                        nameBytes[nameByte] = 32
                    }
                }

                roomName = nameBytes.toString(Charset.forName("cp1251")).trim()
                rooms.add(Room(roomID, roomName))
            }
        }

        return Home(homeName, "192.168.0.170", "noolite.nootech.dns.by:80", false, "admin", "admin", false, rooms)
    }

    private fun parsePresets(preset: ByteArray, user: ByteArray): ArrayList<Preset> {
        var index = -1
        var state = 0
        var name: String
        var commands: Array<ByteArray>

        var presets: ArrayList<Preset> = ArrayList()

        // get presets
        for (presetByte in 6..32773 step 1024) {

            // parse preset.bin
            index++
            commands = Array(73) { ByteArray(14) { -1 } }

            if (toUnsignedByte(preset[presetByte]) != 255) {
                // get preset's commands
                for (commandIndex in 0..72 step 1) {
                    for (commandByte in 0..13 step 1) {
                        commands[commandIndex][commandByte] = preset[presetByte + commandIndex * 14 + commandByte]
                    }
                }

                // get preset's name
                // parse user.bin
                val userNameByte = 10470 + index * 33
                name = parseUnitName(user, userNameByte)

                presets.add(Preset(index, state, name, commands))
            }
        }

        return presets
    }

    private fun parseTXUnits(device: ByteArray, user: ByteArray, home: Home): ArrayList<PowerUnit> {
        var id: String
        var channel = -1
        var type: Int
        var presetState = 0
        var name: String
        var roomID: Int
        var roomName: String
        var preset = false

        var powerUnits: ArrayList<PowerUnit> = ArrayList()

        // get nooLite TX units
        for (deviceByte in 6..517 step 8) {

            // parse device.bin
            id = String.format("%2s%2s%2s%2s", Integer.toHexString(toUnsignedByte(device[deviceByte + 0])), Integer.toHexString(toUnsignedByte(device[deviceByte + 1])), Integer.toHexString(toUnsignedByte(device[deviceByte + 2])), Integer.toHexString(toUnsignedByte(device[deviceByte + 3]))).replace(' ', '0').toUpperCase()
            channel++

            if (id != "FFFFFFFF") {
                type = toUnsignedByte(device[deviceByte + 4])

                // get TX's name
                val userNameByte = 6 + channel * 34
                name = parseUnitName(user, userNameByte)

                // get TX's room name
                roomID = toUnsignedByte(user[userNameByte + 33])
                roomName = getRoomName(home, roomID)

                powerUnits.add(PowerUnit(type, channel, presetState, roomID, roomName, name, preset))
            }
        }

        return powerUnits
    }

    private fun parseRXUnits(device: ByteArray, user: ByteArray, home: Home): RXUnits {
        // for all RXUnits units
        var id: String
        var channel = -1
        var battery = false
        var type: Int
        var roomID: Int
        var roomName: String
        var name: String
        // for temperature & humidity sensors only
        var currentTemperature = .0
        // for humidity sensors only
        var currentHumidity = 0
        // for open/close sensors only
        var closed = true
        // for leakage sensors only
        var leakage = false
        // for light sensors only
        var darkness = true

        var remoteControllers: ArrayList<RemoteController> = ArrayList()
        var temperatureSensors: ArrayList<TemperatureSensor> = ArrayList()
        var humidityTemperatureSensors: ArrayList<HumidityTemperatureSensor> = ArrayList()
        var motionSensors: ArrayList<MotionSensor> = ArrayList()
        var openCloseSensors: ArrayList<OpenCloseSensor> = ArrayList()
        var leakDetectors: ArrayList<LeakDetector> = ArrayList()
        var lightSensors: ArrayList<LightSensor> = ArrayList()

        // get nooLite RXUnits units
        for (deviceByte in 1030..1541 step 8) {

            // parse device.bin
            id = String.format("%2s%2s%2s%2s", Integer.toHexString(toUnsignedByte(device[deviceByte + 0])), Integer.toHexString(toUnsignedByte(device[deviceByte + 1])), Integer.toHexString(toUnsignedByte(device[deviceByte + 2])), Integer.toHexString(toUnsignedByte(device[deviceByte + 3]))).replace(' ', '0').toUpperCase()
            channel++

            if (id != "FFFFFFFF") {
                type = toUnsignedByte(device[deviceByte + 4])

                // get RXUnits's name
                val userNameByte = 2182 + channel * 34
                name = parseUnitName(user, userNameByte)

                // get RXUnits's room name
                roomID = toUnsignedByte(user[userNameByte + 33])
                roomName = getRoomName(home, roomID)

                // sort RXUnits by type
                when (type) {
                    0 ->
                        // PU/PB/PG
                        remoteControllers.add(RemoteController(channel, battery, roomID, roomName, name))
                    1 ->
                        // PT112
                        temperatureSensors.add(TemperatureSensor(channel, !battery, roomID, roomName, name, currentTemperature, -1, -1, -1, -1, -1, -1, -1))
                    2 ->
                        // PT111
                        humidityTemperatureSensors.add(HumidityTemperatureSensor(channel, !battery, roomID, roomName, name, currentTemperature, currentHumidity, -1, -1, -1, -1, -1, -1, -1))
                    5 ->
                        // PM112
                        motionSensors.add(MotionSensor(channel, !battery, roomID, roomName, name))
                    8 ->
                        // DS-1
                        openCloseSensors.add(OpenCloseSensor(channel, !battery, closed, roomID, roomName, name, -1, -1, -1, -1, -1, -1, -1))
                    9 ->
                        // WS-1
                        leakDetectors.add(LeakDetector(channel, battery, leakage, roomID, roomName, name, -1, -1, -1, -1, -1, -1, -1))
                    10 ->
                        // PL111
                        lightSensors.add(LightSensor(channel, battery, darkness, roomID, roomName, name, -1, -1, -1, -1, -1, -1, -1))
                }
            }
        }

        return RXUnits(remoteControllers, temperatureSensors, humidityTemperatureSensors, motionSensors, openCloseSensors, leakDetectors, lightSensors)
    }

    private fun parseFTXUnits(device: ByteArray, user: ByteArray, home: Home): FTXUnits {
        // for all FTXUnits units
        var id: String
        var index = -1
        var type: Int
        var presetState = 0
        var state = 0
        var roomID: Int
        var roomName: String
        var name: String
        var preset = false
        // for dimmers only (SUF)
        var dimmer = true
        var brightness = 0
        // for thermostats only (SRF-T)
        var currentTemperature = 0
        var targetTemperature = 0
        var output = 0

        var powerUnitsF: ArrayList<PowerUnitF> = ArrayList()
        var powerSocketsF: ArrayList<PowerSocketF> = ArrayList()
        var thermostats: ArrayList<Thermostat> = ArrayList()
        var rolletUnitsF: ArrayList<RolletUnitF> = ArrayList()

        // get nooLite FTXUnits units
        for (deviceByte in 2054..2565 step 8) {

            // parse device.bin
            id = String.format("%2s%2s%2s%2s", Integer.toHexString(toUnsignedByte(device[deviceByte + 0])), Integer.toHexString(toUnsignedByte(device[deviceByte + 1])), Integer.toHexString(toUnsignedByte(device[deviceByte + 2])), Integer.toHexString(toUnsignedByte(device[deviceByte + 3]))).replace(' ', '0').toUpperCase()
            index++

            if (id != "FFFFFFFF") {
                type = toUnsignedByte(device[deviceByte + 4])

                // get FTXUnits's name
                val userNameByte = 4358 + index * 34
                name = parseUnitName(user, userNameByte)

                // get FTXUnits's room name
                roomID = toUnsignedByte(user[userNameByte + 33])
                roomName = getRoomName(home, roomID)

                // sort FTXUnits by type
                when (type) {
                    1 ->
                        // SLF
                        powerUnitsF.add(PowerUnitF(id, index, presetState, state, roomID, roomName, name, brightness, preset))
                    2 ->
                        // SLF
                        powerUnitsF.add(PowerUnitF(id, index, presetState, state, roomID, roomName, name, brightness, preset))
                    3 ->
                        // SRF
                        powerSocketsF.add(PowerSocketF(id, index, presetState, state, roomID, roomName, name, preset))
                    4 ->
                        // SRF
                        powerSocketsF.add(PowerSocketF(id, index, presetState, state, roomID, roomName, name, preset))
                    5 -> {
                        // SUF
                        powerUnitsF.add(PowerUnitF(id, index, presetState, state, roomID, roomName, name, brightness, preset))
                        powerUnitsF[powerUnitsF.size - 1].setDimming(dimmer)
                    }
                    6 ->
                        // SRF-T
                        thermostats.add(Thermostat(id, index, state, currentTemperature, targetTemperature, output, roomID, roomName, name))
                    7 ->
                        // SRF-R
                        rolletUnitsF.add(RolletUnitF(id, index, state, roomID, roomName, name))
                }

                ftxCount++
            }
        }

        return FTXUnits(powerUnitsF, powerSocketsF, thermostats, rolletUnitsF)
    }

    private fun parseAutomatics(device: ByteArray, user: ByteArray, auto: ByteArray): ArrayList<Automation> {

        val automatics = ArrayList<Automation>()

        var automationIndex = 0
        var autoStep: Int
        var autoByte = 4102
        do {
            if (toUnsignedByte(auto[autoByte]) != 255) {
                var name = ""
                if (toUnsignedByte(auto[6 + (32 * automationIndex) + 31]) != 255) {
                    val nameBytes = ByteArray(32)

                    for (nameByte in 0..31) {
                        if (toUnsignedByte(auto[6 + (32 * automationIndex) + nameByte]) != 0) {
                            nameBytes[nameByte] = auto[6 + (32 * automationIndex) + nameByte]
                        } else {
                            nameBytes[nameByte] = 32
                        }
                    }

                    name = nameBytes.toString(Charset.forName("cp1251")).trim()
                }

                val type = auto[autoByte].toInt()

                val state = auto[autoByte + 1].toInt()

                val triggerType = auto[autoByte + 2].toInt()

                val triggerIndex = auto[autoByte + 3].toInt()

                val parameter = auto[autoByte + 4].toInt()

                val command = IntArray(14) { 255 }
                for (commandByte in 0..13) {
                    command[commandByte] = toUnsignedByte(auto[autoByte + 5 + commandByte])
                }

                val entityName =
                        when (type) {
                            Automation.TYPE_HEATING,
                            Automation.TYPE_COOLING -> "Температура"

                            Automation.TYPE_DEHUMIDIFICATION,
                            Automation.TYPE_HUMIDIFICATION -> "Влажность"

                            Automation.TYPE_EVENT -> "Событие"

                            else -> ""
                        }

                val triggerNameByte = (if (triggerType == Automation.TRIGGER_RX) 2182 else 4358) + triggerIndex * 34  // RX else FTX (not supported)
                val triggerName = parseUnitName(user, triggerNameByte)

                val eventName =
                        when (type) {
                            Automation.TYPE_HEATING -> "до $parameter°C"
                            Automation.TYPE_COOLING -> "до $parameter°C"

                            Automation.TYPE_DEHUMIDIFICATION -> "до $parameter%"
                            Automation.TYPE_HUMIDIFICATION -> "до $parameter%"

                            Automation.TYPE_EVENT -> {

                                when (toUnsignedByte(device[1030 + (triggerIndex * 8) + 4])) {
                                    0, 7 ->
                                        when (parameter) {
                                            0 -> "выключение"
                                            2 -> "включение"
                                            4 -> "переключение"
                                            else -> ""
                                        }
                                    5 ->
                                        if (parameter == 25) "движение"
                                        else ""
                                    8 ->
                                        when (parameter) {
                                            0 -> "закрытие"
                                            2 -> "открытие"
                                            else -> ""
                                        }
                                    9 ->
                                        when (parameter) {
                                            0 -> "осушениие"
                                            2 -> "протечка"
                                            else -> ""
                                        }
                                    10 ->
                                        when (parameter) {
                                            0 -> "свет"
                                            2 -> "тень"
                                            else -> ""
                                        }
                                    else -> ""
                                }
                            }

                            else -> ""
                        }

                val unitAction =
                        if (command[0] == 255) {
                            "..."
                        } else {
                            if (command[0] == 254) {
                                "Сценарий"
                            } else {
                                when (type) {
                                    Automation.TYPE_HEATING -> "Нагрев"
                                    Automation.TYPE_COOLING -> "Охлаждение"

                                    Automation.TYPE_DEHUMIDIFICATION -> "Осушение"
                                    Automation.TYPE_HUMIDIFICATION -> "Увлажнение"

                                    Automation.TYPE_EVENT -> {
                                        when (command[4]) {
                                            0 -> "Выключить"
                                            2 -> "Включить"
                                            25 -> "Включить на ${(command[6] * 5.0 / 60.0 + .5).toInt()} мин"
                                            else -> "-"
                                        }
                                    }

                                    else -> "..."
                                }
                            }
                        }

                var ftxUnit: Any? = null
                var unitNameByte = -1
                // PRESET
                if (command[0] == 254) {
                    unitNameByte = 10470 + (command[1] * 33)
                } else {
                    if (command[10] == 0 && command[11] == 0 && command[12] == 0 && command[13] == 0) {
                        // TX
                        unitNameByte = 6 + (command[3] * 34)
                    } else {
                        // FTX
                        var deviceId: String
                        val unitId = "%2s%2s%2s%2s".format(Integer.toHexString(command[10]), Integer.toHexString(command[11]), Integer.toHexString(command[12]), Integer.toHexString(command[13])).replace(' ', '0').toUpperCase(Locale.ROOT)
                        var unitIndex = 0

                        for (deviceByte in 2054..2565 step 8) {
                            deviceId = "%2s%2s%2s%2s".format(Integer.toHexString(toUnsignedByte(device[deviceByte + 0])), Integer.toHexString(toUnsignedByte(device[deviceByte + 1])), Integer.toHexString(toUnsignedByte(device[deviceByte + 2])), Integer.toHexString(toUnsignedByte(device[deviceByte + 3]))).replace(' ', '0').toUpperCase(Locale.ROOT)

                            if (deviceId == unitId) {
                                ftxUnit = getFtxUnit(unitIndex)
                                unitNameByte = 4358 + unitIndex * 34

                                break
                            } else {
                                unitIndex++
                            }
                        }
                    }
                }
                val unitName = parseUnitName(user, unitNameByte)

                val automation = Automation(automationIndex, name, type, state, triggerType, triggerIndex, parameter, command, entityName, triggerName, eventName, unitAction, unitName, getRxUnit(triggerIndex))
                automation.allowTemporaryOn(ftxUnit !is Thermostat)
                automatics.add(automation)
            }

            automationIndex++
            autoStep =
                    if (autoByte == 7924) 274
                    else 273
            autoByte += autoStep
        } while (autoByte < 12293)

        return automatics
    }

    private fun parseState(state: String, powerUnitsF: ArrayList<PowerUnitF>, powerSocketsF: ArrayList<PowerSocketF>, thermostats: ArrayList<Thermostat>, rolletUnitsF: ArrayList<RolletUnitF>) {
        var b: Int
        var binary: String
        var brightness: Int
        var temperature: Int

        for (powerUnitF in powerUnitsF) {
            b = 14 + 3 * powerUnitF.index
            binary = String.format("%4s", Integer.toBinaryString(Integer.parseInt(state.substring(b, b + 1), 16))).replace(' ', '0')

            if (binary.substring(2, 3) == "0") {
                powerUnitF.state = PowerUnitF.NOT_CONNECTED
            } else {
                if (binary.substring(0, 1) == "0") {
                    powerUnitF.state = PowerUnitF.OFF
                } else {
                    powerUnitF.state = PowerUnitF.ON
                }
            }

            brightness = Integer.parseInt(state.substring(b + 1, b + 3), 16)
            powerUnitF.brightness = (brightness / 255.0 * 100 + .5).toInt()

            if (activity is HomeActivity) activity.updateAdapterItem(powerUnitF.adapterPosition)
        }

        for (powerSocketF in powerSocketsF) {
            b = 14 + 3 * powerSocketF.index
            binary = String.format("%4s", Integer.toBinaryString(Integer.parseInt(state.substring(b, b + 1), 16))).replace(' ', '0')

            if (binary.substring(2, 3) == "0") {
                powerSocketF.state = PowerSocketF.NOT_CONNECTED
            } else {
                if (binary.substring(0, 1) == "0") {
                    powerSocketF.state = PowerSocketF.OFF
                } else {
                    powerSocketF.state = PowerSocketF.ON
                }
            }

            if (activity is HomeActivity) activity.updateAdapterItem(powerSocketF.adapterPosition)
        }

        for (thermostat in thermostats) {
            b = 14 + 3 * thermostat.index
            binary = String.format("%4s", Integer.toBinaryString(Integer.parseInt(state.substring(b, b + 1), 16))).replace(' ', '0')

            if (binary.substring(2, 3) == "0") {
                thermostat.state = Thermostat.NOT_CONNECTED
                temperature = 255
            } else {
                if (binary.substring(0, 1) == "0") {
                    thermostat.state = Thermostat.OFF
                } else {
                    thermostat.state = Thermostat.ON
                }
                temperature = Integer.parseInt(state.substring(b + 1, b + 3), 16)
            }
            if (binary.substring(1, 2) == "0") {
                thermostat.outputState = Thermostat.OUTPUT_OFF
            } else {
                thermostat.outputState = Thermostat.OUTPUT_ON
            }

            thermostat.currentTemperature = temperature

            if (activity is HomeActivity) activity.updateAdapterItem(thermostat.adapterPosition)
        }

        for (rolletUnitF in rolletUnitsF) {
            b = 14 + 3 * rolletUnitF.index
            binary = String.format("%4s", Integer.toBinaryString(Integer.parseInt(state.substring(b, b + 1), 16))).replace(' ', '0')

            if (binary.substring(2, 3) == "0") {
                rolletUnitF.state = RolletUnitF.NOT_CONNECTED
            } else {
                if (binary.substring(0, 1) == "0") {
                    rolletUnitF.state = RolletUnitF.CLOSE
                } else {
                    rolletUnitF.state = RolletUnitF.OPEN
                }
            }

            rolletUnitF.isInversion = sharedPreferences.getBoolean(rolletUnitF.id, false)

            if (activity is HomeActivity) activity.updateAdapterItem(rolletUnitF.adapterPosition)
        }
    }

    fun parseRecentLog(logRecord: ByteArray) {
        parseRecentLog(logRecord, temperatureSensors, humidityTemperatureSensors, openCloseSensors, leakDetectors, lightSensors)
    }


    private fun parseRecentLog(log: ByteArray, temperatureSensors: ArrayList<TemperatureSensor>, humidityTemperatureSensors: ArrayList<HumidityTemperatureSensor>, openCloseSensors: ArrayList<OpenCloseSensor>, leakDetectors: ArrayList<LeakDetector>, lightSensors: ArrayList<LightSensor>) {
        if (log.isEmpty()) return

        var logRecord = ByteArray(21)
        var batteryState: String
        var toTheNextLogRecord: Boolean
        if (updatedTemperatureSensors == null) updatedTemperatureSensors = IntArray(temperatureSensors.size)
        if (updatedHumidityTemperatureSensors == null) updatedHumidityTemperatureSensors = IntArray(humidityTemperatureSensors.size)
        if (updatedOpenCloseSensors == null) updatedOpenCloseSensors = IntArray(openCloseSensors.size)
        if (updatedLeakDetectors == null) updatedLeakDetectors = IntArray(leakDetectors.size)
        if (updatedLightSensors == null) updatedLightSensors = IntArray(lightSensors.size)

        // parse log.bin
        for (logByte in 0 until log.size step 21) {
            System.arraycopy(log, logByte, logRecord, 0, 21)

            // get recent data for PT112 sensors
            for (i in temperatureSensors.indices) {
                if (updatedTemperatureSensors!![i] == 0) {
                    if (temperatureSensors[i].channel == toUnsignedByte(logRecord[3]) && toUnsignedByte(logRecord[4]) == COMMAND_TEMPERATURE_AND_HUMIDITY) {
                        val temperature = String.format("%8s%8s", Integer.toBinaryString(toUnsignedByte(logRecord[7])), Integer.toBinaryString(toUnsignedByte(logRecord[6]))).replace(' ', '0').substring(4, 16)
                        if (temperature[0] == '0') {
                            temperatureSensors[i].currentTemperature = Integer.parseInt(temperature, 2).toDouble() / 10
                        } else {
                            temperatureSensors[i].currentTemperature = (4096 - Integer.parseInt(temperature, 2)).toDouble() / -10
                        }

                        batteryState = String.format("%8s", Integer.toBinaryString(toUnsignedByte(logRecord[7]))).replace(' ', '0')
                        temperatureSensors[i].isBatteryOK = batteryState[0] == '0'

                        temperatureSensors[i].updateSecond = toUnsignedByte(logRecord[14])
                        temperatureSensors[i].updateMinute = toUnsignedByte(logRecord[15])
                        temperatureSensors[i].updateHour = toUnsignedByte(logRecord[16])
                        temperatureSensors[i].updateWeekDay = toUnsignedByte(logRecord[17])
                        temperatureSensors[i].updateDay = toUnsignedByte(logRecord[18])
                        temperatureSensors[i].updateMonth = toUnsignedByte(logRecord[19])
                        temperatureSensors[i].updateYear = toUnsignedByte(logRecord[20])

                        updatedTemperatureSensors!![i] = 1

                        if (activity is HomeActivity) activity.updateAdapterItem(temperatureSensors[i].adapterPosition)
                    }
                }
            }

            // get recent data for PT111 sensors
            for (i in humidityTemperatureSensors.indices) {
                if (updatedHumidityTemperatureSensors!![i] == 0) {
                    if (humidityTemperatureSensors[i].channel == toUnsignedByte(logRecord[3]) && toUnsignedByte(logRecord[4]) == COMMAND_TEMPERATURE_AND_HUMIDITY) {
                        val temperature = String.format("%8s%8s", Integer.toBinaryString(toUnsignedByte(logRecord[7])), Integer.toBinaryString(toUnsignedByte(logRecord[6]))).replace(' ', '0').substring(4, 16)
                        if (temperature[0] == '0') {
                            humidityTemperatureSensors[i].temperature = Integer.parseInt(temperature, 2).toDouble() / 10
                        } else {
                            humidityTemperatureSensors[i].temperature = (4096 - Integer.parseInt(temperature, 2)).toDouble() / -10
                        }

                        batteryState = String.format("%8s", Integer.toBinaryString(toUnsignedByte(logRecord[7]))).replace(' ', '0')
                        humidityTemperatureSensors[i].isBatteryOK = batteryState[0] == '0'

                        humidityTemperatureSensors[i].humidity = toUnsignedByte(logRecord[8])

                        humidityTemperatureSensors[i].lastUpdateSecond = toUnsignedByte(logRecord[14])
                        humidityTemperatureSensors[i].lastUpdateMinute = toUnsignedByte(logRecord[15])
                        humidityTemperatureSensors[i].lastUpdateHour = toUnsignedByte(logRecord[16])
                        humidityTemperatureSensors[i].lastUpdateWeekDay = toUnsignedByte(logRecord[17])
                        humidityTemperatureSensors[i].lastUpdateDay = toUnsignedByte(logRecord[18])
                        humidityTemperatureSensors[i].lastUpdateMonth = toUnsignedByte(logRecord[19])
                        humidityTemperatureSensors[i].lastUpdateYear = toUnsignedByte(logRecord[20])

                        updatedHumidityTemperatureSensors!![i] = 1

                        if (activity is HomeActivity) activity.updateAdapterItem(humidityTemperatureSensors[i].adapterPosition)
                    }
                }
            }

            // get recent data for DS-1 sensors
            for (i in openCloseSensors.indices) {
                if (updatedOpenCloseSensors!![i] == 0) {
                    if (openCloseSensors[i].channel == toUnsignedByte(logRecord[3]) && (toUnsignedByte(logRecord[4]) == COMMAND_OFF || toUnsignedByte(logRecord[4]) == COMMAND_ON)) {
                        when (toUnsignedByte(logRecord[4])) {
                            0 -> openCloseSensors[i].isClosed = true
                            2 -> openCloseSensors[i].isClosed = false
                        }

                        batteryState = String.format("%8s", Integer.toBinaryString(toUnsignedByte(logRecord[7]))).replace(' ', '0')
                        openCloseSensors[i].isBatteryOK = batteryState[0] == '0'

                        openCloseSensors[i].lastUpdateSecond = toUnsignedByte(logRecord[14])
                        openCloseSensors[i].lastUpdateMinute = toUnsignedByte(logRecord[15])
                        openCloseSensors[i].lastUpdateHour = toUnsignedByte(logRecord[16])
                        openCloseSensors[i].lastUpdateWeekDay = toUnsignedByte(logRecord[17])
                        openCloseSensors[i].lastUpdateDay = toUnsignedByte(logRecord[18])
                        openCloseSensors[i].lastUpdateMonth = toUnsignedByte(logRecord[19])
                        openCloseSensors[i].lastUpdateYear = toUnsignedByte(logRecord[20])

                        updatedOpenCloseSensors!![i] = 1

                        if (activity is HomeActivity) activity.updateAdapterItem(openCloseSensors[i].adapterPosition)
                    }
                }
            }

            // get recent data for WS-1 sensors
            for (i in leakDetectors.indices) {
                if (updatedLeakDetectors!![i] == 0) {
                    if (leakDetectors[i].channel == toUnsignedByte(logRecord[3]) && (toUnsignedByte(logRecord[4]) == COMMAND_OFF || toUnsignedByte(logRecord[4]) == COMMAND_ON)) {
                        when (toUnsignedByte(logRecord[4])) {
                            0 -> leakDetectors[i].isLeakage = false
                            2 -> leakDetectors[i].isLeakage = true
                        }

                        batteryState = String.format("%8s", Integer.toBinaryString(toUnsignedByte(logRecord[7]))).replace(' ', '0')
                        leakDetectors[i].isBatteryLow = batteryState[0] != '0'

                        leakDetectors[i].lastUpdateSecond = toUnsignedByte(logRecord[14])
                        leakDetectors[i].lastUpdateMinute = toUnsignedByte(logRecord[15])
                        leakDetectors[i].lastUpdateHour = toUnsignedByte(logRecord[16])
                        leakDetectors[i].lastUpdateWeekDay = toUnsignedByte(logRecord[17])
                        leakDetectors[i].lastUpdateDay = toUnsignedByte(logRecord[18])
                        leakDetectors[i].lastUpdateMonth = toUnsignedByte(logRecord[19])
                        leakDetectors[i].lastUpdateYear = toUnsignedByte(logRecord[20])

                        updatedLeakDetectors!![i] = 1

                        if (activity is HomeActivity) activity.updateAdapterItem(leakDetectors[i].adapterPosition)
                    }
                }
            }

            // get recent data for PL111 sensors
            for (i in lightSensors.indices) {
                if (updatedLightSensors!![i] == 0) {
                    if (lightSensors[i].channel == toUnsignedByte(logRecord[3]) && (toUnsignedByte(logRecord[4]) == COMMAND_OFF || toUnsignedByte(logRecord[4]) == COMMAND_ON)) {
                        when (toUnsignedByte(logRecord[4])) {
                            0 -> lightSensors[i].setDarkness(false)
                            2 -> lightSensors[i].setDarkness(true)
                        }

                        batteryState = String.format("%8s", Integer.toBinaryString(toUnsignedByte(logRecord[7]))).replace(' ', '0')
                        if (batteryState[0] == '1') lightSensors[i].setBatteryLow()

                        lightSensors[i].lastUpdateSecond = toUnsignedByte(logRecord[14])
                        lightSensors[i].lastUpdateMinute = toUnsignedByte(logRecord[15])
                        lightSensors[i].lastUpdateHour = toUnsignedByte(logRecord[16])
                        lightSensors[i].lastUpdateWeekDay = toUnsignedByte(logRecord[17])
                        lightSensors[i].lastUpdateDay = toUnsignedByte(logRecord[18])
                        lightSensors[i].lastUpdateMonth = toUnsignedByte(logRecord[19])
                        lightSensors[i].lastUpdateYear = toUnsignedByte(logRecord[20])

                        updatedLightSensors!![i] = 1

                        if (activity is HomeActivity) activity.updateAdapterItem(lightSensors[i].adapterPosition)
                    }
                }
            }

            // check of arrays of flags of recent log data updatePRF64
            toTheNextLogRecord = false

            for (updatedTemperatureSensor in updatedTemperatureSensors!!) {
                if (updatedTemperatureSensor == 0) {
                    toTheNextLogRecord = true
                    break
                }
            }
            if (toTheNextLogRecord) continue

            for (updatedHumidityTemperatureSensor in updatedHumidityTemperatureSensors!!) {
                if (updatedHumidityTemperatureSensor == 0) {
                    toTheNextLogRecord = true
                    break
                }
            }
            if (toTheNextLogRecord) continue

            for (updatedOpenCloseSensor in updatedOpenCloseSensors!!) {
                if (updatedOpenCloseSensor == 0) {
                    toTheNextLogRecord = true
                    break
                }
            }
            if (toTheNextLogRecord) continue

            for (updatedLeakDetector in updatedLeakDetectors!!) {
                if (updatedLeakDetector == 0) {
                    toTheNextLogRecord = true
                    break
                }
            }
            if (toTheNextLogRecord) continue

            for (updatedLightSensor in updatedLightSensors!!) {
                if (updatedLightSensor == 0) {
                    toTheNextLogRecord = true
                    break
                }
            }
            if (toTheNextLogRecord) continue
        }

        if (log.size > 21) {
            updatedTemperatureSensors = null
            updatedHumidityTemperatureSensors = null
            updatedOpenCloseSensors = null
            updatedLeakDetectors = null
            updatedLightSensors = null
        }
    }

    private fun getRxUnit(index: Int?): Any? {

        if (index == null) return null

        for (remoteController in remoteControllers) {
            if (remoteController.channel == index) {

                return remoteController
            }
        }

        for (temperatureSensor in temperatureSensors) {
            if (temperatureSensor.channel == index) {

                return temperatureSensor
            }
        }

        for (humidityTemperatureSensor in humidityTemperatureSensors) {
            if (humidityTemperatureSensor.channel == index) {

                return humidityTemperatureSensor
            }
        }

        for (motionSensor in motionSensors) {
            if (motionSensor.channel == index) {

                return motionSensor
            }
        }

        for (lightSensor in lightSensors) {
            if (lightSensor.channel == index) {

                return lightSensor
            }
        }

        for (openCloseSensor in openCloseSensors) {
            if (openCloseSensor.channel == index) {

                return openCloseSensor
            }
        }

        for (leakDetector in leakDetectors) {
            if (leakDetector.channel == index) {

                return leakDetector
            }
        }

        return null
    }

    private fun getFtxUnit(index: Int?): Any? {

        if (index == null) return null

        for (powerUnitF in powerUnitsF) {
            if (powerUnitF.index == index) {

                return powerUnitF
            }
        }

        for (powerSocketF in powerSocketsF) {
            if (powerSocketF.index == index) {

                return powerSocketF
            }
        }

        for (thermostat in thermostats) {
            if (thermostat.index == index) {

                return thermostat
            }
        }

        for (rolletUnit in rolletUnitsF) {
            if (rolletUnit.index == index) {

                return rolletUnit
            }
        }

        return null
    }
}
