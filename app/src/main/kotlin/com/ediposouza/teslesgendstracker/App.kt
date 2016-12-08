package com.ediposouza.teslesgendstracker

import android.app.Application
import android.support.v7.app.AppCompatDelegate
import com.ediposouza.teslesgendstracker.ui.utils.MetricsManager
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */
class App : Application() {

    val NODE_CARDS = "cards"

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initializeDependencies()
    }

    private fun initializeDependencies() {
        MetricsManager.getInstance().initialize(this)
        AndroidThreeTen.init(this)
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
            reference.child(NODE_CARDS).keepSynced(true)
        }
        Timber.plant(LoggerManager())
    }

}