package com.noolitef.automatics

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.noolitef.*
import com.noolitef.customview.UniversalSeekBar
import com.noolitef.ftx.PowerSocketF
import com.noolitef.ftx.PowerUnitF
import com.noolitef.presets.Preset
import com.noolitef.rx.*
import com.noolitef.settings.Settings
import com.noolitef.tx.PowerUnit
import okhttp3.*
import java.io.IOException
import java.nio.charset.Charset


// Создавать и редактировать объект автоматики из состояния представления (view), как в таймерах
// Убрать: newAutomation
// Добавить: createAutomationFromViewState(): Automation
class AutomationFragment : DialogFragment(), View.OnClickListener, IAutomationUnit {

    private lateinit var bCancel: Button
    private lateinit var bSave: Button
    private lateinit var etAutomationName: EditText
    private lateinit var tvTriggerType: TextView
    private lateinit var bTrigger: Button
    private lateinit var llCondition: LinearLayout
    private lateinit var sCondition: Spinner
    private lateinit var usbConditionParameter: UniversalSeekBar
    private lateinit var llEvent: LinearLayout
    private lateinit var sEvent: Spinner
    private lateinit var llUnit: LinearLayout
    private lateinit var bUnit: Button
    private lateinit var llAction: LinearLayout
    private lateinit var sAction: Spinner
    private lateinit var usbActionParameter: UniversalSeekBar
    private lateinit var llDelete: LinearLayout
    private lateinit var bDelete: Button
    private lateinit var rlProgress: RelativeLayout

    private lateinit var nooLitePRF64: PRF64
    private var automation: Automation? = null
    private var newAutomation: Automation? = null
    private var httpClient: OkHttpClient? = null
    private var iAutomationFragment: IAutomationFragment? = null


    // CONSTRUCTOR

    companion object {

        @JvmStatic
        fun getInstance(fragmentManager: FragmentManager?, nooLitePRF64: PRF64, automation: Automation?, httpClient: OkHttpClient, iAutomationFragment: IAutomationFragment): AutomationFragment {

            var fragment = fragmentManager?.findFragmentByTag(AutomationFragment::class.java.simpleName)

            if (fragment == null) {
                fragment = AutomationFragment()
            }
            val automationFragment = fragment as AutomationFragment
            automationFragment.setPRF64(nooLitePRF64)
            automationFragment.setAutomation(automation)
            automationFragment.setClient(httpClient)
            automationFragment.setCallback(iAutomationFragment)

            return fragment
        }
    }

    private fun setPRF64(nooLitePRF64: PRF64) {

        this.nooLitePRF64 = nooLitePRF64
    }

    private fun setAutomation(automation: Automation?) {

        this.automation = automation
        automation?.let {
            newAutomation = Automation(
                    automation.getIndex(),
                    automation.getName(),
                    automation.getType(),
                    automation.getState(),
                    automation.getTriggerType(),
                    automation.getTriggerIndex(),
                    automation.getTriggerParameter(),
                    automation.getCommand(),
                    automation.getEntityName(),
                    automation.getTriggerName(),
                    automation.getEventName(),
                    automation.getUnitAction(),
                    automation.getUnitName(),
                    automation.getTrigger()
            )
        } ?: run {
            newAutomation = Automation(
                    255,
                    "",
                    255,
                    255,
                    255,
                    255,
                    255,
                    IntArray(14) { 255 },
                    "",
                    "",
                    "",
                    "",
                    "",
                    null
            )
        }
    }

    private fun setClient(httpClient: OkHttpClient) {

        this.httpClient = httpClient
    }

    private fun setCallback(iAutomationFragment: IAutomationFragment) {

        this.iAutomationFragment = iAutomationFragment
    }


