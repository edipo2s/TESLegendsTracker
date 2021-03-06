package com.ediposouza.teslesgendstracker.util

import com.ediposouza.teslesgendstracker.data.*

/**
 * Created by ediposouza on 08/12/16.
 */
@Suppress("unused")
abstract class MetricsConstants {

    companion object {

        const val EVENT_LOGIN = "Login"
        const val EVENT_SIGNUP = "SignUp"
        const val PARAM_SIGN_METHOD = "method"
        const val PARAM_SIGN_METHOD_VALUE_GOOGLE = "Google"
        const val PARAM_SIGN_METHOD_VALUE_FACEBOOK = "Facebook"

        const val EVENT_VIEW_CARD = "ViewCard"
        const val PARAM_VIEW_CARD_ID = "ID"
        const val PARAM_VIEW_CARD_NAME = "Name"
        const val PARAM_VIEW_CARD_ATTR = "Attr"

        const val EVENT_VIEW_DECK = "ViewDeck"
        const val PARAM_VIEW_DECK_ID = "ID"
        const val PARAM_VIEW_DECK_NAME = "Name"
        const val PARAM_VIEW_DECK_CLASS = "Class"

        const val EVENT_VIEW_PATCH = "ViewPatch"
        const val PARAM_VIEW_PATCH_ID = "ID"

        const val EVENT_SEARCH = "Search"

        const val PARAM_CONTENT_VIEW_TYPE_CARD = "Card"
        const val PARAM_CONTENT_VIEW_TYPE_DECK = "Deck"
        const val PARAM_CONTENT_VIEW_TYPE_PATCH = "Patch"

        const val PARAM_MIXPANEL_USER_ID = "User ID"
        const val PARAM_MIXPANEL_USER_NAME = "\$name"
        const val PARAM_MIXPANEL_USER_EMAIL = "\$email"
        const val PARAM_MIXPANEL_USER_DONATE = "Donate"
    }

}

sealed class MetricAction(val name: String) {

    companion object {
        const val ALL = "All"
        const val CLEAR = "Clear"
    }

    class ACTION_APP_LAUNCH : MetricAction("AppLaunch")
    class ACTION_CARD_DETAILS_EXPAND : MetricAction("CardDetailsExpand")
    class ACTION_CARD_DETAILS_COLLAPSE : MetricAction("CardDetailsCollapse")
    class ACTION_CARD_DETAILS_CLOSE_TAP : MetricAction("CardDetailsCloseTap")
    class ACTION_CARD_DETAILS_FAVORITE : MetricAction("CardDetailsFavorite")
    class ACTION_CARD_DETAILS_UNFAVORITE : MetricAction("CardDetailsUnfavorite")
    class ACTION_COLLECTION_STATISTICS_EXPAND : MetricAction("CollectionStatisticsExpand")
    class ACTION_COLLECTION_STATISTICS_COLLAPSE : MetricAction("CollectionStatisticsCollapse")

    class ACTION_COLLECTION_CARD_RANTING(val card: Card, val rating: Int) : MetricAction("CardRating") {
        val PARAM_CARD = "Card"
        val PARAM_RATING = "Rating"
    }

    class ACTION_COLLECTION_CARD_QTD_CHANGE(val card: Card, val qtd: Int) : MetricAction("CollectionCardQtdChange") {
        val PARAM_CARD = "Card"
        val PARAM_QTD = "Qtd"
    }

    class ACTION_CARD_FILTER_FAVORITE(val checked: Boolean) : MetricAction("FilterCardFavorite") {
        val PARAM_CHECKED = "Checked"
    }

    class ACTION_CARD_FILTER_SET(val set: CardSet?) : MetricAction("FilterCardSet") {
        val PARAM_SET = "Set"
    }

    class ACTION_CARD_FILTER_ATTR(val attr: CardAttribute?) : MetricAction("FilterCardAttr") {
        val PARAM_ATTR = "Attr"
    }

    class ACTION_CARD_FILTER_RARITY(val rarity: CardRarity?) : MetricAction("FilterCardRarity") {
        val PARAM_RARITY = "Rarity"
    }

    class ACTION_CARD_FILTER_MAGICKA(val magicka: Int) : MetricAction("FilterCardMagicka") {
        val PARAM_MAGICKA = "Magicka"
    }

    class ACTION_CARD_FULL_ART(val card: Card) : MetricAction("ViewCardFullArt") {
        val PARAM_CARD_NAME = "Name"
    }

    class ACTION_CARD_FULL_ART_DOWNLOAD(val card: Card) : MetricAction("CardFullArtDownload") {
        val PARAM_CARD_NAME = "Name"
    }

