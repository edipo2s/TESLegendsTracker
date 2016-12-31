package com.ediposouza.teslesgendstracker.ui.base

import android.support.v4.app.Fragment
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.util.ConfigManager
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricsManager
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.alert
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */

open class BaseFragment : Fragment() {

    protected val eventBus: EventBus by lazy { EventBus.getDefault() }

    protected var isFragmentSelected: Boolean = false

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
                activity.alert(getString(R.string.app_bd_under_updating)) {
                    okButton {
                        MetricsManager.trackAction(MetricAction.ACTION_NOTIFY_UPDATE())
                        System.exit(0)
                    }
                    activity.setTheme(R.style.AppDialog)
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

}
