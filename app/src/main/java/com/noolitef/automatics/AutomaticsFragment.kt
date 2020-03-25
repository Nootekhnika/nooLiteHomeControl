package com.noolitef.automatics

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.noolitef.HomeActivity
import com.noolitef.PRF64
import com.noolitef.R
import com.noolitef.settings.Settings
import okhttp3.*
import java.io.IOException
import java.nio.charset.Charset


class AutomaticsFragment : Fragment(), IAutomatics, IAutomationFragment {

    private lateinit var srlAutomationList: SwipeRefreshLayout
    private lateinit var rvAutomationList: RecyclerView
    private lateinit var automationListAdapter: AutomationListAdapter
    private lateinit var vDisableContent: View

    private lateinit var nooLitePRF64: PRF64
    private lateinit var httpClient: OkHttpClient
    private var automatics: ArrayList<Automation>? = null


    // CONSTRUCTOR

    companion object {

        @JvmStatic
        fun getInstance(fragmentManager: FragmentManager, nooLitePRF64: PRF64, httpClient: OkHttpClient): AutomaticsFragment {

            var fragment = fragmentManager.findFragmentByTag(AutomaticsFragment::class.java.simpleName)

            if (fragment == null) {
                fragment = AutomaticsFragment()
            }
            val automaticsFragment = fragment as AutomaticsFragment
            automaticsFragment.setPRF64(nooLitePRF64)
            automaticsFragment.setHttpClient(httpClient)

            return fragment
        }
    }

    private fun setPRF64(nooLitePRF64: PRF64) {

        this.nooLitePRF64 = nooLitePRF64
    }

    private fun setHttpClient(httpClient: OkHttpClient) {

        this.httpClient = httpClient
    }


    // LIFECYCLE

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return initView(
                inflater.inflate(
                        R.layout.fragment_automatics, container, false
                )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        getAuto(false)
    }

    // crutch
    override fun onDestroy() {

        val fragment = fragmentManager?.findFragmentByTag(AutomationFragment::class.java.simpleName)

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

        initUpdateLayout(view)
        initAutomaticsList(view)
        vDisableContent = view.findViewById(R.id.fragment_automatics_disable_content_view)

        return view
    }

    private fun initUpdateLayout(view: View) {

        srlAutomationList = view.findViewById(R.id.fragment_automatics_swipe_refresh_layout)
        srlAutomationList.setProgressViewEndTarget(
                true,
                resources.getDimensionPixelOffset(R.dimen.swipe_progressbar_toolbar_offset) + resources.getDimensionPixelOffset(R.dimen.dp_16)
        )
        srlAutomationList.setColorSchemeResources(R.color.black_light)
        srlAutomationList.setOnRefreshListener { getAuto(true) }
    }

    private fun initAutomaticsList(view: View) {

        rvAutomationList = view.findViewById(R.id.fragment_automatics_recycler_view)
        rvAutomationList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvAutomationList.itemAnimator = DefaultItemAnimator()
        rvAutomationList.setHasFixedSize(true)
        automationListAdapter = AutomationListAdapter(this)
        automationListAdapter.setHasStableIds(true)
        rvAutomationList.adapter = automationListAdapter
    }


    // INTERFACES