    class ACTION_CARD_FULL_ART_SET_WALLPAPER(val card: Card) : MetricAction("CardFullArtSetWalpaper") {
        val PARAM_CARD_NAME = "Name"
    }

    class ACTION_CARD_START_SOUND_PLAY(val card: Card) : MetricAction("StartSoundPlay") {
        val PARAM_CARD_NAME = "Name"
    }

    class ACTION_CARD_START_SOUND_ATTACK(val card: Card) : MetricAction("StartSoundAttack") {
        val PARAM_CARD_NAME = "Name"
    }

    class ACTION_CARD_START_SOUND_EXTRA(val card: Card) : MetricAction("StartSoundExtra") {
        val PARAM_CARD_NAME = "Name"
    }

    class ACTION_CARD_SOUND_SET_RINGTONE(val card: Card, val soundType: String) : MetricAction("CardSoundSetRingtone") {
        val PARAM_CARD_NAME = "Name"
        val PARAM_CARD_SOUND_TYPE = "SoundType"
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

    class ACTION_DECK_UPDATE(val type: String, val patch: String, val private: Boolean) : MetricAction("DeckUpdate") {
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

    class ACTION_MATCH_STATISTICS_HIDE_EMPTY(val checked: Boolean) : MetricAction("MatchStatisticsHideEmpty") {
        val PARAM_CHECKED = "Checked"
    }

    class ACTION_MATCH_STATISTICS_CLASS_HIDE_EMPTY(val checked: Boolean) : MetricAction("MatchStatisticsClassHideEmpty") {
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

    class ACTION_MATCH_STATISTICS_CLASS(val cls: DeckClass) : MetricAction("MatchStatisticsClass") {
        val PARAM_CLASS = "Class"
    }

    class ACTION_NEW_MATCH_START_WITH(val deck: Deck?, val fromArena: Boolean = false) : MetricAction("MatchStart") {
        val PARAM_DECK = "Deck"
        val PARAM_DECK_VALUE_OTHER = "Other"
        val PARAM_FROM_ARENA = "FromArena"
    }

    class ACTION_NEW_MATCH_SAVE(val myDeckCls: DeckClass, val myDeckType: DeckType, val optDeckCls: DeckClass,
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

    class ACTION_START_DONATE : MetricAction("StartDonate")
    class ACTION_DONATE_BASIC : MetricAction("DonateBasic")
    class ACTION_DONATE_PRO : MetricAction("DonatePro")
    class ACTION_DONATE_NOT_NOW : MetricAction("DonateNotNow")
    class ACTION_NEW_VERSION_DETECTED : MetricAction("NewVersionDetected")
    class ACTION_NEW_VERSION_UPDATE_NOW : MetricAction("NewVersionUpdateNow")
    class ACTION_NEW_VERSION_UPDATE_LATER : MetricAction("NewVersionUpdateLater")
    class ACTION_SHARE : MetricAction("Share")
    class ACTION_ABOUT_DEVELOPER : MetricAction("AboutDeveloper")
    class ACTION_ABOUT_CVH : MetricAction("AboutCVH")
    class ACTION_ABOUT_DIREWOLF : MetricAction("AboutDireWolf")
    class ACTION_ABOUT_CHANGELOG : MetricAction("AboutChangelog")
    class ACTION_ABOUT_RATE : MetricAction("AboutRate")
    class ACTION_ABOUT_TWITTER : MetricAction("AboutTwitter")
    class ACTION_IMPORT_COLLECTION_CANCELLED : MetricAction("ImportCollectionCancelled")
    class ACTION_IMPORT_COLLECTION_FINISH(val cardsImported: Int) : MetricAction("ImportCollectionFinish") {
        val PARAM_CARDS_IMPORTED = "CardsImported"
    }

    class ACTION_ARTICLES_VIEW_NEWS(val article: Article) : MetricAction("ViewArticleNews") {
        val PARAM_ARTICLE = "ArticleID"
    }

    class ACTION_ARTICLES_VIEW_WORLD(val article: Article) : MetricAction("ViewArticleWorld") {
        val PARAM_ARTICLE = "ArticleID"
    }

    class ACTION_ARENA_FILTER_SEASON(val season: Season?) : MetricAction("FilterArenaSeason") {
        val PARAM_SEASON = "Season"
    }

    class ACTION_ARENA_START(val cls: DeckClass, val fromStartMenu: Boolean) : MetricAction("ArenaStart") {
        val PARAM_CLASS = "Class"
        val PARAM_FROM_START_MENU = "FromStartMenu"
    }

    class ACTION_ARENA_PICK(val card: Card) : MetricAction("ArenaPick") {
        val PARAM_CARD = "Card"
    }

    class ACTION_IMPORT_DECKS_CANCELLED : MetricAction("ImportDecksCancelled")
    class ACTION_IMPORT_DECKS_FINISH(val decksImported: Int) : MetricAction("ImportDecksFinish") {
        val PARAM_DECKS_IMPORTED = "DecksImported"
    }

    class ACTION_DECK_CHANGE_VIEW_MODE(val compatMode: Boolean) : MetricAction("DeckChangeViewMode") {
        val PARAM_COMPACT_MODE = "CompactMode"
    }

    class ACTION_DEEPLINK(val path: String, val params: String) : MetricAction("Deeplink") {
        val PARAM_PATH = "path"
        val PARAM_PATH_PARAMS = "pathParams"
    }

    class ACTION_DECK_CHANGE_LANGUAGE(val language: String) : MetricAction("ChangeLanguage") {
        val PARAM_LANGUAGE = "Language"
    }

    class ACTION_WABBATRACK_VIDEO : MetricAction("WabbaTrackVideo")
    class ACTION_WABBATRACK_WEBSITE : MetricAction("WabbaTrackWebsite")

}

sealed class MetricScreen(val name: String) {

