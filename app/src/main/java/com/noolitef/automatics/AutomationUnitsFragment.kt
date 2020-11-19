package com.noolitef.automatics

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noolitef.PRF64
import com.noolitef.R


class AutomationUnitsFragment : DialogFragment(), View.OnClickListener, IAutomationUnit {

    private lateinit var nooLitePRF64: PRF64
    private var showTriggerList: Boolean = false
    private var showPresets: Boolean = false
    private var showPowerUnits: Boolean = false
    private lateinit var iAutomationUnit: IAutomationUnit


    // CONSTRUCTOR

    companion object {

        @JvmStatic
        fun getInstance(
                fragmentManager: FragmentManager?,
                nooLitePRF64: PRF64, showTriggerList: Boolean,
                showPresets: Boolean,
                showPowerUnits: Boolean,
                iAutomationUnit: IAutomationUnit
        ): AutomationUnitsFragment {

            var fragment = fragmentManager?.findFragmentByTag(AutomationUnitsFragment::class.java.simpleName)

            if (fragment == null) {
                fragment = AutomationUnitsFragment()
            }
            val automationUnitsFragment = fragment as AutomationUnitsFragment
            automationUnitsFragment.setPRF64(nooLitePRF64)
            automationUnitsFragment.setFlags(showTriggerList, showPresets, showPowerUnits)
            automationUnitsFragment.setCallback(iAutomationUnit)

            return fragment
        }
    }

    private fun setPRF64(nooLitePRF64: PRF64) {

        this.nooLitePRF64 = nooLitePRF64
    }

    private fun setFlags(showTriggerList: Boolean, showPresets: Boolean, showPowerUnits: Boolean) {

        this.showTriggerList = showTriggerList
        this.showPresets = showPresets
        this.showPowerUnits = showPowerUnits
    }

    private fun setCallback(iAutomationUnit: IAutomationUnit) {

        this.iAutomationUnit = iAutomationUnit
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
                        R.layout.fragment_automation_units, container, false
                )
        )
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


    // INIT VIEW

    private fun initView(view: View): View {

        // toolbar
        val bBack: Button = view.findViewById(R.id.fragment_automation_units_button_back)
        bBack.setOnClickListener(this)
        val tvTitle: TextView = view.findViewById(R.id.fragment_automation_units_title)
        if (showTriggerList) {
            tvTitle.text = "Выберите инициатора"
        } else {
            tvTitle.text = "Выберите исполнителя"
        }
        // list
        val rvAutomationUnitList: RecyclerView = view.findViewById(R.id.fragment_automation_units_list)
        rvAutomationUnitList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvAutomationUnitList.setHasFixedSize(true)
        val automationUnitListAdapter = AutomationUnitListAdapter(nooLitePRF64, showTriggerList, showPresets, showPowerUnits, this)
        rvAutomationUnitList.adapter = automationUnitListAdapter

        return view
    }

    private fun setupWindow() {

        val window = dialog?.window

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
            R.id.fragment_automation_units_button_back -> dismiss()
        }
    }


    // CALLBACKS

    override fun setUnit(unit: Any) {

        iAutomationUnit.setUnit(unit)
        dismiss()
    }
}
