package com.ediposouza.teslesgendstracker

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import com.ediposouza.teslesgendstracker.interactor.BaseInteractor
import com.ediposouza.teslesgendstracker.util.ConfigManager
import com.ediposouza.teslesgendstracker.util.LoggerManager
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */
class App : Application() {

    companion object {

        private var ctx: Context? = null

        var hasUserLogged: Boolean = false

        fun getVersion(): String {
            return ctx?.packageManager?.getPackageInfo(ctx?.packageName, 0)?.versionName ?: ""
        }

    }

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initializeDependencies()
    }

    private fun initializeDependencies() {
        MetricsManager.initialize(this)
        AndroidThreeTen.init(this)
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
            ConfigManager.updateCaches {
                val sync = !ConfigManager.isDBUpdating() && !ConfigManager.isVersionUnsupported()
                reference.child(BaseInteractor.NODE_CARDS).keepSynced(sync)
                reference.child(BaseInteractor.NODE_PATCHES).keepSynced(sync)
            }
        }
        Timber.plant(LoggerManager())
    }

}