    fun showPopupMenu(view: View) {

        val popupMenu: PopupMenu = if (Settings.isNightMode()) {
            val context: Context = ContextThemeWrapper(activity, R.style.PopupMenuDark)
            PopupMenu(context, view)
        } else {
            PopupMenu(activity, view)
        }
        popupMenu.inflate(R.menu.fragment_automatics_popup_menu)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem ->
            if (!isAdded) return@OnMenuItemClickListener false

            when (menuItem.itemId) {
                R.id.fragment_automatics_popup_menu_item_update -> {
                    getAuto(true)

                    return@OnMenuItemClickListener true
                }
                R.id.fragment_automatics_popup_menu_item_new -> {
                    if (srlAutomationList.isRefreshing) return@OnMenuItemClickListener false

                    if (automatics != null) {
                        editAutomation(-1, null)
                    } else {
                        getAuto(true)
                    }

                    return@OnMenuItemClickListener true
                }
            }

            return@OnMenuItemClickListener false
        })
        popupMenu.show()
    }

    private fun setUpdating(visible: Boolean) {

        if (!isAdded) return

        activity?.runOnUiThread {
            disableView(visible)
            srlAutomationList.isRefreshing = visible
        }
    }

    private fun disableView(disabled: Boolean) {

        if (!isAdded) return

        if (disabled) {
            vDisableContent.visibility = View.VISIBLE
        } else {
            vDisableContent.visibility = View.GONE
        }
    }

    private fun updateAutomatics(automatics: ArrayList<Automation>) {

        if (!isAdded) return

        activity?.runOnUiThread {
            automationListAdapter.update(automatics)
            setUpdating(false)
        }
    }

    private fun showSnack(message: String) {

        if (!isAdded) return

        activity?.runOnUiThread {
            (activity as HomeActivity).showSnackBar(message, 0, Snackbar.LENGTH_SHORT)
        }
    }


    // CALLBACKS

    override fun editAutomation(position: Int, automation: Automation?) {

        val automationFragment = AutomationFragment.getInstance(childFragmentManager, nooLitePRF64, automation, httpClient, this)
        if (automationFragment.isAdded) return
        automationFragment.show(fragmentManager, AutomationFragment::class.java.simpleName)
    }

    override fun updateAutomation(position: Int, automation: Automation) {

        setUpdating(true)

        Thread(Runnable {
            automatics!![position] = automation
            postAuto(automatics!!.toAuto())
        }).start()
    }

    override fun onDismiss(update: Boolean) {

        if (update) {
            getAuto(true)
        }
    }


    // MODEL

    private fun getAuto(force: Boolean) {

        setUpdating(true)

        if (!force && nooLitePRF64.getAuto()[0].toInt() != -1) {
            automatics = nooLitePRF64.getAutomatics()
            updateAutomatics(automatics!!)

            return
        }

        val request = Request.Builder()
                .url(Settings.URL() + "auto.bin")
                .get()
                .build()
        val call = httpClient.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                call.cancel()
                showSnack(getStringByResourceId(R.string.no_connection))
                setUpdating(false)
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful) {
                    val file = if (response.body() != null) response.body()!!.bytes() else ByteArray(0)
                    call.cancel()

                    if (file.size == 12294) {
                        nooLitePRF64.setAuto(file)
                        automatics = nooLitePRF64.getAutomatics()
                        updateAutomatics(automatics!!)
                    } else {
                        showSnack(getStringByResourceId(R.string.no_response))
                        setUpdating(false)
                    }
                } else {
                    call.cancel()
                    showSnack("%s %s".format(getStringByResourceId(R.string.connection_error), response.code()))
                    setUpdating(false)
                }
            }
        })
    }

    private fun ArrayList<Automation>.toAuto(): ByteArray {

        val auto = ByteArray(12294) { -1 }
        // auto.bin file prefix in ASCII (Win-1251/cp1251)
        auto[0] = 80  // 'P'
        auto[1] = 82  // 'R'
        auto[2] = 70  // 'F'
        auto[3] = 54  // '6'
        auto[4] = 52  // '4'
        auto[5] = 65  // 'A'

        var autoByte = 4102
        var autoStep: Int
        for (automationIndex in this.indices) {
            // name
            val automationName: ByteArray = this[automationIndex].getName().toByteArray(Charset.forName("cp1251"))
            for (autoNameByte in 0..31) {
                if (autoNameByte < automationName.size) {
                    auto[6 + (32 * automationIndex) + autoNameByte] = automationName[autoNameByte]
                } else {
                    auto[6 + (32 * automationIndex) + autoNameByte] = 0
                }
            }
            // command
            for (autoItemByte in 0..272) {
                auto[autoByte + autoItemByte] = this[automationIndex].getAutoItem()[autoItemByte]
            }

            autoStep =
                    if (automationIndex == 14) 274
                    else 273
            autoByte += autoStep
        }

        return auto
    }

    private fun postAuto(auto: ByteArray) {

        val body = "\r\n\r\nContent-Disposition: form-data; name=\"auto\"; filename=\"auto.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"
                .plus(String(auto, Charset.forName("cp1251")))
                .plus("\r\n\r\n\r\n")
        val request = Request.Builder()
                .url(Settings.URL() + "sett_eic.htm")
                .post(RequestBody.create(null, body.toByteArray(Charset.forName("cp1251"))))
                .build()
        val call = httpClient.newCall(request)

        activity?.runOnUiThread {
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                    call.cancel()
                    showSnack(getStringByResourceId(R.string.no_connection))
                    setUpdating(false)
                }

                override fun onResponse(call: Call, response: Response) {

                    if (response.isSuccessful) {
                        nooLitePRF64.setAuto(auto)
                    } else {
                        showSnack("%s %s".format(getStringByResourceId(R.string.connection_error), response.code()))
                    }
                    call.cancel()
                    setUpdating(false)
                }
            })
        }
    }

    private fun getStringByResourceId(resId: Int): String {

        var string = ""

        if (isAdded) {
            string = getString(resId)
        }

        return string
    }
}
