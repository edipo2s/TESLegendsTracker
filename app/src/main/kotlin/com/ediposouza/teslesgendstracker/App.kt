package com.ediposouza.teslesgendstracker

import android.content.Context
import android.preference.PreferenceManager
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import com.ediposouza.teslesgendstracker.interactor.BaseInteractor
import com.ediposouza.teslesgendstracker.ui.util.firebase.FirebaseDatabaseConnectionHandler
import com.ediposouza.teslesgendstracker.util.ConfigManager
import com.ediposouza.teslesgendstracker.util.LoggerManager
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.threetenabp.AndroidThreeTen
import hotchemi.android.rate.AppRate
import timber.log.Timber


/**
 * Created by EdipoSouza on 10/30/16.
 */
class App : MultiDexApplication() {

    companion object {

        private var ctx: Context? = null

        var currentLanguage: String = ""
        var hasUserAlreadyLogged: Boolean = false

        fun hasUserLogged() = FirebaseAuth.getInstance().currentUser != null

        fun hasUserDonated(): Boolean {
            return ConfigManager.isUserInAdsWhitelist() ||
                    PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(PREF_USER_DONATE, false)
        }

        fun shouldShowChangelog(): Boolean {
            return !PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(getVersion(), false)
        }

        fun setVersionChangelogViewed() {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(getVersion(), true).apply()
        }

        fun getVersion() = ctx?.packageManager?.getPackageInfo(ctx?.packageName, 0)?.versionName ?: ""

    }

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initializeDependencies()
        registerActivityLifecycleCallbacks(FirebaseDatabaseConnectionHandler())
    }

    private fun initializeDependencies() {
        Timber.plant(LoggerManager())
        MetricsManager.initialize(this)
        MetricsManager.trackAction(MetricAction.ACTION_APP_LAUNCH())
        AndroidThreeTen.init(this)
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
            ConfigManager.updateCaches {
                val sync = !ConfigManager.isDBUpdating() && !ConfigManager.isVersionUnsupported()
                reference.child(BaseInteractor.NODE_BASICS).keepSynced(sync)
                reference.child(BaseInteractor.NODE_CARDS).keepSynced(sync)
                reference.child(BaseInteractor.NODE_TOKENS).keepSynced(sync)
                reference.child(BaseInteractor.NODE_PATCHES).keepSynced(sync)
                reference.child(BaseInteractor.NODE_SEASONS).keepSynced(sync)
                reference.child(BaseInteractor.NODE_SPOILER).keepSynced(sync)
                reference.child(BaseInteractor.NODE_NEWS).keepSynced(sync)
            }
        }
        AppRate.with(this)
                .setInstallDays(10)
                .setLaunchTimes(10)
                .setRemindInterval(5)
                .setShowLaterButton(true)
                .setDebug(false)
                .monitor()
    }

}
