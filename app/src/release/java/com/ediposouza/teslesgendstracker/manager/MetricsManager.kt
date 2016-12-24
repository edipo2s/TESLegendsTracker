package com.ediposouza.teslesgendstracker.manager

import android.content.Context
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.*
import com.ediposouza.teslesgendstracker.MetricAction
import com.ediposouza.teslesgendstracker.MetricScreen
import com.ediposouza.teslesgendstracker.MetricsConstants
import com.ediposouza.teslesgendstracker.data.Card
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import io.fabric.sdk.android.Fabric

/**
 * Created by ediposouza on 08/12/16.
 */
object MetricsManager : MetricsConstants() {

    var answers: Answers? = null
    var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        Fabric.with(context, Answers(), Crashlytics())
        answers = Answers.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun trackAction(action: MetricAction, vararg params: String) {
        val bundle = Bundle().apply {
            when (action) {
                is MetricAction.ACTION_COLLECTION_CARD_QTD_CHANGE ->
                    putString(MetricAction.ACTION_COLLECTION_CARD_QTD_CHANGE.PARAM_QTD, params[0])
                is MetricAction.ACTION_CARD_FILTER_ATTR ->
                    putString(MetricAction.ACTION_CARD_FILTER_ATTR.PARAM_ATTR, params[0])
                is MetricAction.ACTION_CARD_FILTER_RARITY ->
                    putString(MetricAction.ACTION_CARD_FILTER_RARITY.PARAM_RARITY, params[0])
                is MetricAction.ACTION_CARD_FILTER_MAGIKA ->
                    putString(MetricAction.ACTION_CARD_FILTER_MAGIKA.PARAM_MAGIKA, params[0])
            }
        }
        firebaseAnalytics?.logEvent(action.name, bundle)
        answers?.logCustom(CustomEvent(action.name))
    }

    fun trackScreen(screen: MetricScreen) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.VALUE, screen.name)
        }
        firebaseAnalytics?.logEvent(MetricScreen.EVENT_SCREEN_VIEW, bundle)
        answers?.logCustom(CustomEvent(MetricScreen.EVENT_SCREEN_VIEW)
                .putCustomAttribute(MetricScreen.PARAM_SCREEN_VIEW_SCREEN, screen.name))
    }

    fun trackSignUp() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, PARAM_SIGN_METHOD_VALUE_GOOGLE)
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
        answers?.logSignUp(SignUpEvent().putMethod(PARAM_SIGN_METHOD_VALUE_GOOGLE))
    }

    fun trackSignIn(user: FirebaseUser?, success: Boolean) {
        if (Fabric.isInitialized()) {
            Crashlytics.setUserIdentifier(user?.uid)
            Crashlytics.setUserName(user?.displayName)
            Crashlytics.setUserEmail(user?.email)
        }
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, PARAM_SIGN_METHOD_VALUE_GOOGLE)
        }
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
        answers?.logLogin(LoginEvent().putMethod(PARAM_SIGN_METHOD_VALUE_GOOGLE).putSuccess(success))
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
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, PARAM_CONTENT_VIEW_TYPE_CARD)
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