package com.noolitef.automatics

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.noolitef.R
import com.noolitef.settings.Settings


private class AutomationListHeader
private class AutomationListFooter


class AutomationListAdapter(private val iAutomatics: IAutomatics) : RecyclerView.Adapter<ViewHolder>() {

    companion object {

        const val HEADER = Int.MIN_VALUE
        const val AUTOMATION = 1
        const val FOOTER = Int.MAX_VALUE
    }

    private val automationList: ArrayList<Any> = ArrayList()


    init {

        setHasStableIds(true)
    }


    inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView)

    inner class AutomationViewHolder(itemView: View) : ViewHolder(itemView) {

        val rlAutomation = itemView.findViewById<RelativeLayout>(R.id.card_view_automation)!!
        val tvAutomationName = itemView.findViewById<TextView>(R.id.card_view_automation_name)!!
        val tvAutomationEntityTriggerEntity = itemView.findViewById<TextView>(R.id.card_view_automation_trigger_entity)!!
        val tvAutomationEntityTriggerName = itemView.findViewById<TextView>(R.id.card_view_automation_trigger_name)!!
        val tvAutomationUnitAction = itemView.findViewById<TextView>(R.id.card_view_automation_unit_action)!!
        val tvAutomationUnitName = itemView.findViewById<TextView>(R.id.card_view_automation_unit_name)!!
        val scAutomationState = itemView.findViewById<SwitchCompat>(R.id.card_view_automation_state)!!
        val tvAutomationIndex = itemView.findViewById<TextView>(R.id.card_view_automation_index)!!
    }

    inner class FooterViewHolder(itemView: View) : ViewHolder(itemView)


    fun update(automatics: ArrayList<Automation>) {

        automationList.clear()
        automationList.add(AutomationListHeader())
        automationList.addAll(automatics)
        automationList.add(AutomationListFooter())
        notifyDataSetChanged()
    }


    override fun getItemId(position: Int): Long {

        return position.toLong()
    }

    override fun getItemCount(): Int {

        return automationList.size
    }

    override fun getItemViewType(position: Int): Int {

        return when (automationList[position]) {
            is AutomationListHeader -> HEADER
            is Automation -> AUTOMATION
            is AutomationListFooter -> FOOTER
            else -> FOOTER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return when (viewType) {
            HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_list_item_header, parent, false)
                HeaderViewHolder(view)
            }
            AUTOMATION -> {
                val view = if (Settings.isNightMode()) {
                    LayoutInflater.from(parent.context).inflate(R.layout.card_view_automation_dark, parent, false)
                } else {
                    LayoutInflater.from(parent.context).inflate(R.layout.card_view_automation, parent, false)
                }
                AutomationViewHolder(view)
            }
            FOOTER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_list_item_footer, parent, false)
                FooterViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_list_item_footer, parent, false)
                FooterViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (holder.itemViewType == AUTOMATION) {
            bindAutomation(holder, position)
        }
    }


    private fun bindAutomation(holder: ViewHolder, position: Int) {

        val automationViewHolder: AutomationViewHolder = holder as AutomationViewHolder
        val automation = automationList[position] as Automation

        automationViewHolder.rlAutomation.setOnClickListener {
            iAutomatics.editAutomation(position - 1, automation)
        }
        automationViewHolder.tvAutomationName.text = automation.getName()
        automationViewHolder.tvAutomationEntityTriggerEntity.text = automation.getEntityName()
        when (automation.getType()) {
            Automation.TYPE_HEATING, Automation.TYPE_COOLING, Automation.TYPE_DEHUMIDIFICATION, Automation.TYPE_HUMIDIFICATION -> {
                automationViewHolder.tvAutomationEntityTriggerName.text = " ''%s''"
                        .format(automation.getTriggerName())
            }
            else -> {  // Automation.TYPE_EVENT
                automationViewHolder.tvAutomationEntityTriggerName.text = " ''%s'' %s"
                        .format(
                                automation.getTriggerName(),
                                automation.getEventName()
                        )
            }
        }
        automationViewHolder.tvAutomationUnitAction.text = automation.getUnitAction()
        when (automation.getType()) {
            Automation.TYPE_HEATING, Automation.TYPE_COOLING, Automation.TYPE_DEHUMIDIFICATION, Automation.TYPE_HUMIDIFICATION -> {
                automationViewHolder.tvAutomationUnitName.text = " ''%s'' %s"
                        .format(
                                automation.getUnitName(),
                                automation.getEventName()
                        )
            }
            else -> {  // Automation.TYPE_EVENT
                automationViewHolder.tvAutomationUnitName.text = " ''%s''"
                        .format(automation.getUnitName())
            }
        }
        automationViewHolder.scAutomationState.isChecked = automation.getState() == Automation.STATE_ON
        automationViewHolder.scAutomationState.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (isChecked) {
                    automation.setState(Automation.STATE_ON)
                } else {
                    automation.setState(Automation.STATE_OFF)
                }
                iAutomatics.updateAutomation(position - 1, automation)  // "-1" - HEADER
            }
        }

        if (Settings.isDeveloperMode()) {
            automationViewHolder.tvAutomationIndex.text = "[%02d]".format(automation.getIndex())
            automationViewHolder.tvAutomationIndex.visibility = View.VISIBLE
        } else {
            automationViewHolder.tvAutomationIndex.visibility = View.GONE
        }
    }
}
