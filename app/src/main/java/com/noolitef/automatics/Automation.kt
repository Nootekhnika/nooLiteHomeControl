package com.noolitef.automatics

import com.noolitef.Thermostat
import com.noolitef.ftx.PowerSocketF
import com.noolitef.ftx.PowerUnitF
import com.noolitef.presets.Preset
import com.noolitef.rx.*
import com.noolitef.tx.PowerUnit


class Automation(
        private var index: Int,
        private var name: String,
        private var type: Int,
        private var state: Int,
        private var triggerType: Int,
        private var triggerIndex: Int,
        private var triggerParameter: Int,
        private var command: IntArray,

        // for list item
        private var entityName: String,
        private var triggerName: String,
        private var eventName: String,
        private var unitAction: String,
        private var unitName: String,

        // for editing fragment
        private var trigger: Any?) {

    companion object {

        const val TYPE_HEATING = 0
        const val TYPE_COOLING = 1
        const val TYPE_DEHUMIDIFICATION = 2
        const val TYPE_HUMIDIFICATION = 3
        const val TYPE_EVENT = 4

        const val STATE_ON = 0
        const val STATE_OFF = 255

        const val TRIGGER_RX = 1
        // const val TRIGGER_FTX = 2
    }

    private var temporaryOn: Boolean = true
    private var unit: Any? = null


    fun getIndex(): Int {

        return index
    }

    fun getName(): String {

        return name
    }

    fun setName(name: String) {

        this.name = name
    }

    fun getType(): Int {

        return type
    }

    fun setType(type: Int) {

        this.type = type
    }

    fun getState(): Int {

        return state
    }

    fun setState(state: Int) {

        this.state = state
    }

    fun getTriggerType(): Int {

        return triggerType
    }

    fun getTriggerIndex(): Int {

        return triggerIndex
    }

    fun getTriggerParameter(): Int {

        return triggerParameter
    }

    fun setTriggerParameter(triggerParameter: Int) {

        this.triggerParameter = triggerParameter
    }

    fun getCommand(): IntArray {

        return command
    }

    fun setCommand(command: IntArray?) {

        command?.let { this.command = command }
    }


    fun getEntityName(): String {

        return entityName
    }

    fun getTriggerName(): String {

        return triggerName
    }

    fun getEventName(): String {

        return eventName
    }

    fun getUnitAction(): String {

        return unitAction
    }

    fun getUnitName(): String {

        return unitName
    }


    fun getTrigger(): Any? {

        return trigger
    }

    fun setTrigger(trigger: Any) {

        this.trigger = trigger

        when (trigger) {
            is RemoteController -> {
                type = TYPE_EVENT
                triggerIndex = trigger.channel
                triggerParameter = 2

                triggerName = trigger.name
            }
            is TemperatureSensor -> {
                type = TYPE_HEATING
                triggerIndex = trigger.channel
                triggerParameter = 22

                triggerName = trigger.name
            }
            is HumidityTemperatureSensor -> {
                type = TYPE_HUMIDIFICATION
                triggerIndex = trigger.channel
                triggerParameter = 50

                triggerName = trigger.name
            }
            is MotionSensor -> {
                type = TYPE_EVENT
                triggerIndex = trigger.channel
                triggerParameter = 25

                triggerName = trigger.name
            }
            is LightSensor -> {
                type = TYPE_EVENT
                triggerIndex = trigger.channel
                triggerParameter = 2

                triggerName = trigger.name
            }
            is OpenCloseSensor -> {
                type = TYPE_EVENT
                triggerIndex = trigger.channel
                triggerParameter = 2

                triggerName = trigger.name
            }
            is LeakDetector -> {
                type = TYPE_EVENT
                triggerIndex = trigger.channel
                triggerParameter = 2

                triggerName = trigger.name
            }
        }
        triggerType = 1
    }

    fun isTemporaryOnAllowed(): Boolean {
        return temporaryOn
    }

    fun allowTemporaryOn(temporaryOn: Boolean) {
        this.temporaryOn = temporaryOn
    }

    fun setUnit(unit: Any?) {

        this.unit = unit
        this.temporaryOn = true

        when (unit) {
            null -> {
                command[0] = 255
                command[1] = 255
                command[2] = 255
                command[3] = 255
                command[4] = 255
                command[5] = 255
                command[6] = 255
                command[7] = 255
                command[8] = 255
                command[9] = 255
                command[10] = 255
                command[11] = 255
                command[12] = 255
                command[13] = 255

                unitName = ""
            }
            is PowerUnit -> {
                command[0] = 0
                command[1] = 0
                command[2] = 0
                command[3] = unit.channel
                command[4] = 0
                command[5] = 0
                command[6] = 0
                command[7] = 0
                command[8] = 0
                command[9] = 0
                command[10] = 0
                command[11] = 0
                command[12] = 0
                command[13] = 0

                unitName = unit.name
            }
            is PowerUnitF -> {
                command[0] = 2
                command[1] = 9
                command[2] = 0
                command[3] = 0
                command[4] = 0
                command[5] = 0
                command[6] = 0
                command[7] = 0
                command[8] = 0
                command[9] = 0
                command[10] = unit.id.substring(0, 2).toInt(16)
                command[11] = unit.id.substring(2, 4).toInt(16)
                command[12] = unit.id.substring(4, 6).toInt(16)
                command[13] = unit.id.substring(6).toInt(16)

                unitName = unit.name
            }
            is PowerSocketF -> {
                command[0] = 2
                command[1] = 9
                command[2] = 0
                command[3] = 0
                command[4] = 0
                command[5] = 0
                command[6] = 0
                command[7] = 0
                command[8] = 0
                command[9] = 0
                command[10] = unit.id.substring(0, 2).toInt(16)
                command[11] = unit.id.substring(2, 4).toInt(16)
                command[12] = unit.id.substring(4, 6).toInt(16)
                command[13] = unit.id.substring(6).toInt(16)

                unitName = unit.name
            }
            is Thermostat -> {
                command[0] = 2
                command[1] = 9
                command[2] = 0
                command[3] = 0
                command[4] = 0
                command[5] = 0
                command[6] = 0
                command[7] = 0
                command[8] = 0
                command[9] = 0
                command[10] = unit.id.substring(0, 2).toInt(16)
                command[11] = unit.id.substring(2, 4).toInt(16)
                command[12] = unit.id.substring(4, 6).toInt(16)
                command[13] = unit.id.substring(6).toInt(16)

                unitName = unit.name

                temporaryOn = false
            }
            is Preset -> {
                command[0] = 254
                command[1] = unit.index
                command[2] = 255
                command[3] = 255
                command[4] = 255
                command[5] = 255
                command[6] = 255
                command[7] = 255
                command[8] = 255
                command[9] = 255
                command[10] = 255
                command[11] = 255
                command[12] = 255
                command[13] = 255

                unitName = unit.name
            }
        }
    }


    fun getAutoItem(): ByteArray {

        val autoItem = ByteArray(273) { -1 }

        autoItem[0] = type.toByte()
        autoItem[1] = state.toByte()
        autoItem[2] = triggerType.toByte()
        autoItem[3] = triggerIndex.toByte()
        autoItem[4] = triggerParameter.toByte()
        for (commandByte in 0..13) {
            autoItem[5 + commandByte] = command[commandByte].toByte()
        }

        return autoItem
    }
}
