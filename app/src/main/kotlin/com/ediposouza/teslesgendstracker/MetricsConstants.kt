package com.ediposouza.teslesgendstracker

/**
 * Created by ediposouza on 08/12/16.
 */
abstract class MetricsConstants() {

    companion object {

        const val PARAM_SIGN_METHOD_VALUE_GOOGLE = "Google"
        const val PARAM_CONTENT_VIEW_TYPE_CARD = "Card"

    }

}

sealed class MetricAction(val name: String) {

    class ACTION_CARD_DETAILS_EXPAND : MetricAction("CardDetailsExpand")
    class ACTION_CARD_DETAILS_COLLAPSE : MetricAction("CardDetailsCollapse")
    class ACTION_CARD_DETAILS_CLOSE_TAP : MetricAction("CardDetailsCloseTap")
    class ACTION_CARD_DETAILS_FAVORITE : MetricAction("CardDetailsFavorite")
    class ACTION_CARD_DETAILS_UNFAVORITE : MetricAction("CardDetailsUnfavorite")
    class ACTION_COLLECTION_STATISTICS_EXPAND : MetricAction("CollectionStatisticsExpand")
    class ACTION_COLLECTION_STATISTICS_COLLAPSE : MetricAction("CollectionStatisticsCollapse")

    class ACTION_COLLECTION_CARD_QTD_CHANGE : MetricAction("CollectionCardQtdChange") {
        companion object {
            val PARAM_QTD = "Qtd"
        }
    }

    class ACTION_CARD_FILTER_ATTR : MetricAction("FilterCardAttr") {
        companion object {
            const val PARAM_ATTR = "Attr"
        }
    }

    class ACTION_CARD_FILTER_RARITY : MetricAction("FilterCardRarity") {
        companion object {
            const val PARAM_RARITY = "Rarity"
            const val VALUE_CLEAR = "Clear"
        }
    }

    class ACTION_CARD_FILTER_MAGIKA : MetricAction("FilterCardMagika") {
        companion object {
            const val PARAM_MAGIKA = "Magika"
            const val VALUE_CLEAR = "Clear"
        }
    }

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

}