package com.ediposouza.teslesgendstracker.util

import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.DeckType
import com.ediposouza.teslesgendstracker.data.Match
import com.ediposouza.teslesgendstracker.data.MatchDeck

/**
 * Created by EdipoSouza on 1/5/17.
 */
object TestUtils {

    fun getTestMatches(): List<Match> {
        val decks = listOf(
                MatchDeck("", Class.ARCHER, DeckType.AGGRO, ""),
                MatchDeck("", Class.ASSASSIN, DeckType.AGGRO, ""),
                MatchDeck("", Class.BATTLEMAGE, DeckType.AGGRO, ""),
                MatchDeck("", Class.CRUSADER, DeckType.AGGRO, ""),
                MatchDeck("", Class.MAGE, DeckType.AGGRO, ""),
                MatchDeck("", Class.MONK, DeckType.AGGRO, ""),
                MatchDeck("", Class.SCOUT, DeckType.AGGRO, ""),
                MatchDeck("", Class.SORCERER, DeckType.AGGRO, ""),
                MatchDeck("", Class.SPELLSWORD, DeckType.AGGRO, ""),
                MatchDeck("", Class.WARRIOR, DeckType.AGGRO, ""),
                MatchDeck("", Class.STRENGTH, DeckType.AGGRO, ""),
                MatchDeck("", Class.INTELLIGENCE, DeckType.AGGRO, ""),
                MatchDeck("", Class.AGILITY, DeckType.AGGRO, ""),
                MatchDeck("", Class.WILLPOWER, DeckType.AGGRO, ""),
                MatchDeck("", Class.ENDURANCE, DeckType.AGGRO, ""),
                MatchDeck("", Class.NEUTRAL, DeckType.AGGRO, "")
        )
        return mutableListOf<Match>().apply {
            for ((indexPlayer, playerDeck) in decks.withIndex()) {
                for ((indexOpponent, opponentDeck) in decks.withIndex()) {
                    addAll(mutableListOf<Match>().apply {
                        for (i in 1..indexPlayer + 1) {
                            add(Match("", false, playerDeck, opponentDeck, 0, false, true))
                        }
                        for (i in 1..indexOpponent + 1) {
                            add(Match("", false, playerDeck, opponentDeck, 0, false, false))
                        }
                    })
                }
            }
        }
    }

}