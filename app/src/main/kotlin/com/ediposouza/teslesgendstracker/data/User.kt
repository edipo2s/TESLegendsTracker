package com.ediposouza.teslesgendstracker.data

/**
 * Created by ediposouza on 10/31/16.
 */
data class UserInfo(

        val name: String,
        val photoUrl: String

)

data class Match(

        val uuid: String,
        val first: Boolean,
        val player: MatchDeck,
        val opponent: MatchDeck,
        val rank: Int,
        val legend: Boolean,
        val win: Boolean

)

data class MatchDeck(

        val name: String,
        val cls: Class,
        val type: DeckType,
        val deck: String? = null,
        val version: String? = null

)