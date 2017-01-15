package com.ediposouza.teslesgendstracker.util

import com.ediposouza.teslesgendstracker.data.*

/**
 * Created by ediposouza on 08/12/16.
 */
abstract class MetricsConstants {

    companion object {

        const val EVENT_LOGIN = "Login"
        const val EVENT_SIGNUP = "SignUp"
        const val PARAM_SIGN_METHOD = "method"
        const val PARAM_SIGN_METHOD_VALUE_GOOGLE = "Google"

        const val EVENT_VIEW_CARD = "ViewCard"
        const val PARAM_VIEW_CARD_ID = "ID"
        const val PARAM_VIEW_CARD_NAME = "Name"
        const val PARAM_VIEW_CARD_ATTR = "Attr"

        const val EVENT_SEARCH = "Search"

        const val PARAM_CONTENT_VIEW_TYPE_CARD = "Card"

        const val PARAM_MIXPANEL_USER_ID = "User ID"
        const val PARAM_MIXPANEL_USER_NAME = "\$name"
        const val PARAM_MIXPANEL_USER_EMAIL = "\$email"
    }

}

sealed class MetricAction(val name: String) {

    companion object {
        const val ALL = "All"
        const val CLEAR = "Clear"
    }

    class ACTION_CARD_DETAILS_EXPAND : MetricAction("CardDetailsExpand")
    class ACTION_CARD_DETAILS_COLLAPSE : MetricAction("CardDetailsCollapse")
    class ACTION_CARD_DETAILS_CLOSE_TAP : MetricAction("CardDetailsCloseTap")
    class ACTION_CARD_DETAILS_FAVORITE : MetricAction("CardDetailsFavorite")
    class ACTION_CARD_DETAILS_UNFAVORITE : MetricAction("CardDetailsUnfavorite")
    class ACTION_COLLECTION_STATISTICS_EXPAND : MetricAction("CollectionStatisticsExpand")
    class ACTION_COLLECTION_STATISTICS_COLLAPSE : MetricAction("CollectionStatisticsCollapse")

    class ACTION_COLLECTION_CARD_QTD_CHANGE(val qtd: Int) : MetricAction("CollectionCardQtdChange") {
        val PARAM_QTD = "Qtd"
    }

    class ACTION_CARD_FILTER_SET(val set: CardSet?) : MetricAction("FilterCardSet") {
        val PARAM_SET = "Set"
    }

    class ACTION_CARD_FILTER_ATTR(val attr: Attribute?) : MetricAction("FilterCardAttr") {
        val PARAM_ATTR = "Attr"
    }

    class ACTION_CARD_FILTER_RARITY(val rarity: CardRarity?) : MetricAction("FilterCardRarity") {
        val PARAM_RARITY = "Rarity"
    }

    class ACTION_CARD_FILTER_MAGIKA(val magika: Int) : MetricAction("FilterCardMagika") {
        val PARAM_MAGIKA = "Magika"
    }

    class ACTION_NOTIFY_UPDATE : MetricAction("NotifyUpdate")
    class ACTION_VERSION_UNSUPPORTED : MetricAction("VersionUnsupported")
    class ACTION_DECK_DETAILS_DELETE : MetricAction("DeckDetailsDelete")
    class ACTION_DECK_DETAILS_LIKE : MetricAction("DeckDetailsLike")
    class ACTION_DECK_DETAILS_UNLIKE : MetricAction("DeckDetailsUnlike")
    class ACTION_DECK_DETAILS_FAVORITE : MetricAction("DeckDetailsFavorite")
    class ACTION_DECK_DETAILS_UNFAVORITE : MetricAction("DeckDetailsUnfavorite")
    class ACTION_DECK_COMMENTS_EXPAND : MetricAction("DeckCommentExpand")
    class ACTION_DECK_COMMENTS_COLLAPSE : MetricAction("DeckCommentCollapse")
    class ACTION_DECK_COMMENTS_SEND : MetricAction("DeckCommentSend")

    class ACTION_NEW_DECK_SAVE(val type: String, val patch: String, val private: Boolean) : MetricAction("DeckNew") {
        val PARAM_TYPE = "Type"
        val PARAM_PATCH = "Patch"
        val PARAM_PRIVATE = "Private"
    }

