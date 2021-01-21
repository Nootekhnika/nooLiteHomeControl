package com.noolitef.ftx

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import com.appyvet.materialrangebar.RangeBar
import com.noolitef.GUIBlockFragment
import com.noolitef.HomeActivity
import com.noolitef.NooLiteF
import com.noolitef.R
import com.noolitef.settings.Settings
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.ConnectException
import java.net.SocketException
import java.util.*

class FTXUnitSettingsFragment : DialogFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, RangeBar.OnRangeBarChangeListener {
    companion object {
        private const val SEND_STATE = "82"
    }

    // DEPENDENCIES

    // main
    private lateinit var homeActivity: HomeActivity
    private lateinit var powerUnitF: PowerUnitF

    // http client
    private lateinit var client: OkHttpClient
    private lateinit var request: Request
    private lateinit var call: Call
    private lateinit var response: Response
    private lateinit var sResponse: String

    // DATA

    // main settings byte
    private var mainSettingsByteString = StringBuilder("00000000")
    private var additionSettingsByteString = StringBuilder("00000000")

    // switch on level
    private var currentSwitchOnLevel = -1
    private var newSwitchOnLevel = -1

    // dimming
    private var currentDimmingState = -1
    private var newDimmingState = -1
    private var currentLowerDimmingLevel = 0
    private var newLowerDimmingLevel = 0
    private var minLowerDimmingLevel = 0
    private var currentUpperDimmingLevel = 100
    private var newUpperDimmingLevel = 100
    private var minUpperDimmingLevel = 100

    // memory state
    private var currentPowerUpState = -1
    private var newPowerUpState = -1
    private var currentRememberState = -1
    private var newRememberState = -1

    // switch on brightness
    private var currentSwitchOnBrightness = -1
    private var newSwitchOnBrightness = -1

    // external input state
    private var currentExternalInputState = -1
    private var newExternalInputState = -1

    // retransmission
    private var currentRetransmissionState = -1
    private var newRetransmissionState = -1
    private var currentRetransmissionDelay = 0
    private var newRetransmissionDelay = 0

    // locks state
    private var currentNooLiteState = -1
    private var newNooLiteState = -1
    private var currentTemporaryOnState = -1
    private var newTemporaryOnState = -1

    // transmitter sensitivity
    private var currentTransmitterSensitivity = -1
    private var newTransmitterSensitivity = -1

    // info
    private var deviceType = -1
    private var firmwareVersion = -1
    private var freeNooLiteMemoryCells = -1
    private var freeNooLiteFMemoryCells = -1

    // VIEW

    // title
    private lateinit var buttonBack: Button
    private lateinit var buttonSave: Button

    // switch on level
    private lateinit var progressSwitchOnLevel: ProgressBar
    private lateinit var imageSwitchOnLevelWarning: ImageView
    private lateinit var seekSwitchOnLevel: SeekBar

    // dimming
    private lateinit var textDimmingState: TextView
    private lateinit var progressDimmingState: ProgressBar
    private lateinit var imageDimmingStateWarning: ImageView
    private lateinit var switchDimmingState: SwitchCompat
    private lateinit var layoutDimmingRange: LinearLayout
    private lateinit var rangeDimming: RangeBar
    private lateinit var layoutDimmingRangeAlternative: LinearLayout
    private lateinit var textDimmingLowerLevel: TextView
    private lateinit var textDimmingUpperLevel: TextView

    // memory state
    private lateinit var progressMemoryState: ProgressBar
    private lateinit var layoutMemoryState: LinearLayout
    private lateinit var checkPowerUpState: AppCompatCheckBox
    private lateinit var checkRememberState: AppCompatCheckBox
    private lateinit var imageMemoryStateWarning: ImageView

    // switch on brightness
    private lateinit var progressSwitchOnBrightness: ProgressBar
    private lateinit var imageSwitchOnBrightnessWarning: ImageView
    private lateinit var layoutSwitchOnBrightness: LinearLayout
    private lateinit var seekSwitchOnBrightness: SeekBar

    // external input state
    private lateinit var progressExternalInputState: ProgressBar
    private lateinit var imageExternalInputStateWarning: ImageView
    private lateinit var groupExternalInputState: RadioGroup
    private lateinit var radioExternalInputStateNone: RadioButton
    private lateinit var radioExternalInputStateSwitchOff: RadioButton
    private lateinit var radioExternalInputStateSwitchOn: RadioButton
    private lateinit var radioExternalInputStateSwitch: RadioButton

    // retransmission
    private lateinit var textRetransmissionState: TextView
    private lateinit var progressRetransmissionState: ProgressBar
    private lateinit var imageRetransmissionStateWarning: ImageView
    private lateinit var switchRetransmissionState: SwitchCompat
    private lateinit var layoutRetransmissionDelay: LinearLayout
    private lateinit var seekRetransmissionDelay: SeekBar

    // locks state
    private lateinit var progressLocksConfig: ProgressBar
    private lateinit var layoutLocksConfig: LinearLayout
    private lateinit var checkNooLiteState: AppCompatCheckBox
    private lateinit var checkTemporaryOnState: AppCompatCheckBox
    private lateinit var imageLocksConfigWarning: ImageView

    // transmitter sensitivity
    private lateinit var layoutTransmitterSettings: LinearLayout
    private lateinit var layoutTransmitterSensitivity: LinearLayout
    private lateinit var progressTransmitterSensitivity: ProgressBar
    private lateinit var groupTransmitterSensitivity: RadioGroup
    private lateinit var radioTransmitterSensitivity0: RadioButton
    private lateinit var radioTransmitterSensitivity6: RadioButton
    private lateinit var radioTransmitterSensitivity12: RadioButton
    private lateinit var radioTransmitterSensitivity18: RadioButton
    private lateinit var imageTransmitterSensitivity: ImageView

    // info
    private lateinit var textDeviceType: TextView
    private lateinit var textFirmwareVersion: TextView
    private lateinit var textFreeNooLiteMemoryCells: TextView
    private lateinit var textFreeNooLiteFMemoryCells: TextView

    // loading fragment
    private var guiBlockFragment: GUIBlockFragment? = null

    // VIEW

    fun instance(client: OkHttpClient, powerUnitF: PowerUnitF) {
        this.client = client
        this.powerUnitF = powerUnitF
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        homeActivity = activity as HomeActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, 0)

        retainInstance = true
        isCancelable = true
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.setCanceledOnTouchOutside(true)

