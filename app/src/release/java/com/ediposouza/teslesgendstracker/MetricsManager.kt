package com.ediposouza.teslesgendstracker.ui.utils

import android.content.Context
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.*
import com.ediposouza.teslesgendstracker.data.Card
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import io.fabric.sdk.android.Fabric

/**
 * Created by ediposouza on 08/12/16.
 */
class MetricsManager() : MetricsManagerConstants() {

    companion object {

        private var static: MetricsManager? = null

        fun getInstance(): MetricsManager {
            if (static == null) {
                static = MetricsManager()
            }
            return static!!
        }

    }

    var answers: Answers? = null
    var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        Fabric.with(context, Answers(), Crashlytics())
        answers = Answers.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun trackScreen(@ScreenParam screen: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.VALUE, screen)
        }
        firebaseAnalytics?.logEvent(EVENT_SCREEN_VIEW, bundle)
        answers?.logCustom(CustomEvent(EVENT_SCREEN_VIEW).putCustomAttribute(PARAM_SCREEN, screen))
    }

    fun trackSignUp() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, SIGN_METHOD_GOOGLE)
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
        answers?.logSignUp(SignUpEvent().putMethod(SIGN_METHOD_GOOGLE))
    }

    fun trackSignIn(user: FirebaseUser?, success: Boolean) {
        if (Fabric.isInitialized()) {
            Crashlytics.setUserIdentifier(user?.uid)
            Crashlytics.setUserName(user?.displayName)
            Crashlytics.setUserEmail(user?.email)
        }
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, SIGN_METHOD_GOOGLE)
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
        answers?.logLogin(LoginEvent().putMethod(SIGN_METHOD_GOOGLE).putSuccess(success))
    }

    fun trackSearch(searchTerm: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
        answers?.logSearch(SearchEvent().putQuery(searchTerm))
    }

    fun trackCardView(card: Card) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, CONTENT_VIEW_TYPE_CARD)
            putString(FirebaseAnalytics.Param.ITEM_ID, card.shortName)
            putString(FirebaseAnalytics.Param.ITEM_NAME, card.name)
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, card.attr.name)
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        answers?.logContentView(ContentViewEvent()
                .putContentId(card.shortName)
                .putContentName(card.name)
                .putContentType(card.attr.name))
    }

}