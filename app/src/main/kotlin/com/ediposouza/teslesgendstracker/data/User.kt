package com.ediposouza.teslesgendstracker.data

/**
 * Created by ediposouza on 10/31/16.
 */
data class User(

        val name: String,
        val collection: List<CardSlot>,
        val collectionPercent: Float,
        val decks: List<Deck>,
        val decksFavorites: List<Deck>,
        val matches: List<Match>

)

data class Match(

        val player: MatchDeck,
        val opponent: MatchDeck,
        val result: Boolean,
        val rack: Int,
        val legend: Boolean

)

data class MatchDeck(

        val name: String?,
        val cls: Class?,
        val type: DeckType?

)