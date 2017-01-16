package com.ediposouza.teslesgendstracker.ui.base

import android.os.Bundle
import android.support.v4.app.Fragment
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.util.ConfigManager
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.alertThemed
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */

open class BaseFragment : Fragment() {

    private val KEY_IS_FRAGMENT_SELECTED = "isFragmentSelectedKey"

    protected val eventBus: EventBus by lazy { EventBus.getDefault() }

    protected var isFragmentSelected: Boolean = false

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putBoolean(KEY_IS_FRAGMENT_SELECTED, isFragmentSelected)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            isFragmentSelected = savedInstanceState.getBoolean(KEY_IS_FRAGMENT_SELECTED)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            eventBus.register(this)
        } catch (e: Exception) {
            Timber.i(e.message)
        }
        ConfigManager.updateCaches {}
    }

    override fun onResume() {
        super.onResume()
        ConfigManager.updateCaches {
            if (ConfigManager.isDBUpdating()) {
                context.alertThemed(R.string.app_bd_under_updating, theme = R.style.AppDialog) {
                    okButton {
                        MetricsManager.trackAction(MetricAction.ACTION_NOTIFY_UPDATE())
                        System.exit(0)
                    }
                }.show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            eventBus.unregister(this)
        } catch (e: Exception) {
            Timber.i(e.message)
        }
    }

    override fun onDestroy() {
        MetricsManager.flush()
        super.onDestroy()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        isFragmentSelected = menuVisible
    }

    protected fun showLogin() {
        eventBus.post(CmdShowLogin())
    }

}
