package com.ediposouza.teslesgendstracker.util

import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.BuildConfig
import com.ediposouza.teslesgendstracker.DEFAULT_DELIMITER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Created by EdipoSouza on 12/30/16.
 */
object ConfigManager {

    val DB_UPDATE_CONFIG = "db_update"
    val ADS_WHITELIST_CONFIG = "ads_whitelist"
    val SHOW_WABBATRACK_CONFIG = "show_wabbatrack"
    val VERSION_UNSUPPORTED_CONFIG = "version_unsupported"
    val SEARCH_ANIMAL_CONFIG = "search_animals"
    val SEARCH_UNDEAD_CONFIG = "search_undead"

    val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    init {
        with(remoteConfig) {
            setConfigSettings(FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build())
            setDefaults(mapOf(DB_UPDATE_CONFIG to false,
                    ADS_WHITELIST_CONFIG to "",
                    SEARCH_ANIMAL_CONFIG to "beast, fish, mammoth, mudcrab, reptile, spider, wolf",
                    SEARCH_UNDEAD_CONFIG to "mummy, skeleton, spirit, vampire",
                    SHOW_WABBATRACK_CONFIG to false,
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

    fun animalRaces() = remoteConfig.getString(SEARCH_ANIMAL_CONFIG)

    fun undeadRaces() = remoteConfig.getString(SEARCH_UNDEAD_CONFIG)

    fun isDBUpdating() = remoteConfig.getBoolean(DB_UPDATE_CONFIG)

    fun isToShowWabbatrack() = remoteConfig.getBoolean(SHOW_WABBATRACK_CONFIG)

    fun isUserInAdsWhitelist(): Boolean {
        val adsWhitelist = remoteConfig.getString(ADS_WHITELIST_CONFIG)
        if (adsWhitelist.isEmpty() || !App.hasUserLogged()) {
            return false
        }
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "a'@b.c"
        if (!adsWhitelist.contains(DEFAULT_DELIMITER)) {
            return adsWhitelist == userEmail
        }
        return adsWhitelist.split(DEFAULT_DELIMITER).map(String::trim).contains(userEmail)
    }

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