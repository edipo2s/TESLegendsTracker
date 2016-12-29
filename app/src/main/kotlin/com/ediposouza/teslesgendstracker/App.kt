package com.ediposouza.teslesgendstracker

import android.app.Application
import android.support.v7.app.AppCompatDelegate
import com.ediposouza.teslesgendstracker.interactor.BaseInteractor
import com.ediposouza.teslesgendstracker.manager.LoggerManager
import com.ediposouza.teslesgendstracker.manager.MetricsManager
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */
class App : Application() {

    companion object {

        var hasUserLogged: Boolean = false

    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initializeDependencies()
    }

    private fun initializeDependencies() {
        MetricsManager.initialize(this)
        AndroidThreeTen.init(this)
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
            reference.child(BaseInteractor.NODE_CARDS).keepSynced(true)
            reference.child(BaseInteractor.NODE_PATCHES).keepSynced(true)
        }
        Timber.plant(LoggerManager())
    }

}