    class ACTION_MATCH_STATISTICS_WIN_RATE(val checked: Boolean) : MetricAction("MatchStatisticsWinRate") {
        val PARAM_CHECKED = "Checked"
    }

    class ACTION_MATCH_STATISTICS_CLASS_WIN_RATE(val checked: Boolean) : MetricAction("MatchStatisticsClassWinRate") {
        val PARAM_CHECKED = "Checked"
    }

    class ACTION_MATCH_STATISTICS_FILTER_MODE(val mode: MatchMode) : MetricAction("FilterMatchMode") {
        val PARAM_MODE = "Mode"
    }

    class ACTION_MATCH_STATISTICS_FILTER_SEASON(val season: Season?) : MetricAction("FilterMatchStatisticsSeason") {
        val PARAM_SEASON = "Season"
    }

    class ACTION_MATCH_STATISTICS_CLASS_FILTER_SEASON(val season: Season?) : MetricAction("FilterMatchStatisticsClassSeason") {
        val PARAM_SEASON = "Season"
    }

    class ACTION_MATCH_STATISTICS_CLASS(val cls: Class) : MetricAction("MatchStatisticsClass") {
        val PARAM_CLASS = "Class"
    }

    class ACTION_NEW_MATCH_START_WITH(val deck: Deck?) : MetricAction("MatchStatisticsClass") {
        val PARAM_DECK = "Deck"
        val PARAM_DECK_VALUE_OTHER = "Other"
    }

    class ACTION_NEW_MATCH_SAVE(val myDeckCls: Class, val myDeckType: DeckType, val optDeckCls: Class,
                                val optDeckType: DeckType, val mode: MatchMode, val season: String?,
                                val legendRank: Boolean, val deckTrackerUsed: Boolean) : MetricAction("MatchNew") {
        val PARAM_MY_CLS = "MyClass"
        val PARAM_MY_TYPE = "MyType"
        val PARAM_OPT_CLS = "OptClass"
        val PARAM_OPT_TYPE = "OptType"
        val PARAM_MODE = "Mode"
        val PARAM_SEASON = "Season"
        val PARAM_LEGEND = "Legend"
    }

    class ACTION_DONATE_BASIC : MetricAction("DonateBasic")
    class ACTION_DONATE_PRO : MetricAction("DonatePro")
    class ACTION_DONATE_NOT_NOW : MetricAction("DonateNotNow")
    class ACTION_NEW_VERSION_DETECTED : MetricAction("NewVersionDetected")
    class ACTION_NEW_VERSION_UPDATE_NOW : MetricAction("NewVersionUpdateNow")
    class ACTION_NEW_VERSION_UPDATE_LATER : MetricAction("NewVersionUpdateLater")

}

sealed class MetricScreen(val name: String) {

    companion object {

        const val EVENT_SCREEN_VIEW = "ScreenView"
        const val PARAM_SCREEN_VIEW_SCREEN = "Screen"

    }

    class SCREEN_CARDS_ALL : MetricScreen("CardsAll")
    class SCREEN_CARDS_COLLECTION : MetricScreen("CardsCollection")
    class SCREEN_CARDS_FAVORED : MetricScreen("CardsFavored")
    class SCREEN_CARDS_STATISTICS : MetricScreen("CardsStatistics")
    class SCREEN_CARD_DETAILS : MetricScreen("CardDetails")
    class SCREEN_DECKS_PUBLIC : MetricScreen("DecksPublic")
    class SCREEN_DECKS_OWNED : MetricScreen("DecksOwned")
    class SCREEN_DECKS_FAVORED : MetricScreen("DecksFavored")
    class SCREEN_DECK_DETAILS : MetricScreen("DeckDetails")
    class SCREEN_NEW_DECKS : MetricScreen("NewDeck")
    class SCREEN_MATCHES_STATISTICS : MetricScreen("MatchesStatistics")
    class SCREEN_MATCHES_STATISTICS_CLASS : MetricScreen("MatchesStatisticsClass")
    class SCREEN_MATCHES_HISTORY : MetricScreen("MatchesHistory")
    class SCREEN_NEW_MATCHES : MetricScreen("NewMatches")
    class SCREEN_DONATE : MetricScreen("Donate")

}