        return initView(inflater.inflate(R.layout.fragment_settings_unit_ftx, null))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSettings()
    }

    override fun onStart() {
        super.onStart()

        setupFragment()
    }

    override fun onStop() {
        super.onStop()

        call.cancel()
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
            R.id.fragment_settings_unit_ftx_button_back -> dismiss()
            R.id.fragment_settings_unit_ftx_button_save -> postSettings()
        }
    }

    // VIEW:HANDLING

    override fun onCheckedChanged(view: CompoundButton?, checked: Boolean) {
        when (view?.id) {
            R.id.fragment_settings_unit_ftx_switch_dimming_state -> {
                newDimmingState =
                        if (checked) {
                            1
                        } else {
                            0
                        }
                showDimmingSettings()
            }
            R.id.fragment_settings_unit_ftx_checkbox_power_up_state -> {
                newPowerUpState =
                        if (checked) {
                            checkRememberState.isChecked = false
                            1
                        } else {
                            0
                        }
            }
            R.id.fragment_settings_unit_ftx_checkbox_remember_state -> {
                newRememberState =
                        if (checked) {
                            checkPowerUpState.isChecked = false
                            1
                        } else {
                            0
                        }
            }
            R.id.fragment_settings_unit_ftx_radio_external_input_state_none -> {
                if (checked) newExternalInputState = 3
            }
            R.id.fragment_settings_unit_ftx_radio_external_input_state_switch_off -> {
                if (checked) newExternalInputState = 0
            }
            R.id.fragment_settings_unit_ftx_radio_external_input_state_switch_on -> {
                if (checked) newExternalInputState = 2
            }
            R.id.fragment_settings_unit_ftx_radio_external_input_state_switch -> {
                if (checked) newExternalInputState = 1
            }
            R.id.fragment_settings_unit_ftx_switch_retransmission_state -> {
                newRetransmissionState =
                        if (checked) {
                            1
                        } else {
                            0
                        }
                showRetransmissionSettings()
            }
            R.id.fragment_settings_unit_ftx_checkbox_noolite_state -> {
                newNooLiteState =
                        if (checked) {
                            1
                        } else {
                            0
                        }
            }
            R.id.fragment_settings_unit_ftx_checkbox_temporary_on_state -> {
                newTemporaryOnState =
                        if (checked) {
                            1
                        } else {
                            0
                        }
            }
            R.id.fragment_settings_unit_ftx_radio_transmitter_sensitivity_0 -> {
                if (checked) newTransmitterSensitivity = 0
            }
            R.id.fragment_settings_unit_ftx_radio_transmitter_sensitivity_6 -> {
                if (checked) newTransmitterSensitivity = 1
            }
            R.id.fragment_settings_unit_ftx_radio_transmitter_sensitivity_12 -> {
                if (checked) newTransmitterSensitivity = 2
            }
            R.id.fragment_settings_unit_ftx_radio_transmitter_sensitivity_18 -> {
                if (checked) newTransmitterSensitivity = 3
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar?.id) {
            R.id.fragment_settings_unit_ftx_seek_switch_on_level -> {
                newSwitchOnLevel = if (powerUnitF is PowerUnitFA) {
                    progress
                } else {
                    (progress * 2.55 + .5).toInt()
                }
                if (newSwitchOnLevel == 0) {
                    newSwitchOnLevel = if (powerUnitF is PowerUnitFA) {
                        1
                    } else {
                        3
                    }
                    seekSwitchOnLevel.progress = 1
                }

//                if (newSwitchOnLevel < newLowerDimmingLevel) {
//                    newLowerDimmingLevel = newSwitchOnLevel
//                    if (powerUnitF is PowerUnitFA) {
//                        rangeDimming.setRangePinsByIndices(newLowerDimmingLevel, newUpperDimmingLevel)
//                    } else {
//                        rangeDimming.setRangePinsByIndices((newLowerDimmingLevel / 255.0 * 100 + .5).toInt(), (newUpperDimmingLevel / 255.0 * 100 + .5).toInt())
//                    }
//                }
//                if (newSwitchOnLevel > newUpperDimmingLevel) {
//                    newUpperDimmingLevel = newSwitchOnLevel
//                    if (powerUnitF is PowerUnitFA) {
//                        rangeDimming.setRangePinsByIndices(newLowerDimmingLevel, newUpperDimmingLevel)
//                    } else {
//                        rangeDimming.setRangePinsByIndices((newLowerDimmingLevel / 255.0 * 100 + .5).toInt(), (newUpperDimmingLevel / 255.0 * 100 + .5).toInt())
//                    }
//                }
            }
            R.id.fragment_settings_unit_ftx_seek_switch_on_brightness -> {
                newSwitchOnBrightness = if (powerUnitF is PowerUnitFA) {
                    progress
                } else {
                    (progress * 2.55 + .5).toInt()
                }
            }
            R.id.fragment_settings_unit_ftx_seek_retransmission_delay -> {
                newRetransmissionDelay = progress
            }
        }
    }

    override fun onRangeChangeListener(rangeBar: RangeBar?, leftPinIndex: Int, rightPinIndex: Int, leftPinValue: String?, rightPinValue: String?) {
        newLowerDimmingLevel = if (powerUnitF is PowerUnitFA) {
            leftPinIndex
        } else {
            (leftPinIndex * 2.55 + .5).toInt()
        }
        newUpperDimmingLevel = if (powerUnitF is PowerUnitFA) {
            rightPinIndex
        } else {
            (rightPinIndex * 2.55 + .5).toInt()
        }

        textDimmingLowerLevel.text = leftPinIndex.toString().plus("%")
        textDimmingUpperLevel.text = rightPinIndex.toString().plus("%")

//        if (rightPinIndex - leftPinIndex > 19) {
//            newLowerDimmingLevel = if (powerUnitF is PowerUnitFA) {
//                leftPinIndex
//            } else {
//                (leftPinIndex * 2.55 + .5).toInt()
//            }
//            minLowerDimmingLevel = leftPinIndex
//            textDimmingLowerLevel.text = leftPinIndex.toString().plus("%")
//
//            newUpperDimmingLevel = if (powerUnitF is PowerUnitFA) {
//                rightPinIndex
//            } else {
//                (rightPinIndex * 2.55 + .5).toInt()
//            }
//            minUpperDimmingLevel = rightPinIndex
//            textDimmingUpperLevel.text = rightPinIndex.toString().plus("%")
//        } else {
//            if (powerUnitF is PowerUnitFA) {
//                var pivot = leftPinIndex + (rightPinIndex - leftPinIndex) / 2
//                if (pivot < 11) {
//                    pivot = 11
//                }
//                if (pivot > 90) {
//                    pivot = 90
//                }
//                minLowerDimmingLevel = pivot - 10
//                minUpperDimmingLevel = pivot + 10
//            }
//            rangeDimming.setRangePinsByIndices(minLowerDimmingLevel, minUpperDimmingLevel)
//        }

        if (leftPinIndex == 0) {
            minLowerDimmingLevel = 1
            newLowerDimmingLevel = if (powerUnitF is PowerUnitFA) {
                minLowerDimmingLevel
            } else {
                (minLowerDimmingLevel * 2.55 + .5).toInt()
            }
            rangeDimming.setRangePinsByIndices(minLowerDimmingLevel, rightPinIndex)
        }
//        if (leftPinIndex > 50) {
//            minLowerDimmingLevel = 50
//            newLowerDimmingLevel = if (powerUnitF is PowerUnitFA) {
//                minLowerDimmingLevel
//            } else {
//                (minLowerDimmingLevel * 2.55 + .5).toInt()
//            }
//            rangeDimming.setRangePinsByIndices(minLowerDimmingLevel, minUpperDimmingLevel)
//        }

//        if (leftPinIndex > seekSwitchOnLevel.progress) {
//            newSwitchOnLevel = if (powerUnitF is PowerUnitFA) {
//                leftPinIndex
//            } else {
//                (leftPinIndex * 2.55 + .5).toInt()
//            }
//            seekSwitchOnLevel.progress = leftPinIndex
//        }
//        if (rightPinIndex < seekSwitchOnLevel.progress) {
//            newSwitchOnLevel = if (powerUnitF is PowerUnitFA) {
//                rightPinIndex
//            } else {
//                (rightPinIndex * 2.55 + .5).toInt()
//            }
//            seekSwitchOnLevel.progress = rightPinIndex
//        }
    }

    // VIEW:INITIALISATION

    private fun initView(fragmentView: View): View {
        // title
        buttonBack = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_button_back)
        buttonBack.setOnClickListener(this)
        buttonSave = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_button_save)
        buttonSave.setOnClickListener(this)
        // switch on level
        progressSwitchOnLevel = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_progress_switch_on_level)
        imageSwitchOnLevelWarning = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_image_switch_on_level_warning)
        seekSwitchOnLevel = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_seek_switch_on_level)
        seekSwitchOnLevel.setOnSeekBarChangeListener(this)
        // dimming
        textDimmingState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_text_dimming_state)
        progressDimmingState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_progress_dimming)
        imageDimmingStateWarning = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_image_dimming_warning)
        switchDimmingState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_switch_dimming_state)
        switchDimmingState.setOnCheckedChangeListener(this)
        layoutDimmingRange = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_layout_dimming_range)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            rangeDimming = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_range_dimming)
            rangeDimming.visibility = View.VISIBLE

        } else {
            layoutDimmingRangeAlternative = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_layout_dimming_range_alternative)
            rangeDimming = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_range_dimming_alternative)
            layoutDimmingRangeAlternative.visibility = View.VISIBLE
        }
        rangeDimming.setPinTextFormatter { value -> value.plus("%") }
        rangeDimming.setOnRangeBarChangeListener(this)
        textDimmingLowerLevel = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_text_dimming_lower_level)
        textDimmingUpperLevel = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_text_dimming_upper_level)
        // memory state
        progressMemoryState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_progress_memory_state)
        layoutMemoryState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_layout_memory_state)
        checkPowerUpState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_checkbox_power_up_state)
        checkPowerUpState.setOnCheckedChangeListener(this)
        checkRememberState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_checkbox_remember_state)
        checkRememberState.setOnCheckedChangeListener(this)
        imageMemoryStateWarning = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_image_memory_state_warning)
        // switch on brightness
        progressSwitchOnBrightness = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_progress_switch_on_brightness)
        imageSwitchOnBrightnessWarning = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_image_switch_on_brightness_warning)
        layoutSwitchOnBrightness = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_layout_switch_on_brightness)
        seekSwitchOnBrightness = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_seek_switch_on_brightness)
        seekSwitchOnBrightness.setOnSeekBarChangeListener(this)
        if (powerUnitF is PowerUnitFA) {
            seekSwitchOnBrightness.isEnabled = false
        }
        // external input state
        progressExternalInputState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_progress_external_input_state)
        imageExternalInputStateWarning = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_image_external_input_state)
        groupExternalInputState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_group_external_input_state)
        radioExternalInputStateNone = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_radio_external_input_state_none)
        radioExternalInputStateNone.setOnCheckedChangeListener(this)
        radioExternalInputStateSwitchOff = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_radio_external_input_state_switch_off)
        radioExternalInputStateSwitchOff.setOnCheckedChangeListener(this)
        radioExternalInputStateSwitchOn = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_radio_external_input_state_switch_on)
        radioExternalInputStateSwitchOn.setOnCheckedChangeListener(this)
        radioExternalInputStateSwitch = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_radio_external_input_state_switch)
        radioExternalInputStateSwitch.setOnCheckedChangeListener(this)
        // retransmission
        textRetransmissionState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_text_retransmission_state)
        progressRetransmissionState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_progress_retransmission_state)
        imageRetransmissionStateWarning = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_image_retransmission_state_warning)
        switchRetransmissionState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_switch_retransmission_state)
        switchRetransmissionState.setOnCheckedChangeListener(this)
        layoutRetransmissionDelay = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_layout_retransmission_delay)
        seekRetransmissionDelay = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_seek_retransmission_delay)
        seekRetransmissionDelay.setOnSeekBarChangeListener(this)
        // locks state
        progressLocksConfig = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_progress_locks_config)
        layoutLocksConfig = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_layout_locks_config)
        checkNooLiteState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_checkbox_noolite_state)
        checkNooLiteState.setOnCheckedChangeListener(this)
        checkTemporaryOnState = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_checkbox_temporary_on_state)
        checkTemporaryOnState.setOnCheckedChangeListener(this)
        imageLocksConfigWarning = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_image_locks_config_warning)
        // transmitter sensitivity
        layoutTransmitterSettings = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_layout_transmitter_settings)
        if (powerUnitF is PowerUnitFA) {
            layoutTransmitterSettings.visibility = View.VISIBLE
        }
        layoutTransmitterSensitivity = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_layout_transmitter_sensitivity)
        progressTransmitterSensitivity = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_progress_transmitter_sensitivity)
        groupTransmitterSensitivity = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_group_transmitter_sensitivity)
        radioTransmitterSensitivity0 = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_radio_transmitter_sensitivity_0)
        radioTransmitterSensitivity0.setOnCheckedChangeListener(this)
        radioTransmitterSensitivity6 = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_radio_transmitter_sensitivity_6)
        radioTransmitterSensitivity6.setOnCheckedChangeListener(this)
        radioTransmitterSensitivity12 = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_radio_transmitter_sensitivity_12)
        radioTransmitterSensitivity12.setOnCheckedChangeListener(this)
        radioTransmitterSensitivity18 = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_radio_transmitter_sensitivity_18)
        radioTransmitterSensitivity18.setOnCheckedChangeListener(this)
        imageTransmitterSensitivity = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_image_transmitter_sensitivity_warning)
        // info
        textDeviceType = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_text_info_device_type)
        textFirmwareVersion = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_text_info_firmware_version)
        textFreeNooLiteMemoryCells = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_text_info_free_noolite_cells)
        textFreeNooLiteFMemoryCells = fragmentView.findViewById(R.id.fragment_settings_unit_ftx_text_info_free_noolitef_cells)

        return fragmentView
    }

    private fun setupFragment() {
        val fragmentWindow = dialog?.window
        val dialogParams = fragmentWindow?.attributes
        dialogParams?.dimAmount = 0.75f
        fragmentWindow?.attributes = dialogParams
        fragmentWindow?.setBackgroundDrawableResource(R.color.transparent)

        val display = DisplayMetrics()
        homeActivity.windowManager.defaultDisplay.getMetrics(display)
        val displayWidth = display.widthPixels
        val displayHeight = display.heightPixels
        if (displayWidth < displayHeight) {
            fragmentWindow?.setLayout(displayWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        } else {
            fragmentWindow?.setLayout(displayHeight, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    // IView

    private fun showSwitchOnLevelSettings() {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            when (newSwitchOnLevel) {
                -1 -> {
                    progressSwitchOnLevel.visibility = View.GONE
                    seekSwitchOnLevel.progress = 0
                    seekSwitchOnLevel.visibility = View.GONE
                    imageSwitchOnLevelWarning.visibility = View.VISIBLE
                }
                else -> {
                    progressSwitchOnLevel.visibility = View.GONE
                    imageSwitchOnLevelWarning.visibility = View.GONE
                    if (powerUnitF is PowerUnitFA) {
                        seekSwitchOnLevel.progress = newSwitchOnLevel
                    } else {
                        seekSwitchOnLevel.progress = (newSwitchOnLevel / 255.0 * 100 + .5).toInt()
                    }
                    seekSwitchOnLevel.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showDimmingSettings() {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            when (newDimmingState) {
                -1 -> {
                    textDimmingState.text = "Диммирование недоступно"
                    progressDimmingState.visibility = View.GONE
                    switchDimmingState.isChecked = false
                    switchDimmingState.visibility = View.GONE
                    layoutDimmingRange.visibility = View.GONE
                    imageDimmingStateWarning.visibility = View.VISIBLE
                }
                0 -> {
                    textDimmingState.text = "Диммирование выключено"
                    progressDimmingState.visibility = View.GONE
                    imageDimmingStateWarning.visibility = View.GONE
                    switchDimmingState.isChecked = false
                    switchDimmingState.visibility = View.VISIBLE
                    layoutDimmingRange.visibility = View.GONE
                }
                1 -> {
                    textDimmingState.text = "Диммирование включено"
                    progressDimmingState.visibility = View.GONE
                    imageDimmingStateWarning.visibility = View.GONE
                    switchDimmingState.isChecked = true
                    switchDimmingState.visibility = View.VISIBLE
                    layoutDimmingRange.visibility = View.VISIBLE
                }
            }
            if (powerUnitF is PowerUnitFA) {
                rangeDimming.setRangePinsByIndices(newLowerDimmingLevel, newUpperDimmingLevel)
            } else {
                rangeDimming.setRangePinsByIndices((newLowerDimmingLevel / 255.0 * 100 + .5).toInt(), (newUpperDimmingLevel / 255.0 * 100 + .5).toInt())
            }
        }
    }

    private fun showMemoryStateSettings() {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            when (newPowerUpState) {
                -1 -> {
                    checkPowerUpState.isChecked = false
                }
                0 -> {
                    checkPowerUpState.isChecked = false
                }
                1 -> {
                    checkPowerUpState.isChecked = true
                }
            }
            when (newRememberState) {
                -1 -> {
                    progressMemoryState.visibility = View.GONE
                    checkRememberState.isChecked = false
                    layoutMemoryState.visibility = View.GONE
                    imageMemoryStateWarning.visibility = View.VISIBLE
                }
                0 -> {
                    progressMemoryState.visibility = View.GONE
                    imageMemoryStateWarning.visibility = View.GONE
                    checkRememberState.isChecked = false
                    layoutMemoryState.visibility = View.VISIBLE
                }
                1 -> {
                    progressMemoryState.visibility = View.GONE
                    imageMemoryStateWarning.visibility = View.GONE
                    checkRememberState.isChecked = true
                    layoutMemoryState.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showSwitchOnBrightnessSettings() {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            when (newSwitchOnBrightness) {
                -1 -> {
                    progressSwitchOnBrightness.visibility = View.GONE
                    seekSwitchOnBrightness.progress = 0
                    layoutSwitchOnBrightness.visibility = View.GONE
                    imageSwitchOnBrightnessWarning.visibility = View.VISIBLE
                }
                else -> {
                    progressSwitchOnBrightness.visibility = View.GONE
                    imageSwitchOnBrightnessWarning.visibility = View.GONE
                    if (powerUnitF is PowerUnitFA) {
                        seekSwitchOnBrightness.progress = newSwitchOnBrightness
                    } else {
                        seekSwitchOnBrightness.progress = (newSwitchOnBrightness / 255.0 * 100 + .5).toInt()
                    }
                    layoutSwitchOnBrightness.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showExternalInputState() {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            when (newExternalInputState) {
                0 -> {
                    radioExternalInputStateSwitchOff.isChecked = true
                }
                1 -> {
                    radioExternalInputStateSwitch.isChecked = true
                }
                2 -> {
                    radioExternalInputStateSwitchOn.isChecked = true
                }
                3 -> {
                    radioExternalInputStateNone.isChecked = true
                }
            }
            if (newExternalInputState == -1) {
                progressExternalInputState.visibility = View.GONE
                groupExternalInputState.visibility = View.GONE
                imageExternalInputStateWarning.visibility = View.VISIBLE
            } else {
                progressExternalInputState.visibility = View.GONE
                imageExternalInputStateWarning.visibility = View.GONE
                groupExternalInputState.visibility = View.VISIBLE
            }
        }
    }

    private fun showRetransmissionSettings() {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            when (newRetransmissionState) {
                -1 -> {
                    textRetransmissionState.text = "Ретрансляция недоступна"
                    progressRetransmissionState.visibility = View.GONE
                    switchRetransmissionState.isChecked = false
                    switchRetransmissionState.visibility = View.GONE
                    layoutRetransmissionDelay.visibility = View.GONE
                    imageRetransmissionStateWarning.visibility = View.VISIBLE
                }
                0 -> {
                    textRetransmissionState.text = "Ретрансляция выключена"
                    progressRetransmissionState.visibility = View.GONE
                    imageRetransmissionStateWarning.visibility = View.GONE
                    switchRetransmissionState.isChecked = false
                    switchRetransmissionState.visibility = View.VISIBLE
                    layoutRetransmissionDelay.visibility = View.GONE
                }
                1 -> {
                    textRetransmissionState.text = "Ретрансляция включена"
                    progressRetransmissionState.visibility = View.GONE
                    imageRetransmissionStateWarning.visibility = View.GONE
                    switchRetransmissionState.isChecked = true
                    switchRetransmissionState.visibility = View.VISIBLE
                    layoutRetransmissionDelay.visibility = View.VISIBLE
                }
            }
            seekRetransmissionDelay.progress = newRetransmissionDelay
        }
    }

    private fun showLocksConfig() {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            when (newNooLiteState) {
                -1 -> {
                    checkNooLiteState.isChecked = false
                }
                0 -> {
                    checkNooLiteState.isChecked = false
                }
                1 -> {
                    checkNooLiteState.isChecked = true
                }
            }
            when (newTemporaryOnState) {
                -1 -> {
                    progressLocksConfig.visibility = View.GONE
                    checkTemporaryOnState.isChecked = false
                    layoutLocksConfig.visibility = View.GONE
                    imageLocksConfigWarning.visibility = View.VISIBLE
                }
                0 -> {
                    progressLocksConfig.visibility = View.GONE
                    imageLocksConfigWarning.visibility = View.GONE
                    checkTemporaryOnState.isChecked = false
                    layoutLocksConfig.visibility = View.VISIBLE
                }
                1 -> {
                    progressLocksConfig.visibility = View.GONE
                    imageLocksConfigWarning.visibility = View.GONE
                    checkTemporaryOnState.isChecked = true
                    layoutLocksConfig.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showTransmitterSensitivity() {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            when (newTransmitterSensitivity) {
                0 -> {
                    radioTransmitterSensitivity0.isChecked = true
                }
                1 -> {
                    radioTransmitterSensitivity6.isChecked = true
                }
                2 -> {
                    radioTransmitterSensitivity12.isChecked = true
                }
                3 -> {
                    radioTransmitterSensitivity18.isChecked = true
                }
            }
            if (newTransmitterSensitivity == -1) {
                progressTransmitterSensitivity.visibility = View.GONE
                layoutTransmitterSensitivity.visibility = View.GONE
                imageTransmitterSensitivity.visibility = View.VISIBLE
            } else {
                progressTransmitterSensitivity.visibility = View.GONE
                imageTransmitterSensitivity.visibility = View.GONE
                layoutTransmitterSensitivity.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDeviceInfo() {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            when (deviceType) {
                -1 -> {
                    textDeviceType.text = "-"
                }
                5 -> {
                    textDeviceType.text = "SUF-1-300"
                }
                9 -> {
                    textDeviceType.text = "SUF-1-300-A"
                }
                else -> {
                    textDeviceType.text = "устройство не определено"
                }
            }
            if (firmwareVersion > -1) {
                textFirmwareVersion.text = firmwareVersion.toString()
            } else {
                textFirmwareVersion.text = "-"
            }
            if (freeNooLiteMemoryCells > -1) {
                textFreeNooLiteMemoryCells.text = "nooLite: ".plus(freeNooLiteMemoryCells)
            } else {
                textFreeNooLiteMemoryCells.text = "nooLite: -"
            }
            if (freeNooLiteFMemoryCells > -1) {
                textFreeNooLiteFMemoryCells.text = "nooLite-F: ".plus(freeNooLiteFMemoryCells)
            } else {
                textFreeNooLiteFMemoryCells.text = "nooLite-F: -"
            }
        }
    }

    private fun blockUI() {
        if (!isAdded) return
        guiBlockFragment = childFragmentManager.findFragmentByTag("GUI_BLOCK_FRAGMENT") as GUIBlockFragment?
                ?: GUIBlockFragment()
        if (guiBlockFragment?.isAdded!!) return
        childFragmentManager.beginTransaction().add(guiBlockFragment!!, "GUI_BLOCK_FRAGMENT").show(guiBlockFragment!!).commit()
    }

    private fun unblockUI() {
        if (!isAdded) return
        guiBlockFragment?.dismiss()
    }

    private fun showToast(message: String) {
        if (!isAdded) return
        homeActivity.runOnUiThread {
            unblockUI()
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // MODEL

    // GET

    private fun getSettings() {
        Thread {
            try {
                if (powerUnitF is PowerUnitFA) {
                    getInfo()
                    Thread.sleep(100)
                    getMainSettings()
                    Thread.sleep(100)
                    getDimmingSettings()
                    Thread.sleep(100)
                    getRetransmissionSettings()
                } else {
                    val successfully: Boolean = getMainSettingsByte()
                    Thread.sleep(100)
                    getSwitchOnLevel()
                    Thread.sleep(100)
                    getDimmingState(successfully)
                    getDimmingRange(successfully)
                    getMemoryState(successfully)
                    Thread.sleep(100)
                    getSwitchOnBrightness()
                    getExternalInputState(successfully)
                    Thread.sleep(100)
                    getRetransmissionState(successfully)
                    getRetransmissionDelay(successfully)
                    getLocksConfig(successfully)
                    Thread.sleep(100)
                    getDeviceInfo()
                }
            } catch (se: SocketException) {
                call.cancel()
                homeActivity.writeAppLog("FTXUnitSettings.kt : getSettings()" + "\n" + se.toString() + "\n" + NooLiteF.getStackTrace(se))
                showToast(homeActivity.getString(R.string.connection_error))
            } catch (e: Exception) {
                call.cancel()
                homeActivity.writeAppLog("FTXUnitSettings.kt : getSettings()" + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e))
                showToast(homeActivity.getString(R.string.some_thing_went_wrong))
            } finally {
                showSwitchOnLevelSettings()
                showDimmingSettings()
                showMemoryStateSettings()
                showSwitchOnBrightnessSettings()
                showExternalInputState()
                showRetransmissionSettings()
                showLocksConfig()
                if (powerUnitF is PowerUnitFA) {
                    showTransmitterSensitivity()
                }
                showDeviceInfo()
            }
        }.start()
    }

    // SUF-1-300 SETTINGS

    // get switch on level

    private fun getSwitchOnLevel(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801200000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newSwitchOnLevel = sResponse.substring(14, 16).toInt(16)
                        currentSwitchOnLevel = newSwitchOnLevel
                        showSwitchOnLevelSettings()
                        return true
                    }
                }
            }
        }
        call.cancel()
        newSwitchOnLevel = -1
        currentSwitchOnLevel = newSwitchOnLevel
        showSwitchOnLevelSettings()
        return false
    }

    // get main settings byte

    private fun getMainSettingsByte(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801000000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        mainSettingsByteString = StringBuilder(String.format("%8s", Integer.toBinaryString(sResponse.substring(14, 16).toInt(16))).replace(' ', '0'))
                        return true
                    }
                }
            }
        }
        call.cancel()
        return false
    }

    // get dimming

    private fun getDimmingState(successfully: Boolean) {
        if (!successfully) {
            newDimmingState = -1
            currentDimmingState = newDimmingState
            return
        }

        newDimmingState = mainSettingsByteString.substring(6, 7).toInt()
        currentDimmingState = newDimmingState
    }

    // get dimming range

    private fun getDimmingRange(successfully: Boolean): Boolean {
        if (!successfully) {
            showDimmingSettings()
            return false
        }

        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801100000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newLowerDimmingLevel = sResponse.substring(16, 18).toInt(16)
                        currentLowerDimmingLevel = newLowerDimmingLevel
                        newUpperDimmingLevel = sResponse.substring(14, 16).toInt(16)
                        currentUpperDimmingLevel = newUpperDimmingLevel
                        showDimmingSettings()
                        return true
                    }
                }
            }
        }
        call.cancel()
        newDimmingState = -1
        currentDimmingState = newDimmingState
        showDimmingSettings()
        return false
    }

    // get memory state

    private fun getMemoryState(successfully: Boolean) {
        if (!successfully) {
            newPowerUpState = -1
            currentPowerUpState = newPowerUpState
            newRememberState = -1
            currentRememberState = newRememberState
            showMemoryStateSettings()
            return
        }

        newPowerUpState = mainSettingsByteString.substring(2, 3).toInt()
        currentPowerUpState = newPowerUpState
        newRememberState = mainSettingsByteString.substring(7).toInt()
        currentRememberState = newRememberState
        showMemoryStateSettings()
    }

    // get switch on brightness

    private fun getSwitchOnBrightness(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801800000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newSwitchOnBrightness = sResponse.substring(16, 18).toInt(16)
                        currentSwitchOnBrightness = newSwitchOnBrightness
                        showSwitchOnBrightnessSettings()
                        return true
                    }
                }
            }
        }
        call.cancel()
        newSwitchOnBrightness = -1
        currentSwitchOnBrightness = newSwitchOnBrightness
        showSwitchOnBrightnessSettings()
        return false
    }

    // get external input state

    private fun getExternalInputState(successfully: Boolean) {
        if (!successfully) {
            newExternalInputState = -1
            currentExternalInputState = newExternalInputState
            showExternalInputState()
            return
        }

        newExternalInputState = mainSettingsByteString.substring(3, 5).toInt(2)
        currentExternalInputState = newExternalInputState
        showExternalInputState()
    }

    // get retransmission

    private fun getRetransmissionState(successfully: Boolean) {
        if (!successfully) {
            newRetransmissionState = -1
            currentRetransmissionState = newRetransmissionState
            return
        }

        newRetransmissionState = mainSettingsByteString.substring(1, 2).toInt()
        currentRetransmissionState = newRetransmissionState
    }

    // get retransmission delay

    private fun getRetransmissionDelay(successfully: Boolean): Boolean {
        if (!successfully) {
            showRetransmissionSettings()
            return false
        }

        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801300000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newRetransmissionDelay = sResponse.substring(14, 16).toInt(16)
                        currentRetransmissionDelay = newRetransmissionDelay
                        showRetransmissionSettings()
                        return true
                    }
                }
            }
        }
        call.cancel()
        newRetransmissionDelay = -1
        currentRetransmissionDelay = newRetransmissionDelay
        showRetransmissionSettings()
        return false
    }

    // get locks config

    private fun getLocksConfig(successfully: Boolean) {
        if (!successfully) {
            newNooLiteState = -1
            currentNooLiteState = newNooLiteState
            newTemporaryOnState = -1
            currentTemporaryOnState = newTemporaryOnState
            showLocksConfig()
            return
        }

        newNooLiteState = mainSettingsByteString.substring(5, 6).toInt()
        currentNooLiteState = newNooLiteState
        newTemporaryOnState = mainSettingsByteString.substring(0, 1).toInt()
        currentTemporaryOnState = newTemporaryOnState
        showLocksConfig()
    }

    // get device info

    private fun getDeviceInfo(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000800200000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        deviceType = sResponse.substring(14, 16).toInt(16)
                        firmwareVersion = sResponse.substring(16, 18).toInt(16)
                        freeNooLiteMemoryCells = sResponse.substring(18, 20).toInt(16)
                        freeNooLiteFMemoryCells = sResponse.substring(20, 22).toInt(16)
                        showDeviceInfo()
                        return true
                    }
                }
            }
        }
        call.cancel()
        deviceType = -1
        firmwareVersion = -1
        freeNooLiteMemoryCells = -1
        freeNooLiteFMemoryCells = -1
        showDeviceInfo()
        return false
    }

    // POST

    private fun postSettings() {
        blockUI()

        if (powerUnitF is PowerUnitFA) {
            if (newUpperDimmingLevel - newLowerDimmingLevel == 0) {
                newUpperDimmingLevel += 1
                if (newUpperDimmingLevel > 100) {
                    newLowerDimmingLevel = 99
                    newUpperDimmingLevel = 100
                }
            }
            if (newSwitchOnLevel < newLowerDimmingLevel || newUpperDimmingLevel < newSwitchOnLevel) {
                newSwitchOnLevel = newLowerDimmingLevel
            }
        } else {
            if (rangeDimming.rightIndex - rangeDimming.leftIndex < 20) {
                if (rangeDimming.leftIndex + 20 > 100) {
                    rangeDimming.setRangePinsByIndices(80, 100)
                } else {
                    rangeDimming.setRangePinsByIndices(rangeDimming.leftIndex, rangeDimming.leftIndex + 20)
                }
            }
            if (50 < seekSwitchOnLevel.progress) {
                seekSwitchOnLevel.progress = 50
            }
            if (newDimmingState == 1 && newRetransmissionState == 1) {
                newDimmingState = 1
                newRetransmissionState = 0
            }
        }

        Thread {
            try {
                if (powerUnitF is PowerUnitFA) {
                    val setD0 = setMainSettingsByte()
                    val setD1 = setAdditionalSettingsByte()
                    if (setD0 || setD1) {
                        if (!postMainSettings()) throw ConnectException("ConnectException in postMainSettings()")
                        Thread.sleep(200)
                    }
                    if ((currentLowerDimmingLevel != newLowerDimmingLevel) || (currentSwitchOnLevel != newSwitchOnLevel) || (currentUpperDimmingLevel != newUpperDimmingLevel)) {
                        if (!postDimmingSettings()) throw ConnectException("ConnectException in postDimmingSettings()")
                        Thread.sleep(200)
                    }
                    if (currentRetransmissionDelay != newRetransmissionDelay) {
                        if (!postRetransmissionSettings()) throw ConnectException("ConnectException in postRetransmissionSettings()")
                        Thread.sleep(200)
                    }
                } else {
                    if (setMainSettingsByte()) {
                        if (!postMainSettingsByte()) throw ConnectException("ConnectException in postMainSettingsByte()")
                        Thread.sleep(200)
                    }
                    if ((currentLowerDimmingLevel != newLowerDimmingLevel) || (currentUpperDimmingLevel != newUpperDimmingLevel)) {
                        if (!postDimmingRange()) throw ConnectException("ConnectException in postDimmingRange()")
                        Thread.sleep(200)
                    }
                    if (currentSwitchOnLevel != newSwitchOnLevel) {
                        if (!postSwitchOnLevel()) throw ConnectException("ConnectException in postSwitchOnLevel()")
                        Thread.sleep(200)
                    }
                    if (currentSwitchOnBrightness != newSwitchOnBrightness) {
                        if (!postSwitchOnBrightness()) throw ConnectException("ConnectException in postSwitchOnBrightness()")
                        Thread.sleep(200)
                    }
                    if (currentRetransmissionDelay != newRetransmissionDelay) {
                        if (!postRetransmissionDelay()) throw ConnectException("ConnectException in postRetransmissionDelay()")
                    }

                    getDimmingState(true)
                    getMemoryState(true)
                    getExternalInputState(true)
                    getRetransmissionState(true)
                    getLocksConfig(true)
                }
                showToast("Настройки отправлены на устройство")
            } catch (ce: ConnectException) {
                call.cancel()
                homeActivity.writeAppLog("FTXUnitSettings.kt : setSettings()" + "\n" + ce.toString() + "\n" + NooLiteF.getStackTrace(ce))
                showToast(homeActivity.getString(R.string.connection_error))
            } catch (e: Exception) {
                call.cancel()
                homeActivity.writeAppLog("FTXUnitSettings.kt : setSettings()" + "\n" + e.toString() + "\n" + NooLiteF.getStackTrace(e))
                showToast(homeActivity.getString(R.string.some_thing_went_wrong))
            } finally {
                showSwitchOnLevelSettings()
                showDimmingSettings()
                showMemoryStateSettings()
                showSwitchOnBrightnessSettings()
                showExternalInputState()
                showRetransmissionSettings()
                showLocksConfig()
                if (powerUnitF is PowerUnitFA) {
                    showTransmitterSensitivity()
                }
                showDeviceInfo()

                unblockUI()
            }
        }.start()
    }

    // set main settings byte

    private fun setMainSettingsByte(): Boolean {
        var post = false
        if (currentTemporaryOnState != newTemporaryOnState) {
            mainSettingsByteString.replace(0, 1, newTemporaryOnState.toString())
            post = true
        }
        if (currentRetransmissionState != newRetransmissionState) {
            mainSettingsByteString.replace(1, 2, newRetransmissionState.toString())
            post = true
        }
        if (currentPowerUpState != newPowerUpState) {
            mainSettingsByteString.replace(2, 3, newPowerUpState.toString())
            post = true
        }
        if (currentExternalInputState != newExternalInputState) {
            mainSettingsByteString.replace(3, 5, String.format("%2s", Integer.toBinaryString(newExternalInputState)).replace(' ', '0'))
            post = true
        }
        if (currentNooLiteState != newNooLiteState) {
            mainSettingsByteString.replace(5, 6, newNooLiteState.toString())
            post = true
        }
        if (currentDimmingState != newDimmingState) {
            mainSettingsByteString.replace(6, 7, newDimmingState.toString())
            post = true
        }
        if (currentRememberState != newRememberState) {
            mainSettingsByteString.replace(7, 8, newRememberState.toString())
            post = true
        }
        return post
    }

    private fun setAdditionalSettingsByte(): Boolean {
        var post = false
        if (currentTransmitterSensitivity != newTransmitterSensitivity) {
            additionSettingsByteString.replace(6, 8, String.format("%2s", Integer.toBinaryString(newTransmitterSensitivity)).replace(' ', '0'))
            post = true
        }
        return post
    }

    // post main settings byte

    private fun postMainSettingsByte(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008110%s00FF00%s", NooLiteF.getHexString(mainSettingsByteString.toString().toInt(2)), powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        mainSettingsByteString = StringBuilder(String.format("%8s", Integer.toBinaryString(sResponse.substring(14, 16).toInt(16))).replace(' ', '0'))

                        return true
                    }
                }
            }
        }
        call.cancel()
        return false
    }

    // post dimming range

    private fun postDimmingRange(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008111%s%s0000%s", NooLiteF.getHexString(newUpperDimmingLevel), NooLiteF.getHexString(newLowerDimmingLevel), powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newLowerDimmingLevel = sResponse.substring(16, 18).toInt(16)
                        currentLowerDimmingLevel = newLowerDimmingLevel
                        newUpperDimmingLevel = sResponse.substring(14, 16).toInt(16)
                        currentUpperDimmingLevel = newUpperDimmingLevel

                        return true
                    }
                }
            }
        }
        call.cancel()
        return false
    }

    // post switch on level

    private fun postSwitchOnLevel(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008112%s000000%s", NooLiteF.getHexString(newSwitchOnLevel), powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newSwitchOnLevel = sResponse.substring(14, 16).toInt(16)
                        currentSwitchOnLevel = newSwitchOnLevel

                        return true
                    }
                }
            }
        }
        call.cancel()
        return false
    }

    // post retransmission delay

    private fun postRetransmissionDelay(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008113%s000000%s", NooLiteF.getHexString(newRetransmissionDelay), powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newRetransmissionDelay = sResponse.substring(14, 16).toInt(16)
                        currentRetransmissionDelay = newRetransmissionDelay

                        return true
                    }
                }
            }
        }
        call.cancel()
        return false
    }

    // post switch on brightness

    private fun postSwitchOnBrightness(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008118%s00FF00%s", NooLiteF.getHexString(newSwitchOnBrightness), powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newSwitchOnBrightness = sResponse.substring(16, 18).toInt(16)
                        currentSwitchOnBrightness = newSwitchOnBrightness

                        return true
                    }
                }
            }
        }
        call.cancel()
        return false
    }

    // SUF-1-300-A SETTINGS

    // GET

    private fun getInfo() {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000800000000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        deviceType = sResponse.substring(14, 16).toInt(16)
                        firmwareVersion = sResponse.substring(16, 18).toInt(16)
                        newSwitchOnBrightness = sResponse.substring(20, 22).toInt(16)
                        currentSwitchOnBrightness = newSwitchOnBrightness
                        freeNooLiteMemoryCells = -1
                        freeNooLiteFMemoryCells = -1

                        return
                    }
                }
            }
        }
        call.cancel()
        deviceType = -1
        firmwareVersion = -1
        currentSwitchOnBrightness = -1
        newSwitchOnBrightness = -1
        freeNooLiteMemoryCells = -1
        freeNooLiteFMemoryCells = -1
    }

    private fun getMainSettings() {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801000000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        mainSettingsByteString = StringBuilder(String.format("%8s", Integer.toBinaryString(sResponse.substring(14, 16).toInt(16))).replace(' ', '0'))

                        newRememberState = mainSettingsByteString.substring(7).toInt()
                        currentRememberState = newRememberState
                        newDimmingState = mainSettingsByteString.substring(6, 7).toInt()
                        currentDimmingState = newDimmingState
                        newNooLiteState = mainSettingsByteString.substring(5, 6).toInt()
                        currentNooLiteState = newNooLiteState
                        newExternalInputState = mainSettingsByteString.substring(3, 5).toInt(2)
                        currentExternalInputState = newExternalInputState
                        newPowerUpState = mainSettingsByteString.substring(2, 3).toInt()
                        currentPowerUpState = newPowerUpState
                        newRetransmissionState = mainSettingsByteString.substring(1, 2).toInt()
                        currentRetransmissionState = newRetransmissionState
                        newTemporaryOnState = mainSettingsByteString.substring(0, 1).toInt()
                        currentTemporaryOnState = newTemporaryOnState

                        additionSettingsByteString = StringBuilder(String.format("%8s", Integer.toBinaryString(sResponse.substring(16, 18).toInt(16))).replace(' ', '0'))
                        newTransmitterSensitivity = additionSettingsByteString.substring(6).toInt(2)
                        currentTransmitterSensitivity = newTransmitterSensitivity

                        return
                    }
                }
            }
        }
        call.cancel()
    }

    private fun getDimmingSettings() {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801100000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newSwitchOnLevel = sResponse.substring(16, 18).toInt(16)
                        currentSwitchOnLevel = newSwitchOnLevel
                        newLowerDimmingLevel = sResponse.substring(18, 20).toInt(16)
                        currentLowerDimmingLevel = newLowerDimmingLevel
                        newUpperDimmingLevel = sResponse.substring(14, 16).toInt(16)
                        currentUpperDimmingLevel = newUpperDimmingLevel

                        return
                    }
                }
            }
        }
        call.cancel()
        currentSwitchOnLevel = -1
        newSwitchOnLevel = -1
        newDimmingState = -1
        currentDimmingState = newDimmingState
    }

    private fun getRetransmissionSettings() {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=0002080000801200000000%s", powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newRetransmissionDelay = sResponse.substring(14, 16).toInt(16)
                        currentRetransmissionDelay = newRetransmissionDelay

                        return
                    }
                }
            }
        }
        call.cancel()
        newRetransmissionDelay = -1
        currentRetransmissionDelay = -1
    }

    // POST

    private fun postMainSettings(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008110%s%sFFFF%s", NooLiteF.getHexString(mainSettingsByteString.toString().toInt(2)), NooLiteF.getHexString(additionSettingsByteString.toString().toInt(2)), powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        mainSettingsByteString = StringBuilder(String.format("%8s", Integer.toBinaryString(sResponse.substring(14, 16).toInt(16))).replace(' ', '0'))

                        newRememberState = mainSettingsByteString.substring(7).toInt()
                        currentRememberState = newRememberState
                        newDimmingState = mainSettingsByteString.substring(6, 7).toInt()
                        currentDimmingState = newDimmingState
                        newNooLiteState = mainSettingsByteString.substring(5, 6).toInt()
                        currentNooLiteState = newNooLiteState
                        newExternalInputState = mainSettingsByteString.substring(3, 5).toInt(2)
                        currentExternalInputState = newExternalInputState
                        newPowerUpState = mainSettingsByteString.substring(2, 3).toInt()
                        currentPowerUpState = newPowerUpState
                        newRetransmissionState = mainSettingsByteString.substring(1, 2).toInt()
                        currentRetransmissionState = newRetransmissionState
                        newTemporaryOnState = mainSettingsByteString.substring(0, 1).toInt()
                        currentTemporaryOnState = newTemporaryOnState

                        additionSettingsByteString = StringBuilder(String.format("%8s", Integer.toBinaryString(sResponse.substring(16, 18).toInt(16))).replace(' ', '0'))
                        newTransmitterSensitivity = additionSettingsByteString.substring(6).toInt(2)
                        currentTransmitterSensitivity = newTransmitterSensitivity

                        return true
                    }
                }
            }
        }
        call.cancel()
        return false
    }

    private fun postDimmingSettings(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008111%s%s%s00%s", NooLiteF.getHexString(newUpperDimmingLevel), NooLiteF.getHexString(newSwitchOnLevel), NooLiteF.getHexString(newLowerDimmingLevel), powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newSwitchOnLevel = sResponse.substring(16, 18).toInt(16)
                        currentSwitchOnLevel = newSwitchOnLevel
                        newLowerDimmingLevel = sResponse.substring(18, 20).toInt(16)
                        currentLowerDimmingLevel = newLowerDimmingLevel
                        newUpperDimmingLevel = sResponse.substring(14, 16).toInt(16)
                        currentUpperDimmingLevel = newUpperDimmingLevel

                        return true
                    }
                }
            }
        }
        call.cancel()
        return false
    }

    private fun postRetransmissionSettings(): Boolean {
        request = Request.Builder()
                .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=010C%s000000000000000000", powerUnitF.id))
                .build()
        call = client.newCall(request)
        response = call.execute()
        if (response.isSuccessful) {
            call.cancel()
            Thread.sleep(100)
            request = Request.Builder()
                    .url(Settings.URL() + String.format(Locale.ROOT, "send.htm?sd=00020800008112%s000000%s", NooLiteF.getHexString(newRetransmissionDelay), powerUnitF.id))
                    .build()
            call = client.newCall(request)
            response = call.execute()
            if (response.isSuccessful) {
                call.cancel()
                Thread.sleep(200)
                request = Request.Builder()
                        .url(Settings.URL() + "rxset.htm")
                        .build()
                call = client.newCall(request)
                response = call.execute()
                if (response.isSuccessful) {
                    sResponse = response.body()!!.string()
                    call.cancel()
                    if (sResponse.substring(10, 12) == SEND_STATE && sResponse.substring(22, 30) == powerUnitF.id) {
                        newRetransmissionDelay = sResponse.substring(14, 16).toInt(16)
                        currentRetransmissionDelay = newRetransmissionDelay

                        return true
                    }
                }
            }
        }
        call.cancel()
        return false
    }
}
