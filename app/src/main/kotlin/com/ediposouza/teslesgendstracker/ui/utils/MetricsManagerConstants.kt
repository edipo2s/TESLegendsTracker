package com.ediposouza.teslesgendstracker.ui.utils

import android.support.annotation.StringDef

/**
 * Created by ediposouza on 08/12/16.
 */
abstract class MetricsManagerConstants() {

    companion object {

        const val SIGN_METHOD_GOOGLE: String = "Google"
        const val CONTENT_VIEW_TYPE_CARD: String = "Card"

        const val EVENT_SCREEN_VIEW: String = "ScreenView"
        const val PARAM_SCREEN: String = "Screen"

        const val SCREEN_CARDS_ALL: String = "CardsAll"
        const val SCREEN_CARDS_COLLECTION: String = "CardsCollection"
        const val SCREEN_CARDS_FAVORED: String = "CardsFavored"
        const val SCREEN_CARDS_STATISTICS: String = "CardsStatistics"
        const val SCREEN_CARD_DETAILS: String = "CardDetails"
        const val SCREEN_DECKS_PUBLIC: String = "DecksPublic"
        const val SCREEN_DECKS_OWNED: String = "DecksOwned"
        const val SCREEN_DECKS_FAVORED: String = "DecksFavored"
        const val SCREEN_DECK_DETAILS: String = "DeckDetails"
        const val SCREEN_NEW_DECKS: String = "NewDeck"

    }

    @StringDef(SCREEN_CARDS_ALL, SCREEN_CARDS_COLLECTION, SCREEN_CARDS_FAVORED, SCREEN_CARDS_STATISTICS,
            SCREEN_CARD_DETAILS, SCREEN_DECKS_PUBLIC, SCREEN_DECKS_OWNED, SCREEN_DECKS_FAVORED,
            SCREEN_DECK_DETAILS, SCREEN_NEW_DECKS)
    annotation class ScreenParam

}