    companion object {

        const val EVENT_SCREEN_VIEW = "ScreenView"
        const val PARAM_SCREEN_VIEW_SCREEN = "Screen"

    }

    class SCREEN_BASICS_LEVELUP : MetricScreen("BasicsLevelUp")
    class SCREEN_BASICS_RACES : MetricScreen("BasicsRaces")
    class SCREEN_BASICS_RANKED : MetricScreen("BasicsRanked")
    class SCREEN_CARDS_ALL : MetricScreen("CardsAll")
    class SCREEN_CARDS_COLLECTION : MetricScreen("CardsCollection")
    class SCREEN_CARDS_TOKENS : MetricScreen("CardsTokens")
    class SCREEN_CARDS_STATISTICS : MetricScreen("CardsStatistics")
    class SCREEN_CARD_DETAILS : MetricScreen("CardDetails")
    class SCREEN_CARD_FULL_ART : MetricScreen("CardFullArt")
    class SCREEN_DECKS_PUBLIC : MetricScreen("DecksPublic")
    class SCREEN_DECKS_OWNED : MetricScreen("DecksOwned")
    class SCREEN_DECKS_FAVORED : MetricScreen("DecksFavored")
    class SCREEN_DECK_DETAILS : MetricScreen("DeckDetails")
    class SCREEN_NEW_DECKS : MetricScreen("NewDeck")
    class SCREEN_MATCHES_STATISTICS : MetricScreen("MatchesStatistics")
    class SCREEN_MATCHES_STATISTICS_CLASS : MetricScreen("MatchesStatisticsClass")
    class SCREEN_MATCHES_HISTORY : MetricScreen("MatchesHistory")
    class SCREEN_NEW_MATCHES : MetricScreen("NewMatches")
    class SCREEN_ARTICLES_NEWS : MetricScreen("ArticlesNews")
    class SCREEN_ARTICLES_WORLD : MetricScreen("ArticlesWorld")
    class SCREEN_WABBATRACK_ABOUT : MetricScreen("WabbaTrackDeckAbout")
    class SCREEN_WABBATRACK_ARENATIER : MetricScreen("WabbaTrackDeckArenaTier")
    class SCREEN_WABBATRACK_DECKTRACKER : MetricScreen("WabbaTrackDeckTracker")
    class SCREEN_WABBATRACK_MATCHES : MetricScreen("WabbaTrackDeckMatches")
    class SCREEN_WABBATRACK_AUTOBUILD : MetricScreen("WabbaTrackDeckAutoBuild")
    class SCREEN_SEASONS : MetricScreen("Seasons")
    class SCREEN_SPOILER : MetricScreen("Spoiler")
    class SCREEN_ARENA : MetricScreen("Arena")
    class SCREEN_NEW_ARENA : MetricScreen("NewArena")
    class SCREEN_NEW_ARENA_DRAFT : MetricScreen("NewArenaDraft")
    class SCREEN_PATCH_DETAILS : MetricScreen("PatchDetails")
    class SCREEN_DONATE : MetricScreen("Donate")
    class SCREEN_ABOUT : MetricScreen("About")
    class SCREEN_IMPORT_COLLECTION : MetricScreen("ImportCollection")
    class SCREEN_IMPORT_DECKS : MetricScreen("ImportDecks")

}