    // LIFECYCLE

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, 0)

        retainInstance = true
        isCancelable = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return initView(
                inflater.inflate(
                        R.layout.fragment_automation, container, false
                )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        setupViewState(automation)
    }

    override fun onStart() {

        super.onStart()

        setupWindow()
    }

    override fun onDestroyView() {

        val dialog = dialog

        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }

        super.onDestroyView()
    }

    // crutch
    override fun onDestroy() {

        val fragment = fragmentManager?.findFragmentByTag(AutomationUnitsFragment::class.java.simpleName)

        if (fragment != null) {
            if (fragment.isVisible) {
                val fragmentTransaction = fragmentManager?.beginTransaction()
                fragmentTransaction?.remove(fragment)?.commitAllowingStateLoss()
            }
        }

        super.onDestroy()
    }


    // INIT VIEW

    private fun initView(view: View): View {

        // toolbar
        bCancel = view.findViewById(R.id.fragment_automation_button_cancel)
        bCancel.setOnClickListener(this)
        bSave = view.findViewById(R.id.fragment_automation_button_save)
        bSave.setOnClickListener(this)
        // name
        etAutomationName = view.findViewById(R.id.fragment_automation_edit_name)
        // trigger
        tvTriggerType = view.findViewById(R.id.fragment_automation_text_trigger_type)
        bTrigger = view.findViewById(R.id.fragment_automation_button_trigger)
        bTrigger.setOnClickListener(this)
        // condition
        llCondition = view.findViewById(R.id.fragment_automation_layout_condition)
        sCondition = view.findViewById(R.id.fragment_automation_spinner_condition)
        // condition: parameter
        usbConditionParameter = view.findViewById(R.id.fragment_automation_universal_seek_bar_condition_parameter)
        // condition: event
        llEvent = view.findViewById(R.id.fragment_automation_layout_event)
        sEvent = view.findViewById(R.id.fragment_automation_spinner_event)
        // unit
        llUnit = view.findViewById(R.id.fragment_automation_layout_unit)
        bUnit = view.findViewById(R.id.fragment_automation_button_unit)
        bUnit.setOnClickListener(this)
        // action
        llAction = view.findViewById(R.id.fragment_automation_layout_action)
        sAction = view.findViewById(R.id.fragment_automation_spinner_action)
        // action parameter
        usbActionParameter = view.findViewById(R.id.fragment_automation_universal_seek_bar_action_parameter)
        // footer
        llDelete = view.findViewById(R.id.fragment_automation_layout_delete)
        bDelete = view.findViewById(R.id.fragment_automation_button_delete)
        bDelete.setOnClickListener(this)
        // progress
        rlProgress = view.findViewById(R.id.fragment_automation_layout_progress)

        return view
    }

    private fun setupViewState(automation: Automation?) {

        if (automation == null) {
            // new
            etAutomationName.setText("")
            tvTriggerType.text = "Инициатор"
            bTrigger.text = "выберите устройство"
            llCondition.visibility = View.GONE
            llEvent.visibility = View.GONE
            llUnit.visibility = View.GONE
            llAction.visibility = View.GONE
            llDelete.visibility = View.GONE
        } else {
            // exist
            // name
            etAutomationName.setText(automation.getName())
            // trigger
            initTrigger(automation.getTrigger(), automation.getTriggerName())
            // condition
            initCondition(automation.getType(), automation.getTrigger())
            // condition: parameter
            initConditionParameter(automation.getType(), automation.getTriggerParameter())
            // condition: event
            initEvent(automation.getType(), automation.getTrigger(), automation.getTriggerParameter())
            // unit
            initUnit(automation.getUnitName(), automation.getCommand())
            // action
            initAction(automation.getTrigger(), automation.isTemporaryOnAllowed(), automation.getCommand())
            // action parameter
            initActionParameter(automation.getTrigger(), automation.getCommand())
            // footer
            if (automation.getIndex() != 255) {
                llDelete.visibility = View.VISIBLE
            }
        }
    }

    private fun initTrigger(trigger: Any?, triggerName: String) {

        when (trigger) {
            is RemoteController -> bTrigger.text = "Пульт: %s".format(triggerName)
            is TemperatureSensor, is HumidityTemperatureSensor, is MotionSensor, is LightSensor, is OpenCloseSensor, is LeakDetector ->
                bTrigger.text = "Датчик: %s".format(triggerName)
        }
    }

    private fun initCondition(automationType: Int, trigger: Any?) {

        var conditions: Array<String>? = null
        when (trigger) {
            is RemoteController, is MotionSensor, is LightSensor, is OpenCloseSensor, is LeakDetector -> {
                llCondition.visibility = View.GONE
            }
            is HumidityTemperatureSensor -> {
                conditions = activity?.resources?.getStringArray(R.array.automationHumidityTemperatureTriggerConditions)
                llCondition.visibility = View.VISIBLE
            }
            is TemperatureSensor -> {
                conditions = activity?.resources?.getStringArray(R.array.automationTemperatureTriggerConditions)
                llCondition.visibility = View.VISIBLE
            }
            null -> {
                llCondition.visibility = View.GONE
            }
        }
        if (conditions == null) {
            conditions = Array(1) { "" }
        }

        val conditionsAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, conditions)
        sCondition.adapter = conditionsAdapter
        try {
            // see strings.xml -> automation[Humidity]TemperatureTriggerConditions
            when (automationType) {
                Automation.TYPE_HEATING -> sCondition.setSelection(0)
                Automation.TYPE_COOLING -> sCondition.setSelection(1)
                Automation.TYPE_DEHUMIDIFICATION -> sCondition.setSelection(2)
                Automation.TYPE_HUMIDIFICATION -> sCondition.setSelection(3)
                else -> sCondition.setSelection(0)
            }
        } catch (e: Exception) {
            sCondition.setSelection(0)
        }
        sCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (newAutomation?.getTrigger()) {
                    is TemperatureSensor -> {
                        when (position) {
                            0 -> newAutomation?.setType(Automation.TYPE_HEATING)
                            1 -> newAutomation?.setType(Automation.TYPE_COOLING)
                        }
                    }
                    is HumidityTemperatureSensor -> {
                        when (position) {
                            0 -> newAutomation?.setType(Automation.TYPE_HEATING)
                            1 -> newAutomation?.setType(Automation.TYPE_COOLING)
                            2 -> newAutomation?.setType(Automation.TYPE_DEHUMIDIFICATION)
                            3 -> newAutomation?.setType(Automation.TYPE_HUMIDIFICATION)
                        }
                    }
                }
                newAutomation?.let {
                    initConditionParameter(it.getType(), usbConditionParameter.progress)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun initConditionParameter(automationType: Int, triggerParameter: Int) {

        when (automationType) {
            Automation.TYPE_HEATING, Automation.TYPE_COOLING -> {
                usbConditionParameter.setMeasure("°C")
                usbConditionParameter.max = 50
                usbConditionParameter.progress = triggerParameter
                usbConditionParameter.visibility = View.VISIBLE
            }
            Automation.TYPE_DEHUMIDIFICATION, Automation.TYPE_HUMIDIFICATION -> {
                usbConditionParameter.setMeasure("%")
                usbConditionParameter.max = 100
                usbConditionParameter.progress = triggerParameter
                usbConditionParameter.visibility = View.VISIBLE
            }
            Automation.TYPE_EVENT -> {
                usbConditionParameter.visibility = View.GONE
            }
        }

        usbConditionParameter.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                newAutomation?.setTriggerParameter(seekBar?.progress ?: 0)
            }

        })
    }

    private fun initEvent(automationType: Int, trigger: Any?, triggerParameter: Int) {

        when (automationType) {
            Automation.TYPE_HEATING, Automation.TYPE_COOLING, Automation.TYPE_DEHUMIDIFICATION, Automation.TYPE_HUMIDIFICATION -> {
                llEvent.visibility = View.GONE
            }
            Automation.TYPE_EVENT -> {
                var events: Array<String>? = null
                when (trigger) {
                    is RemoteController -> {
                        events = activity?.resources?.getStringArray(R.array.automationRemoteTriggerEvents)
                    }
                    is MotionSensor -> {
                        events = activity?.resources?.getStringArray(R.array.automationMotionTriggerEvents)
                    }
                    is LightSensor -> {
                        events = activity?.resources?.getStringArray(R.array.automationLightTriggerEvents)
                    }
                    is OpenCloseSensor -> {
                        events = activity?.resources?.getStringArray(R.array.automationOpenCloseTriggerEvents)
                    }
                    is LeakDetector -> {
                        events = activity?.resources?.getStringArray(R.array.automationLeakTriggerEvents)
                    }
                }
                if (events == null) {
                    events = Array(1) { "" }
                }

                val eventsAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, events)
                sEvent.adapter = eventsAdapter
                try {
                    // see strings.xml -> automation[Remote,Motion,Light,OpenClose,Leak]TriggerEvents
                    when (trigger) {
                        is RemoteController -> {
                            when (triggerParameter) {
                                // CMD: 0 -> OFF
                                0 -> sEvent.setSelection(0)
                                // CMD: 2 -> ON
                                2 -> sEvent.setSelection(1)
                                // CMD: 4 -> SWITCH
                                4 -> sEvent.setSelection(2)
                                else -> sEvent.setSelection(0)
                            }
                        }
                        is MotionSensor -> {
                            // CMD: 25 -> TEMPORARY_ON (MOTION)
                            if (triggerParameter == 25) {
                                sEvent.setSelection(0)
                            } else {
                                sEvent.setSelection(0)
                            }
                        }
                        is LightSensor -> {
                            when (triggerParameter) {
                                // CMD: 0 -> LIGHT
                                0 -> sEvent.setSelection(1)
                                // CMD: 2 -> DARK
                                2 -> sEvent.setSelection(0)
                                else -> sEvent.setSelection(0)
                            }
                        }
                        is OpenCloseSensor -> {
                            when (triggerParameter) {
                                // CMD: 0 -> CLOSE
                                0 -> sEvent.setSelection(0)
                                // CMD: 2 -> OPEN
                                2 -> sEvent.setSelection(1)
                                else -> sEvent.setSelection(0)
                            }
                        }
                        is LeakDetector -> {
                            when (triggerParameter) {
                                // CMD: 0 -> DRY
                                0 -> sEvent.setSelection(0)
                                // CMD: 2 -> WET
                                2 -> sEvent.setSelection(1)
                                else -> sEvent.setSelection(0)
                            }
                        }
                        else -> sEvent.setSelection(0)
                    }
                } catch (e: Exception) {
                    sEvent.setSelection(0)
                }
                sEvent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        when (newAutomation?.getTrigger()) {
                            is RemoteController -> {
                                when (position) {
                                    0 -> newAutomation?.setTriggerParameter(0)
                                    1 -> newAutomation?.setTriggerParameter(2)
                                    2 -> newAutomation?.setTriggerParameter(4)
                                }
                            }
                            is MotionSensor -> {
                                when (position) {
                                    0 -> newAutomation?.setTriggerParameter(25)
                                }
                            }
                            is LightSensor -> {
                                when (position) {
                                    0 -> newAutomation?.setTriggerParameter(2)
                                    1 -> newAutomation?.setTriggerParameter(0)
                                }
                            }
                            is OpenCloseSensor -> {
                                when (position) {
                                    0 -> newAutomation?.setTriggerParameter(0)
                                    1 -> newAutomation?.setTriggerParameter(2)
                                }
                            }
                            is LeakDetector -> {
                                when (position) {
                                    0 -> newAutomation?.setTriggerParameter(0)
                                    1 -> newAutomation?.setTriggerParameter(2)
                                }
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }

                llEvent.visibility = View.VISIBLE
            }
        }
    }

    private fun initUnit(unitName: String, command: IntArray) {

        if (command[0] == 255) {
            bUnit.text = "выберите исполнителя"
        } else {
            if (command[0] == 254) {
                bUnit.text = "Сценарий: %s".format(unitName)
            } else {
                bUnit.text = "Устройство: %s".format(unitName)
            }
        }
        llUnit.visibility = View.VISIBLE
    }

    private fun initAction(trigger: Any?, temporaryOn: Boolean, command: IntArray) {

        when (trigger) {
            is TemperatureSensor, is HumidityTemperatureSensor -> {
                llAction.visibility = View.GONE

                return
            }
        }

        if (command[0] > 253) {
            llAction.visibility = View.GONE

            return
        }

        var actions: Array<String>?
        actions = if (temporaryOn) {
            activity?.resources?.getStringArray(R.array.automationActionsRelay)
        } else {
            activity?.resources?.getStringArray(R.array.automationActions)
        }
        if (actions == null) {
            actions = Array(1) { "" }
        }
        val actionsAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, actions)
        sAction.adapter = actionsAdapter
        try {
            // see strings.xml -> automationActions
            when (command[4]) {
                // CMD: 0 -> OFF
                0 -> sAction.setSelection(0)
                // CMD: 2 -> ON
                2 -> sAction.setSelection(1)
                // CMD: 25 -> TEMPORARY_ON
                25 -> sAction.setSelection(2)
                else -> sAction.setSelection(0)
            }
        } catch (e: Exception) {
            sAction.setSelection(0)
        }
        sAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val newCommand = newAutomation?.getCommand()
                when (position) {
                    0 -> {
                        newCommand?.set(4, 0)
                        newCommand?.set(5, 0)
                        newCommand?.set(6, 0)
                        newAutomation?.setCommand(newCommand)
                    }
                    1 -> {
                        newCommand?.set(4, 2)
                        newCommand?.set(5, 0)
                        newCommand?.set(6, 0)
                        newAutomation?.setCommand(newCommand)
                    }
                    2 -> {
                        newCommand?.set(4, 25)
                        newCommand?.set(5, 5)
                        newAutomation?.setCommand(newCommand)
                    }
                }
                initActionParameter(newAutomation?.getTrigger(), newAutomation?.getCommand())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        llAction.visibility = View.VISIBLE
    }

    private fun initActionParameter(trigger: Any?, command: IntArray?) {

        when (trigger) {
            is TemperatureSensor, is HumidityTemperatureSensor -> {
                usbActionParameter.visibility = View.GONE

                return
            }
        }

        if ((command?.get(0) ?: 0) > 253) {
            usbActionParameter.visibility = View.GONE

            return
        }

        usbActionParameter.max = 20
        usbActionParameter.setMeasure(" мин")
        usbActionParameter.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                var time: Int = seekBar?.progress ?: 0
                if (time == 0) {
                    time = 1
                    seekBar?.progress = time
                }
                val parameter = (time / 5.0 * 60.0 + .5).toInt()
                newAutomation?.getCommand()?.set(6, parameter)
            }
        })

        if ((command?.get(4) ?: 0) == 25) {
            var time: Int = ((command?.get(6) ?: 0) * 5.0 / 60.0 + .5).toInt()
            if (time == 0) {
                time = 1
                newAutomation?.getCommand()?.set(6, 12)  // 1 мин
            }
            usbActionParameter.progress = time

            usbActionParameter.visibility = View.VISIBLE
        } else {
            usbActionParameter.visibility = View.GONE
        }
    }

    private fun setupWindow() {

        val window = dialog.window

        val dialogParams = window?.attributes
        dialogParams?.dimAmount = 0.75f
        window?.attributes = dialogParams
        window?.setBackgroundDrawableResource(R.color.transparent)

        val display = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(display)
        val displayWidth = display.widthPixels
        val displayHeight = display.heightPixels
        if (displayWidth < displayHeight) {
            window?.setLayout(displayWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        } else {
            window?.setLayout(displayHeight, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }


    // INTERFACES

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.fragment_automation_button_cancel -> dismiss()
            R.id.fragment_automation_button_save -> {
                if (etAutomationName.text.isEmpty()) {
                    showToast("Назовите автоматику")
                    return
                }
                if (newAutomation?.getTrigger() == null) {
                    showToast("Выберите инициатора")
                    return
                }
                if (newAutomation?.getCommand()?.get(0) == 255) {
                    showToast("Выберите исполнителя")
                    return
                }

                Thread(Runnable {
                    setUpdating(true)
                    newAutomation?.setName(etAutomationName.text.toString())
                    when (newAutomation?.getType()) {
                        Automation.TYPE_HEATING, Automation.TYPE_COOLING, Automation.TYPE_HUMIDIFICATION, Automation.TYPE_DEHUMIDIFICATION -> {
                            newAutomation?.setTriggerParameter(usbConditionParameter.progress)
                        }
                    }
                    newAutomation!!.setState(Automation.STATE_ON)
                    if (newAutomation?.getIndex() == 255) {
                        addAutomation(newAutomation)
                    } else {
                        editAutomation(newAutomation)
                    }
                }).start()
            }
            R.id.fragment_automation_button_trigger -> {
                val automationUnitsFragment: AutomationUnitsFragment = AutomationUnitsFragment.getInstance(
                        childFragmentManager, nooLitePRF64,
                        showTriggerList = true,
                        showPresets = false,
                        showPowerUnits = false,
                        iAutomationUnit = this
                )
                if (automationUnitsFragment.isAdded) return
                automationUnitsFragment.show(fragmentManager, AutomationUnitsFragment::class.java.simpleName)
            }
            R.id.fragment_automation_button_unit -> {
                val automationUnitsFragment: AutomationUnitsFragment = AutomationUnitsFragment.getInstance(
                        childFragmentManager, nooLitePRF64,
                        showTriggerList = false,
                        showPresets = newAutomation?.getType() == Automation.TYPE_EVENT,
                        showPowerUnits = newAutomation?.getType() != Automation.TYPE_EVENT,
                        iAutomationUnit = this
                )
                if (automationUnitsFragment.isAdded) return
                automationUnitsFragment.show(fragmentManager, AutomationUnitsFragment::class.java.simpleName)
            }
            R.id.fragment_automation_button_delete -> {
                var confirmDialog = childFragmentManager.findFragmentByTag("CONFIRM_DIALOG") as ConfirmDialog?
                if (confirmDialog == null) {
                    confirmDialog = ConfirmDialog()
                    confirmDialog.setTitle("Удаление автоматики")
                    confirmDialog.setMessage("Удалить автоматику ''" + etAutomationName.text.toString() + "''?")
                    confirmDialog.setConfirmDialogListener(object : ConfirmDialogListener {
                        override fun onAccept() {
                            Thread(Runnable {
                                setUpdating(true)
                                newAutomation!!.setState(Automation.STATE_OFF)
                                deleteAutomation(newAutomation)
                            }).start()
                        }

                        override fun onDecline() {
                        }
                    })
                }
                if (confirmDialog.isAdded) return
                childFragmentManager.beginTransaction().add(confirmDialog, "CONFIRM_DIALOG").show(confirmDialog).commit()
            }
        }
    }

    private fun hideSoftKeyboard() {

        try {
            val inputMethodManager: InputMethodManager? = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view: View? = dialog.currentFocus

            inputMethodManager?.hideSoftInputFromWindow(view?.windowToken, 0)
        } catch (e: Exception) {
        }
    }

    private fun setUpdating(visible: Boolean) {

        if (!isAdded) return

        activity?.runOnUiThread {
            hideSoftKeyboard()
            showProgress(visible)
        }
    }

    private fun showProgress(visible: Boolean) {

        if (!isAdded) return

        if (visible) {
            rlProgress.visibility = View.VISIBLE
        } else {
            rlProgress.visibility = View.GONE
        }
    }

    private fun showToast(message: String?) {

        if (!isAdded) return

        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }


    // CALLBACK

    override fun setUnit(unit: Any) {

        newAutomation?.setName(etAutomationName.text.toString())

        when (unit) {
            is RemoteController, is TemperatureSensor, is HumidityTemperatureSensor, is MotionSensor, is LightSensor, is OpenCloseSensor, is LeakDetector -> {
                newAutomation?.setTrigger(unit)
                newAutomation?.setUnit(null)
                setupViewState(newAutomation)
            }
            is PowerUnit, is PowerUnitF, is PowerSocketF, is Thermostat, is Preset -> {
                newAutomation?.setUnit(unit)
                setupViewState(newAutomation)
            }
        }
    }


    // MODEL

    private fun addAutomation(automation: Automation?) {

        try {
            val auto: ByteArray = nooLitePRF64.getAuto()

            var automationIndex = 0
            var autoStep: Int
            var autoByte = 4102

            do {
                if (auto[autoByte].toInt() == -1) {
                    val automationName: ByteArray? = automation?.getName()?.toByteArray(Charset.forName("cp1251"))

                    automationName?.let {
                        for (autoNameByte in 0..31) {
                            if (autoNameByte < automationName.size) {
                                auto[6 + (32 * automationIndex) + autoNameByte] = automationName[autoNameByte]
                            } else {
                                auto[6 + (32 * automationIndex) + autoNameByte] = 0
                            }
                        }
                    }

                    val autoItem: ByteArray? = automation?.getAutoItem()

                    autoItem?.let {
                        for (autoItemByte: Int in autoItem.indices) {
                            auto[autoByte + autoItemByte] = autoItem[autoItemByte]
                        }
                    }

                    postAuto(auto, "Автоматика ''${automation?.getName()}'' добавлена")

                    return
                }

                automationIndex++
                autoStep =
                        if (autoByte == 7924) 274
                        else 273
                autoByte += autoStep
            } while (autoByte < 12293)

            showToast("Можно сохранить только 30 автоматик")

        } catch (e: Exception) {
            showToast(getStringByResourceId(R.string.some_thing_went_wrong))
            setUpdating(false)
        }
    }

    private fun editAutomation(automation: Automation?) {

        try {
            val auto: ByteArray = nooLitePRF64.getAuto()

            var automationIndex = 0
            var autoStep: Int
            var autoByte = 4102

            do {
                if (automationIndex == automation?.getIndex()) {
                    val automationName: ByteArray? = automation.getName().toByteArray(Charset.forName("cp1251"))

                    automationName?.let {
                        for (autoNameByte in 0..31) {
                            if (autoNameByte < automationName.size) {
                                auto[6 + (32 * automationIndex) + autoNameByte] = automationName[autoNameByte]
                            } else {
                                auto[6 + (32 * automationIndex) + autoNameByte] = 0
                            }
                        }
                    }

                    val autoItem: ByteArray? = automation.getAutoItem()

                    autoItem?.let {
                        for (autoItemByte: Int in autoItem.indices) {
                            auto[autoByte + autoItemByte] = autoItem[autoItemByte]
                        }
                    }

                    postAuto(auto, "Автоматика ''${automation.getName()}'' сохранена")

                    return
                }

                automationIndex++
                autoStep =
                        if (autoByte == 7924) 274
                        else 273
                autoByte += autoStep
            } while (autoByte < 12293)

        } catch (e: Exception) {
            showToast(getStringByResourceId(R.string.some_thing_went_wrong))
            setUpdating(false)
        }
    }

    private fun deleteAutomation(automation: Automation?) {

        try {
            val auto: ByteArray = nooLitePRF64.getAuto()

            var automationIndex = 0
            var autoStep: Int
            var autoByte = 4102

            do {
                if (automationIndex == automation?.getIndex()) {
                    for (autoNameByte in 0..31) {
                        auto[6 + (32 * automationIndex) + autoNameByte] = -1
                    }

                    for (autoItemByte: Int in 0..272) {
                        auto[autoByte + autoItemByte] = -1
                    }

                    postAuto(auto, "Автоматика ''${automation.getName()}'' удалена")

                    return
                }

                automationIndex++
                autoStep =
                        if (autoByte == 7924) 274
                        else 273
                autoByte += autoStep
            } while (autoByte < 12293)

        } catch (e: Exception) {
            showToast(getStringByResourceId(R.string.some_thing_went_wrong))
            setUpdating(false)
        }
    }

    private fun postAuto(auto: ByteArray, message: String) {

        val body = "\r\n\r\nContent-Disposition: form-data; name=\"auto\"; filename=\"auto.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .plus(String(auto, Charset.forName("cp1251")))
                .plus("\r\n\r\n\r\n")
        val request = Request.Builder()
                .url(Settings.URL() + "sett_eic.htm")
                .post(RequestBody.create(null, body.toByteArray(Charset.forName("cp1251"))))
                .build()
        val call = httpClient?.newCall(request)

        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                call.cancel()
                showToast(getStringByResourceId(R.string.no_connection))
                setUpdating(false)
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful) {
                    showToast(message)
                    iAutomationFragment?.onDismiss(true)
                    dismiss()
                } else {
                    showToast("%s %s".format(getStringByResourceId(R.string.connection_error), response.code()))
                }
                call.cancel()
                setUpdating(false)
            }
        })
    }


    private fun getStringByResourceId(resId: Int): String {

        var string = ""

        if (isAdded) {
            string = getString(resId)
        }

        return string
    }
}
