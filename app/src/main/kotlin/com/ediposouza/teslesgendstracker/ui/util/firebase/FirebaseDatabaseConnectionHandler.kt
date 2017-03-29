package com.ediposouza.teslesgendstracker.ui.util.firebase

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.util.Log

import com.google.firebase.database.FirebaseDatabase

/**
 * Created by EdipoSouza on 3/28/17.
 */

class FirebaseDatabaseConnectionHandler : Application.ActivityLifecycleCallbacks {

    private var count = 0
    private val delayedTimeMillis: Long = 5000 // change this if you want different timeout
    private val mHandler = Handler()

    private val TAG = FirebaseDatabaseConnectionHandler::class.java.simpleName

    override fun onActivityCreated(activity: Activity?, bundle: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity?) {
        count++
        Log.d(TAG, "onActivityStarted: count=" + count)
        if (count > 0) {
            FirebaseDatabase.getInstance().goOnline()
        }
    }

    override fun onActivityResumed(activity: Activity?) {

    }

    override fun onActivityPaused(activity: Activity?) {

    }

    override fun onActivityStopped(activity: Activity?) {
        count--
        Log.d(TAG, "onActivityStopped: count=" + count)
        if (count == 0) {
            Log.d(TAG, "onActivityStopped: going offline in 5 seconds..")
            mHandler.postDelayed(Runnable {
                // just make sure that in the defined seconds no other activity is brought to front
                Log.d(TAG, "run: confirming if it is safe to go offline. Activity count: " + count)
                if (count == 0) {
                    Log.d(TAG, "run: going offline...")
                    FirebaseDatabase.getInstance().goOffline()
                } else {
                    Log.d(TAG, "run: Not going offline..")
                }
            }, delayedTimeMillis)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity?, bundle: Bundle?) {

    }

    override fun onActivityDestroyed(activity: Activity?) {

    }

}
