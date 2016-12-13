package com.ediposouza.teslesgendstracker.ui.base

import android.support.v4.app.Fragment
import com.ediposouza.teslesgendstracker.manager.MetricManager
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */

open class BaseFragment : Fragment() {

    protected val eventBus by lazy { EventBus.getDefault() }
    protected val metricsManager by lazy { MetricManager.getInstance() }

    protected var fragmentSelected: Boolean = false

    override fun onStart() {
        super.onStart()
        try {
            eventBus.register(this)
        } catch (e: Exception) {
            Timber.i(e.message)
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

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        fragmentSelected = menuVisible
    }

}
