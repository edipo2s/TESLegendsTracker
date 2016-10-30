package com.ediposouza.teslesgendstracker.ui.cards

import android.os.Bundle
import android.support.v4.app.Fragment
import icepick.Icepick
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */

open class BaseFragment : Fragment() {

    protected val mEventBus by lazy { EventBus.getDefault() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Icepick.restoreInstanceState(this, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }

    override fun onStart() {
        super.onStart()
        try {
            mEventBus.register(this)
        } catch (e: Exception) {
            Timber.i(e.message)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            mEventBus.unregister(this)
        } catch (e: Exception) {
            Timber.i(e.message)
        }
    }

}
