package com.ediposouza.teslesgendstracker.ui.cards

import android.support.v4.app.Fragment
import com.ediposouza.teslesgendstracker.ui.utils.MetricsManager
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */

open class BaseFragment : Fragment() {

    protected val eventBus by lazy { EventBus.getDefault() }
    protected val metricsManager by lazy { MetricsManager.getInstance() }

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

}
