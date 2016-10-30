package com.ediposouza.teslesgendstracker

import android.util.Log
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */
class LoggerManager : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return BuildConfig.ENABLE_LOGS_IN_RELEASE
        }
        return super.isLoggable(tag, priority)
    }

    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (!isLoggable(tag, priority)) {
            return
        }
        if (priority == Log.ASSERT) {
            Log.wtf(tag, message)
        } else {
            Log.println(priority, tag, message)
        }
    }

}