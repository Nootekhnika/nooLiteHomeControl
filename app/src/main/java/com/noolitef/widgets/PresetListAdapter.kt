package com.noolitef.widgets

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.noolitef.R
import com.noolitef.presets.Preset


class PresetListAdapter(
        private val activity: WidgetPresetConfigureActivity,
        presets: ArrayList<Preset>
) : RecyclerView.Adapter<ViewHolder>() {

    inner class HomeActivityViewHolder(itemView: View) : ViewHolder(itemView) {
        var layoutClickable = itemView.findViewById<RelativeLayout>(R.id.card_view_preset_list_item_home_activity_layout_clickable)!!
    }

    inner class PresetViewHolder(itemView: View) : ViewHolder(itemView) {
        var layoutClickable = itemView.findViewById<RelativeLayout>(R.id.card_view_preset_list_item_layout_clickable)!!
        var textName = itemView.findViewById<TextView>(R.id.card_view_preset_list_item_text_name)!!
    }

    inner class FooterViewHolder(itemView: View) : ViewHolder(itemView)

    companion object {
        const val HOME_ACTIVITY = 0
        const val PRESET = 1
        const val FOOTER = 2
    }

    private var presetList: ArrayList<Any> = ArrayList()

    init {
        presetList.addAll(presets)
        if (presetList.size == 0)
            presetList.add(PresetListHomeActivity())
        else
            presetList.add(PresetListFooter())
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return presetList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (presetList[position]) {
            is PresetListHomeActivity -> HOME_ACTIVITY
            is Preset -> PRESET
            is PresetListFooter -> FOOTER
            else -> FOOTER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            HOME_ACTIVITY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_preset_list_item_home_activity, parent, false)
                HomeActivityViewHolder(view)
            }
            PRESET -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_preset_list_item, parent, false)
                PresetViewHolder(view)
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
        when (holder.itemViewType) {
            HOME_ACTIVITY -> bindHomeActivity(holder, position)
            PRESET -> bindPreset(holder, position)
        }
    }

    private fun bindHomeActivity(holder: ViewHolder, position: Int) {
        val homeActivityViewHolder: HomeActivityViewHolder = holder as HomeActivityViewHolder
        homeActivityViewHolder.layoutClickable.setOnClickListener {
            activity.runHomeActivity()
        }
    }

    private fun bindPreset(holder: ViewHolder, position: Int) {
        val presetViewHolder: PresetViewHolder = holder as PresetViewHolder
        val preset = presetList[position] as Preset
        presetViewHolder.layoutClickable.setOnClickListener {
            activity.setupWidgetPreset(preset.index, preset.name, getPresetRunTime(preset))
        }
        presetViewHolder.textName.text = preset.name
    }

    private fun getPresetRunTime(preset: Preset): Long {
        var runTime: Long = 250

        for (i in 0..72) {
            if (preset.getCommand(i)[0] == 255.toByte()) {
                break
            }
            runTime += 250
        }

        return runTime
    }
}