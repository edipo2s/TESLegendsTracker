package com.ediposouza.teslesgendstracker.util

import android.content.Context
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.*
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.BuildConfig
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.data.Patch
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crash.FirebaseCrash
import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.fabric.sdk.android.Fabric
import org.json.JSONObject
import timber.log.Timber
import java.math.BigDecimal
import java.util.*

/**
 * Created by ediposouza on 08/12/16.
 */
object MetricsManager : MetricsConstants() {

    var gcmSender: String = ""
    var answers: Answers? = null
    var firebaseAnalytics: FirebaseAnalytics? = null
    var mixpanelAnalytics: MixpanelAPI? = null

    fun initialize(context: Context) {
        if (BuildConfig.PREPARE_TO_RELEASE) {
            Fabric.with(context, Answers(), Crashlytics())
        } else {
            Timber.w("Fabric not initialized")
        }
        gcmSender = context.getString(R.string.gcm_defaultSenderId)
        answers = Answers.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        mixpanelAnalytics = MixpanelAPI.getInstance(context, context.getString(R.string.mixpanel_app_id))
    }

    fun flush() {
        mixpanelAnalytics?.flush()
    }

    fun trackAction(action: MetricAction) {
        val bundle = Bundle().apply {
            when (action) {
                is MetricAction.ACTION_COLLECTION_CARD_RANTING -> {
                    putString(action.PARAM_CARD, action.card.shortName)
                    putInt(action.PARAM_RATING, action.rating)
                }
                is MetricAction.ACTION_COLLECTION_CARD_QTD_CHANGE -> {
                    putString(action.PARAM_CARD, action.card.shortName)
                    putInt(action.PARAM_QTD, action.qtd)
                }
                is MetricAction.ACTION_CARD_FULL_ART ->
                    putString(action.PARAM_CARD_NAME, action.card.name)
                is MetricAction.ACTION_CARD_FULL_ART_DOWNLOAD ->
                    putString(action.PARAM_CARD_NAME, action.card.name)
                is MetricAction.ACTION_CARD_FULL_ART_SET_WALLPAPER ->
                    putString(action.PARAM_CARD_NAME, action.card.name)
                is MetricAction.ACTION_CARD_START_SOUND_PLAY ->
                    putString(action.PARAM_CARD_NAME, action.card.name)
                is MetricAction.ACTION_CARD_START_SOUND_ATTACK ->
                    putString(action.PARAM_CARD_NAME, action.card.name)
                is MetricAction.ACTION_CARD_START_SOUND_EXTRA ->
                    putString(action.PARAM_CARD_NAME, action.card.name)
                is MetricAction.ACTION_CARD_SOUND_SET_RINGTONE -> {
                    putString(action.PARAM_CARD_NAME, action.card.name)
                    putString(action.PARAM_CARD_SOUND_TYPE, action.soundType)
                }
                is MetricAction.ACTION_CARD_FILTER_FAVORITE ->
                    putBoolean(action.PARAM_CHECKED, action.checked)
                is MetricAction.ACTION_CARD_FILTER_SET ->
                    putString(action.PARAM_SET, action.set?.name ?: MetricAction.CLEAR)
                is MetricAction.ACTION_CARD_FILTER_ATTR ->
                    putString(action.PARAM_ATTR, action.attr?.name ?: MetricAction.CLEAR)
                is MetricAction.ACTION_CARD_FILTER_RARITY ->
                    putString(action.PARAM_RARITY, action.rarity?.name ?: MetricAction.CLEAR)
                is MetricAction.ACTION_CARD_FILTER_MAGICKA ->
                    putString(action.PARAM_MAGICKA, if (action.magicka > 0) action.magicka.toString() else MetricAction.CLEAR)
                is MetricAction.ACTION_NEW_DECK_SAVE -> {
                    putString(action.PARAM_TYPE, action.type)
                    putString(action.PARAM_PATCH, action.patch)
                    putString(action.PARAM_PRIVATE, action.private.toString())
                }
                is MetricAction.ACTION_DECK_UPDATE -> {
                    putString(action.PARAM_TYPE, action.type)
                    putString(action.PARAM_PATCH, action.patch)
                    putString(action.PARAM_PRIVATE, action.private.toString())
                }
                is MetricAction.ACTION_MATCH_STATISTICS_WIN_RATE ->
                    putBoolean(action.PARAM_CHECKED, action.checked)
                is MetricAction.ACTION_MATCH_STATISTICS_CLASS_WIN_RATE ->
                    putBoolean(action.PARAM_CHECKED, action.checked)
                is MetricAction.ACTION_MATCH_STATISTICS_HIDE_EMPTY ->
                    putBoolean(action.PARAM_CHECKED, action.checked)
                is MetricAction.ACTION_MATCH_STATISTICS_CLASS_HIDE_EMPTY ->
                    putBoolean(action.PARAM_CHECKED, action.checked)
                is MetricAction.ACTION_MATCH_STATISTICS_FILTER_MODE ->
                    putString(action.PARAM_MODE, action.mode.name)
                is MetricAction.ACTION_MATCH_STATISTICS_FILTER_SEASON ->
                    putString(action.PARAM_SEASON, action.season?.uuid ?: MetricAction.ALL)
                is MetricAction.ACTION_MATCH_STATISTICS_CLASS_FILTER_SEASON ->
                    putString(action.PARAM_SEASON, action.season?.uuid ?: MetricAction.ALL)
                is MetricAction.ACTION_MATCH_STATISTICS_CLASS ->
                    putString(action.PARAM_CLASS, action.cls.name)
                is MetricAction.ACTION_NEW_MATCH_START_WITH -> {
                    putString(action.PARAM_DECK, action.deck?.cls?.name ?: action.PARAM_DECK_VALUE_OTHER)
                    putBoolean(action.PARAM_FROM_ARENA, action.fromArena)
                }
                is MetricAction.ACTION_NEW_MATCH_SAVE -> {
                    putString(action.PARAM_MY_CLS, action.myDeckCls.name)
                    putString(action.PARAM_MY_TYPE, action.myDeckType.name)
                    putString(action.PARAM_OPT_CLS, action.optDeckCls.name)
                    putString(action.PARAM_OPT_TYPE, action.optDeckType.name)
                    putString(action.PARAM_MODE, action.mode.name)
                    putString(action.PARAM_SEASON, action.season)
                    putBoolean(action.PARAM_LEGEND, action.legendRank)
                }
                is MetricAction.ACTION_DONATE_BASIC,
                is MetricAction.ACTION_DONATE_PRO -> {
                    val value = 6L.takeIf { action is MetricAction.ACTION_DONATE_BASIC } ?: 13L
                    val valueCurrency = "BRL"
                    answers?.logPurchase(PurchaseEvent()
                            .putItemName(action.name)
                            .putItemPrice(BigDecimal.valueOf(value))
                            .putCurrency(Currency.getInstance(valueCurrency))
                            .putSuccess(true))
                    firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.PURCHASE_REFUND, Bundle().apply {
                        putString(FirebaseAnalytics.Param.ITEM_NAME, action.name)
                        putString(FirebaseAnalytics.Param.CURRENCY, valueCurrency)
                        putDouble(FirebaseAnalytics.Param.VALUE, value.toDouble())
                    })
                    mixpanelAnalytics?.people?.trackCharge(value.toDouble(), JSONObject(mutableMapOf<String, Any>().apply {
                        put(FirebaseAnalytics.Param.ITEM_NAME, action.name)
                    }))
                }
                is MetricAction.ACTION_IMPORT_COLLECTION_FINISH ->
                    putInt(action.PARAM_CARDS_IMPORTED, action.cardsImported)
                is MetricAction.ACTION_IMPORT_DECKS_FINISH ->
                    putInt(action.PARAM_DECKS_IMPORTED, action.decksImported)
                is MetricAction.ACTION_ARTICLES_VIEW_NEWS ->
                    putString(action.PARAM_ARTICLE, action.article.uuidDate)
                is MetricAction.ACTION_ARTICLES_VIEW_WORLD ->
                    putString(action.PARAM_ARTICLE, action.article.uuidDate)
                is MetricAction.ACTION_ARENA_FILTER_SEASON ->
                    putString(action.PARAM_SEASON, action.season?.uuid ?: MetricAction.ALL)
                is MetricAction.ACTION_ARENA_START -> {
                    putString(action.PARAM_CLASS, action.cls.name)
                    putBoolean(action.PARAM_FROM_START_MENU, action.fromStartMenu)
                }
                is MetricAction.ACTION_ARENA_PICK ->
                    putString(action.PARAM_CARD, action.card.shortName)
                is MetricAction.ACTION_DECK_CHANGE_VIEW_MODE ->
                    putBoolean(action.PARAM_COMPACT_MODE, action.compatMode)
                is MetricAction.ACTION_DEEPLINK -> {
                    putString(action.PARAM_PATH, action.path)
                    putString(action.PARAM_PATH_PARAMS, action.params)
                }
                is MetricAction.ACTION_SHARE -> answers?.logShare(ShareEvent())
                is MetricAction.ACTION_DECK_CHANGE_LANGUAGE ->
                    putString(action.PARAM_LANGUAGE, action.language)
            }
        }
        answers?.logCustom(CustomEvent(action.name))
        firebaseAnalytics?.logEvent(action.name, bundle)
        mixpanelAnalytics?.trackBundle(action.name, bundle)
    }

    fun trackScreen(screen: MetricScreen) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.VALUE, screen.name)
        }
        answers?.logCustom(CustomEvent(MetricScreen.EVENT_SCREEN_VIEW)
                .putCustomAttribute(MetricScreen.PARAM_SCREEN_VIEW_SCREEN, screen.name))
        firebaseAnalytics?.logEvent(MetricScreen.EVENT_SCREEN_VIEW, bundle)
        mixpanelAnalytics?.trackBundle(MetricScreen.EVENT_SCREEN_VIEW, bundle)
    }

    fun trackSignUp(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, method)
        }
        answers?.logSignUp(SignUpEvent().putMethod(method))
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
        mixpanelAnalytics?.trackMap(EVENT_SIGNUP, mapOf(PARAM_SIGN_METHOD to method))
    }

    fun trackSignIn(user: FirebaseUser?, success: Boolean, method: String) {
        identifyUser(user)
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, method)
        }
        answers?.logLogin(LoginEvent().putMethod(method).putSuccess(success))
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
        mixpanelAnalytics?.trackMap(EVENT_LOGIN, mapOf(PARAM_SIGN_METHOD to method))
    }

    private fun identifyUser(user: FirebaseUser?) {
        val userId = user?.uid
        if (Crashlytics.getInstance() != null && BuildConfig.PREPARE_TO_RELEASE) {
            Crashlytics.setUserIdentifier(userId)
            Crashlytics.setUserName(user?.displayName)
            Crashlytics.setUserEmail(user?.email)
        }
        mixpanelAnalytics?.identify(userId)
        mixpanelAnalytics?.people?.apply {
            identify(userId)
            initPushHandling(gcmSender)
            set(PARAM_MIXPANEL_USER_NAME, user?.displayName)
            set(PARAM_MIXPANEL_USER_EMAIL, user?.email)
            set(PARAM_MIXPANEL_USER_DONATE, App.hasUserDonated())
        }
        mixpanelAnalytics?.registerSuperPropertiesMap(mapOf(PARAM_MIXPANEL_USER_ID to userId))
        FirebaseCrash.log(userId)
    }

    fun trackSearch(searchTerm: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
        }
        answers?.logSearch(SearchEvent().putQuery(searchTerm))
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
        mixpanelAnalytics?.trackBundle(EVENT_SEARCH, bundle)
    }

    fun trackCardView(card: Card) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, PARAM_CONTENT_VIEW_TYPE_CARD)
            putString(FirebaseAnalytics.Param.ITEM_ID, card.shortName)
            putString(FirebaseAnalytics.Param.ITEM_NAME, card.name)
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, card.attr.name)
        }
        answers?.logContentView(ContentViewEvent()
                .putContentId(card.shortName)
                .putContentName(card.name)
                .putContentType(card.attr.name))
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        mixpanelAnalytics?.trackMap(EVENT_VIEW_CARD, mapOf(
                PARAM_VIEW_CARD_ID to card.shortName,
                PARAM_VIEW_CARD_NAME to card.name,
                PARAM_VIEW_CARD_ATTR to card.attr.name))
    }

    fun trackDeckView(deck: Deck) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, PARAM_CONTENT_VIEW_TYPE_DECK)
            putString(FirebaseAnalytics.Param.ITEM_ID, deck.uuid)
            putString(FirebaseAnalytics.Param.ITEM_NAME, deck.name)
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, deck.cls.name)
        }
        answers?.logContentView(ContentViewEvent()
                .putContentId(deck.uuid)
                .putContentName(deck.name)
                .putContentType(deck.cls.name))
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        mixpanelAnalytics?.trackMap(EVENT_VIEW_DECK, mapOf(
                PARAM_VIEW_DECK_ID to deck.uuid,
                PARAM_VIEW_DECK_NAME to deck.name,
                PARAM_VIEW_DECK_CLASS to deck.cls.name))
    }

    fun trackPatchView(patch: Patch) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, PARAM_CONTENT_VIEW_TYPE_PATCH)
            putString(FirebaseAnalytics.Param.ITEM_ID, patch.uuidDate)
            putString(FirebaseAnalytics.Param.ITEM_NAME, patch.desc)
        }
        answers?.logContentView(ContentViewEvent()
                .putContentId(patch.uuidDate)
                .putContentName(patch.desc))
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        mixpanelAnalytics?.trackMap(EVENT_VIEW_DECK, mapOf(
                PARAM_VIEW_DECK_ID to patch.uuidDate,
                PARAM_VIEW_DECK_NAME to patch.desc))
    }

}