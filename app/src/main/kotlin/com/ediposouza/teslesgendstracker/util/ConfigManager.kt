package com.ediposouza.teslesgendstracker.util

import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.BuildConfig
import com.ediposouza.teslesgendstracker.DEFAULT_DELIMITER
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Created by EdipoSouza on 12/30/16.
 */
object ConfigManager {

    val DB_UPDATE_CONFIG = "db_update"
    val SHOW_DECK_ADS_CONFIG = "showDeckAds"
    val VERSION_UNSUPPORTED_CONFIG = "version_unsupported"

    val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    init {
        with(remoteConfig) {
            setConfigSettings(FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build())
            setDefaults(mapOf(DB_UPDATE_CONFIG to false,
                    SHOW_DECK_ADS_CONFIG to false,
                    VERSION_UNSUPPORTED_CONFIG to ""))
        }
        updateCaches {}
    }

    fun updateCaches(onComplete: () -> Unit) {
        with(remoteConfig) {
            fetch(1).addOnCompleteListener {
                if (it.isSuccessful) {
                    activateFetched()
                    onComplete()
                }
            }
        }
    }

    fun isDBUpdating() = remoteConfig.getBoolean(DB_UPDATE_CONFIG)

    fun isShowDeckAds() = remoteConfig.getBoolean(SHOW_DECK_ADS_CONFIG)

    fun isVersionUnsupported(): Boolean {
        val unsupportedVersions = remoteConfig.getString(VERSION_UNSUPPORTED_CONFIG)
        if (unsupportedVersions.isEmpty()) {
            return false
        }
        if (!unsupportedVersions.contains(DEFAULT_DELIMITER)) {
            return unsupportedVersions == App.getVersion()
        }
        return unsupportedVersions.split(DEFAULT_DELIMITER).map(String::trim).contains(App.getVersion())
    }

}